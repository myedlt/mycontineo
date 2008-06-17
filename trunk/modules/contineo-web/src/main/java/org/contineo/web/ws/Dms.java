package org.contineo.web.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.document.CheckinDocUtil;
import org.contineo.core.document.Document;
import org.contineo.core.document.History;
import org.contineo.core.document.Version;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.HistoryDAO;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.doxter.Storer;
import org.contineo.core.i18n.DateBean;
import org.contineo.core.i18n.Language;
import org.contineo.core.i18n.LanguageManager;
import org.contineo.core.searchengine.SearchDocument;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.searchengine.search.Search;
import org.contineo.core.searchengine.search.SearchOptions;
import org.contineo.core.security.ExtMenu;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.GroupDAO;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.security.dao.UserDAO;
import org.contineo.core.security.dao.UserDocDAO;
import org.contineo.core.text.AnalyzeText;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.core.util.IconSelector;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.util.SnippetStripper;

/**
 * Web service implementation
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class Dms {
	protected static Log log = LogFactory.getLog(Dms.class);

	/**
	 * Creates a new folder
	 * 
	 * @param username
	 * @param password
	 * @param name Name of the folder
	 * @param parent Parent identifier
	 * @return 'error' if error occurred, the folder identifier if it was
	 *         created
	 * @throws Exception
	 */
	public String createFolder(String username, String password, String name, int parent) throws Exception {
		checkCredentials(username, password);

		MenuDAO dao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu parentMenu = dao.findByPrimaryKey(parent);

		checkWriteEnable(username, parent);

		Menu menu = new Menu();
		menu.setMenuText(name);
		menu.setMenuParent(parent);
		menu.setMenuSort(1);
		menu.setMenuIcon("folder.png");
		menu.setMenuType(Menu.MENUTYPE_DIRECTORY);
		menu.setMenuHier(parentMenu.getMenuHier());
		menu.setMenuRef("");
		menu.setMenuGroup(parentMenu.getMenuGroupNames());

		boolean stored = dao.store(menu);
		menu.setMenuPath(parentMenu.getMenuPath() + "/" + menu.getMenuId());
		stored = dao.store(menu);

		if (!stored) {
			log.error("Folder " + name + " not created");
			return "error";
		} else {
			log.info("Created folder " + name);
		}

		return Integer.toString(menu.getMenuId());
	}

	/**
	 * Deletes an existing folder and all it's contained elements
	 * 
	 * @param username
	 * @param password
	 * @param folder Folder identifier
	 * @return A return code('ok' if all went ok, 'error' if some errors
	 *         occurred)
	 * @throws Exception
	 */
	public String deleteFolder(String username, String password, int folder) throws Exception {
		checkCredentials(username, password);

		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		int parentId = -1;

		boolean sqlop = true;

		Menu menu = mdao.findByPrimaryKey(folder);
		if (menu == null || menu.getMenuType() != Menu.MENUTYPE_DIRECTORY) {
			log.error("Folder " + folder + " not found");
			return "error";
		}

		checkWriteEnable(username, folder);

		// remove sub-elements
		Collection children = mdao.findByParentId(folder);
		Iterator childIter = children.iterator();

		while (childIter.hasNext()) {
			Menu m = (Menu) childIter.next();
			deleteFile(m, m.getMenuId(), username);
		}

		boolean deleted = mdao.delete(folder);

		if (!deleted) {
			sqlop = false;
		}

		parentId = menu.getMenuParent();

		Collection<ExtMenu> coll2 = new ArrayList<ExtMenu>();
		Collection coll = mdao.findByUserName(username, parentId);
		Iterator iter = coll.iterator();
		SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		String docpath = settings.getValue("docdir");
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);

		while (iter.hasNext()) {
			Menu m = (Menu) iter.next();
			ExtMenu xmenu = new ExtMenu(m);

			// calculate size of menu
			int size = 0;

			if (m.getMenuType() == Menu.MENUTYPE_FILE) {
				long sz = FileBean
						.getSize(docpath + "/" + m.getMenuPath() + "/" + m.getMenuId() + "/" + m.getMenuRef());
				sz = sz / 1024;
				size = (int) sz;
			} else {
				size = menuDao.findByUserName(username, m.getMenuId()).size();
			}

			xmenu.setSize(size);

			// check if menu is writable
			boolean writable = false;

			if (menuDao.isWriteEnable(m.getMenuId(), username)) {
				writable = true;
			} else {
				writable = false;
			}

			xmenu.setWritable(writable);

			// only done on documents
			// set the checkout/checkin status of the document
			// set the checkout user, if the document is checked out
			if (m.getMenuType() == Menu.MENUTYPE_FILE) {
				DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
				Document doc = ddao.findByMenuId(m.getMenuId());

				if (doc != null) {
					xmenu.setDocStatus(doc.getDocStatus());
					xmenu.setCheckoutUser(doc.getCheckoutUser());
				}
			}

			coll2.add(xmenu);
		}

		if (!sqlop) {
			log.error("Some elements were not deleted");
			return "error";
		} else {
			return "ok";
		}
	}

	public String createDocument(String username, String password, int parent, String docName, String source,
			String sourceDate, String author, String sourceType, String coverage, String language, String keywords,
			String versionDesc, String filename, String groups) throws Exception {
		checkCredentials(username, password);

		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu parentMenu = mdao.findByPrimaryKey(parent);

		if (parentMenu == null) {
			log.error("Menu " + parentMenu + " not found");
			return "error - parent not found";
		}

		String ext = filename.substring(filename.lastIndexOf(".") + 1);
		String icon = IconSelector.selectIcon(ext);

		String[] groupNames = parseGroups(groups);
		if (groupNames.length < 1)
			return "error - no valid groups";

		checkWriteEnable(username, parent);

		// Makes menuPath
		String menupath = new StringBuilder(parentMenu.getMenuPath()).append("/").append(parentMenu.getMenuId())
				.toString();
		int menuhier = parentMenu.getMenuHier();

		// Makes new Menu
		Menu menu = new Menu();
		menu.setMenuParent(parentMenu.getMenuId());
		menu.setMenuPath(menupath);
		menu.setMenuHier(menuhier++);
		menu.setMenuIcon(icon.toString());
		menu.setMenuType(Menu.MENUTYPE_FILE);
		menu.setMenuRef(filename);
		menu.setMenuGroup(groupNames);

		// and stores it
		mdao.store(menu);

		// Stores the document in the repository
		SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		String path = new StringBuilder(settings.getValue("docdir")).append("/").append(menupath).append("/").append(
				String.valueOf(menu.getMenuId())).append("/").toString();
		String mpath = new StringBuilder(menupath).append("/").append(String.valueOf(menu.getMenuId())).toString();

		// We can obtain the request (incoming) MessageContext as follows
		MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

		// Now we can access the 'document' attachment in the response
		DataHandler handler = inMessageContext.getAttachmentMap().getDataHandler("document");

		// Get file to upload inputStream
		InputStream stream = handler.getInputStream();

		// stores it in folder
		Storer storer = (Storer) Context.getInstance().getBean(Storer.class);
		try {
			storer.store(stream, mpath, filename, "1.0");
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		File f = new File(new StringBuilder(path).append(filename).toString());

		// Parses the file where it is already stored
		Parser parser = ParserFactory.getParser(f);
		String content = null;
		String name = "";
		String auth = "";
		String keys = keywords;

		// and gets some fields
		if (parser != null) {
			content = parser.getContent();
			if (content == null)
				content = "";
			name = parser.getTitle();
			if (StringUtils.isNotEmpty(docName))
				name = docName;
			auth = parser.getAuthor();
			if (StringUtils.isNotEmpty(author))
				auth = author;

			if (StringUtils.isEmpty(keys)) {
				AnalyzeText analyzer = new AnalyzeText();
				keys = analyzer.getTerms(5, content.toString(), language);
			}
		}

		if (content == null) {
			content = "";
		}

		if (StringUtils.isEmpty(source)) {
			source = settings.getValue("defaultSource");
		}

		// update the file size and the menu text
		menu.setMenuText(name);
		mdao.store(menu);

		DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);

		Document doc = new Document();
		Version vers = new Version();
		doc.setMenu(menu);

		if ((name != null) && !name.equals("")) {
			doc.setDocName(name);
		} else {
			doc.setDocName(filename.substring(0, filename.lastIndexOf(".")));
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		doc.setDocDate(DateBean.toCompactString());
		if (StringUtils.isNotEmpty(sourceDate))
			doc.setSourceDate(DateBean.toCompactString(df.parse(sourceDate)));
		doc.setDocPublisher(username);
		doc.setDocStatus(Document.DOC_CHECKED_IN);
		doc.setDocType(filename.substring(filename.lastIndexOf(".") + 1));
		doc.setDocVersion("1.0");
		doc.setSource(source);
		doc.setSourceAuthor(auth);

		doc.setSourceType(sourceType != null ? sourceType : "");
		doc.setCoverage(coverage != null ? coverage : "");
		doc.setLanguage(language);

		/* insert initial version 1.0 */
		vers.setVersion("1.0");
		vers.setVersionComment(versionDesc);
		vers.setVersionDate(DateBean.toCompactString());

		vers.setVersionUser(username);
		doc.addVersion(vers);

		/* create keywords for the document */
		Set<String> keysSet = ddao.toKeywords(keys);
		doc.setKeywords(keysSet);

		boolean stored = ddao.store(doc);

		/* create history entry */
		History history = new History();
		history.setDocId(doc.getDocId());
		history.setDate(DateBean.toCompactString());
		history.setUsername(username);
		history.setEvent(History.STORED);

		HistoryDAO historyDAO = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);
		historyDAO.store(history);

		/* create search index entry */
		String lang = doc.getLanguage();
		Indexer index = (Indexer) Context.getInstance().getBean(Indexer.class);
		int luceneId = index.addFile(new File(new StringBuilder(path).append(File.separator).append(menu.getMenuRef())
				.toString()), doc, getContent(handler.getInputStream()).toString(), lang);
		SearchDocument searchDoc = new SearchDocument();
		searchDoc.setLuceneId(luceneId);
		searchDoc.setMenuId(menu.getMenuId());

		// get the index for the document
		Language cLanguage = LanguageManager.getInstance().getLanguage(lang);
		if (cLanguage == null)
			cLanguage = LanguageManager.getInstance().getDefaultLanguage();
		
		searchDoc.setIndex(cLanguage.getIndex());

		SearchDocumentDAO searchDocDao = (SearchDocumentDAO) Context.getInstance().getBean(SearchDocumentDAO.class);
		searchDocDao.store(searchDoc);

		// Update file size
		mdao.store(menu);

		/* create messages */
		if (!stored) {
			return "error";
		} else {
			return "ok";
		}
	}

	/**
	 * Parses a comma-separated list of group names checking the existence
	 * 
	 * @param groups The list of comma-separated group names
	 * @return The array of existing groups
	 */
	private String[] parseGroups(String groups) {
		ArrayList<String> array = new ArrayList<String>();
		GroupDAO gdao = (GroupDAO) Context.getInstance().getBean(GroupDAO.class);

		StringTokenizer st = new StringTokenizer(groups, ",", false);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (gdao.findByPrimaryKey(token) != null)
				array.add(token);
		}
		return array.toArray(new String[] {});
	}

	/**
	 * Downloads a document. The document content is sent as attachment
	 * identified by 'document'.
	 * 
	 * @param username
	 * @param password
	 * @param id The document menu id
	 * @param version The specific version(it can be empty)
	 * @return A return code('ok' if all went ok)
	 * @throws Exception
	 */
	public String downloadDocument(String username, String password, int id, String version) throws Exception {
		checkCredentials(username, password);

		checkReadEnable(username, id);

		DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		Document doc = docDao.findByMenuId(id);
		Menu menu = doc.getMenu();

		SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		String path = settings.getValue("docdir") + "/" + menu.getMenuPath() + "/" + menu.getMenuId();

		String menuref;

		if (StringUtils.isEmpty(version)) {
			menuref = menu.getMenuRef();
		} else {
			menuref = version;
		}

		// load the file from the file system and output it to the
		// responseWriter
		File file = new File(path + "/" + menuref);

		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}

		log.debug("Attach file " + file.getPath());

		// We can obtain the request (incoming) MessageContext as follows
		MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

		// We can obtain the operation context from the request message context
		OperationContext operationContext = inMessageContext.getOperationContext();

		// Now we can obtain the response (outgoing) message context from the
		// operation context
		MessageContext outMessageContext = operationContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

		// Now we can append the 'document' attachment to the response
		DataHandler handler = new DataHandler(new FileDataSource(file));
		outMessageContext.addAttachment("document", handler);

		return "ok";
	}

	/**
	 * Retrieves the document meta-data
	 * 
	 * @param username
	 * @param password
	 * @param id The document menu id
	 * @return
	 * @throws Exception
	 */
	public DocumentInfo downloadDocumentInfo(String username, String password, int id) throws Exception {
		checkCredentials(username, password);

		checkReadEnable(username, id);

		// Retrieve the document
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		Document doc = docDao.findByMenuId(id);
		Menu menu = doc.getMenu();
		Menu parentMenu = menuDao.findByPrimaryKey(menu.getMenuParent());

		// Populate document's metadata
		DocumentInfo info = new DocumentInfo();
		info.setId(menu.getMenuId());
		info.setName(doc.getDocName());
		info.setAuthor(doc.getSourceAuthor());
		info.setSourceDate(convertDateToXML(doc.getSourceDate()));
		info.setLanguage(doc.getLanguage());
		info.setParentId(menu.getMenuParent());
		info.setParentName(parentMenu.getMenuText());
		info.setSource(doc.getSource());
		info.setType(doc.getSourceType());
		info.setUploadDate(convertDateToXML(doc.getDocDate()));
		info.setWriteable(menuDao.isMenuWriteable(id, username));
		info.setUploadUser(doc.getDocPublisher());
		info.setCoverage(doc.getCoverage());

		Set<Version> versions = doc.getVersions();
		for (Version version : versions) {
			VersionInfo vInfo = new VersionInfo();
			vInfo.setDate(convertDateToXML(version.getVersionDate()));
			vInfo.setDescription(version.getVersionComment());
			vInfo.setId(version.getVersion());
			info.addVersion(vInfo);
		}

		return info;
	}

	/**
	 * Downloads folder metadata
	 * 
	 * @param username
	 * @param password
	 * @param folder The folder identifier
	 * @return The folder metadata
	 * @throws Exception
	 */
	public FolderContent downloadFolderContent(String username, String password, int folder) throws Exception {
		FolderContent folderContent = new FolderContent();

		checkCredentials(username, password);

		checkReadEnable(username, folder);

		// Retrieve the referenced menu and it's parent populating the folder
		// content
		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu folderMenu = mdao.findByPrimaryKey(folder);
		folderContent.setId(folder);
		folderContent.setName(folderMenu.getMenuText());
		folderContent.setParentId(folderMenu.getMenuParent());
		Menu parenMenu = mdao.findByPrimaryKey(folderContent.getParentId());
		folderContent.setParentName(parenMenu.getMenuText());

		// Now search for sub-elements
		Collection<Menu> children = mdao.findChildren(folder);
		for (Menu menu : children) {
			Content content = new Content();
			content.setId(menu.getMenuId());
			content.setName(menu.getMenuText());
			content.setWriteable(mdao.isReadEnable(menu.getMenuId(), username) ? 1 : 0);

			if (menu.getMenuType() == Menu.MENUTYPE_FILE)
				folderContent.addDocument(content);
			else if (menu.getMenuType() == Menu.MENUTYPE_DIRECTORY)
				folderContent.addFolder(content);
		}

		return folderContent;
	}

	/**
	 * Deletes a document
	 * 
	 * @param username
	 * @param password
	 * @param id The document menu id
	 * @return A return code('ok' if all went ok)
	 * @throws Exception
	 */
	public String deleteDocument(String username, String password, int id) throws Exception {
		checkCredentials(username, password);

		checkWriteEnable(username, id);

		DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		docDao.deleteByMenuId(id);

		return "ok";
	}

	/**
	 * Marks the document as checked out
	 * 
	 * @param username
	 * @param password
	 * @param id The document menu id
	 * @return A return code('ok' if all went ok)
	 * @throws Exception
	 */
	public String checkout(String username, String password, int id) throws Exception {
		checkCredentials(username, password);

		checkWriteEnable(username, id);

		DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		Document document = ddao.findByMenuId(id);

		if (document.getDocStatus() == Document.DOC_CHECKED_IN) {
			document.setCheckoutUser(username);
			document.setDocStatus(Document.DOC_CHECKED_OUT);
			document.setMenu(document.getMenu());
			ddao.store(document);

			/* create history checkout entry */
			History history = new History();
			history.setDocId(document.getDocId());
			history.setDate(DateBean.toCompactString());
			history.setUsername(username);
			history.setEvent(History.CHECKOUT);

			HistoryDAO historyDAO = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);
			historyDAO.store(history);

			log.debug("Checked out document " + id);
		} else {
			return "already checked out";
		}
		return "ok";
	}

	/**
	 * Uploads a new version of an already checked out document
	 * 
	 * @param username
	 * @param password
	 * @param id
	 * @param filename
	 * @param description
	 * @param type
	 * @return ok if all went right
	 * @throws Exception
	 */
	public String checkin(String username, String password, int id, String filename, String description, String type)
			throws Exception {
		checkCredentials(username, password);

		checkWriteEnable(username, id);

		DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		Document document = ddao.findByMenuId(id);

		if (document.getDocStatus() == Document.DOC_CHECKED_OUT) {
			// determines the kind of version to create
			Version.VERSION_TYPE versionType;

			if ("release".equals(type)) {
				versionType = Version.VERSION_TYPE.NEW_RELEASE;
			} else if ("subversion".equals(type)) {
				versionType = Version.VERSION_TYPE.NEW_SUBVERSION;
			} else {
				versionType = Version.VERSION_TYPE.OLD_VERSION;
			}

			try {
				// We can obtain the request (incoming) MessageContext as
				// follows
				MessageContext inMessageContext = MessageContext.getCurrentMessageContext();

				// Now we can access the 'document' attachment in the
				// response
				DataHandler handler = inMessageContext.getAttachmentMap().getDataHandler("document");

				// Get file to upload inputStream
				InputStream stream = handler.getInputStream();

				// checkin the document; throws an exception if
				// something goes wrong
				CheckinDocUtil.checkinDocument(document.getDocId(), stream, filename, username, versionType,
						description);

				/* create positive log message */
				log.info("Document " + id + " checked in");
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} else {
			return "document not checked out";
		}

		return "ok";
	}

	/**
	 * Search for a documents
	 * 
	 * @param username
	 * @param password
	 * @param query The query string
	 * @param indexLanguage The index language, if null all indexes are
	 *        considered
	 * @param queryLanguage The language in which the query is expressed
	 * @param maxHits The maximum number of hits to be returned
	 * @return The objects representing the search result
	 * @throws Exception
	 */
	public SearchResult search(String username, String password, String query, String indexLanguage,
			String queryLanguage, int maxHits) throws Exception {
		checkCredentials(username, password);

		SearchResult searchResult = new SearchResult();

		SearchOptions opt = new SearchOptions();
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("content");
		fields.add("keywords");
		fields.add("name");

		String[] flds = (String[]) fields.toArray(new String[fields.size()]);
		opt.setFields(flds);

		ArrayList<String> languages = new ArrayList<String>();
		if (StringUtils.isEmpty(indexLanguage)) {
			languages.add("en");
			languages.add("it");
			languages.add("fr");
			languages.add("de");
			languages.add("es");
			languages.add("ro");
		} else {
			languages.add(indexLanguage);
		}

		String[] langs = (String[]) languages.toArray(new String[languages.size()]);
		opt.setLanguages(langs);
		opt.setQueryStr(query);
		opt.setUsername(username);
		opt.setFormat("all");

		// Execute the search
		Search lastSearch = new Search(opt, queryLanguage);
		lastSearch.setMaxHits(maxHits);
		List<org.contineo.core.searchengine.search.Result> tmp = lastSearch.search();

		// Prepares the result array
		ArrayList<Result> result = new ArrayList<Result>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		for (org.contineo.core.searchengine.search.Result res : tmp) {
			Result newRes = new Result();
			newRes.setId(res.getMenuId());
			newRes.setDate(df.format(res.getDate()));
			newRes.setName(res.getName());
			newRes.setSummary(SnippetStripper.strip(res.getSummary()));
			newRes.setLength(res.getSize());
			result.add(newRes);
		}

		searchResult.setTotalHits(result.size());
		searchResult.setResult(result.toArray(new Result[] {}));
		searchResult.setEstimatedHitsNumber(lastSearch.getEstimatedHitsNumber());
		searchResult.setTime(lastSearch.getExecTime());
		searchResult.setMoreHits(lastSearch.isMoreHitsPresent() ? 1 : 0);

		log.info("User:" + username + " Query:" + query);
		log.info("Results number:" + result.size());

		return searchResult;
	}

	/**
	 * Check provided credentials
	 * 
	 * @param username The username
	 * @param password The password
	 * @throws Exception Raised if the user is not authenticated
	 */
	private void checkCredentials(String username, String password) throws Exception {
		UserDAO userDao = (UserDAO) Context.getInstance().getBean(UserDAO.class);

		if (!userDao.validateUser(username, password)) {
			log.error("Invalid credentials " + username + "/" + password);
			throw new Exception("Invalid credentials");
		}
	}

	private void checkWriteEnable(String username, int menuId) throws Exception {
		MenuDAO dao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		if (!dao.isWriteEnable(menuId, username)) {
			log.error("User " + username + " cannot write element " + menuId);
			throw new Exception("The provided user has no write permissions");
		}
	}

	private void checkReadEnable(String username, int menuId) throws Exception {
		MenuDAO dao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		if (!dao.isReadEnable(menuId, username)) {
			log.error("User " + username + " cannot read element " + menuId);
			throw new Exception("The provided user has no read permissions");
		}
	}

	private boolean deleteFile(Menu menu, int id, String username) {
		boolean sqlop = true;

		try {
			SearchDocumentDAO searchDocDao = (SearchDocumentDAO) Context.getInstance().getBean(SearchDocumentDAO.class);
			searchDocDao.deleteByMenuId(id);

			UserDocDAO uddao = (UserDocDAO) Context.getInstance().getBean(UserDocDAO.class);
			uddao.delete(username, id);

			DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
			Document doc = ddao.findByMenuId(id);

			if (doc != null) {
				Indexer indexer = (Indexer) Context.getInstance().getBean(Indexer.class);
				indexer.deleteFile(String.valueOf(id), doc.getLanguage());
			}

			TermDAO termDao = (TermDAO) Context.getInstance().getBean(TermDAO.class);
			boolean deleted2 = termDao.delete(id);

			boolean deleted1 = ddao.deleteByMenuId(id);

			if (!deleted1 || !deleted2) {
				sqlop = false;
			}

			MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
			boolean deleted = mdao.delete(id);

			if (!deleted) {
				sqlop = false;
			}

			// String path = conf.getValue("docdir");
			String menupath = menu.getMenuPath() + "/" + String.valueOf(id);

			// FileBean.deleteDir(path);
			Storer storer = (Storer) Context.getInstance().getBean(Storer.class);
			storer.delete(menupath);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			sqlop = false;
		}

		return sqlop;
	}

	/**
	 * converts a date from contineo's internal representation to a valid XML
	 * string
	 */
	protected String convertDateToXML(String date) {
		if (date.length() < 9) {
			return DateBean.convertDate("yyyyMMdd", "yyyy-MM-dd", date);
		} else {
			return DateBean.convertDate("yyyyMMdd HHmmss", "yyyy-MM-dd HH:mm:ss", date);
		}
	}

	private StringBuffer getContent(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int read = 0;
		StringBuffer content = new StringBuffer();

		while ((read = in.read(buffer, 0, 1024)) >= 0) {
			content.append(new String(buffer, 0, read));
		}

		return content;
	}
}