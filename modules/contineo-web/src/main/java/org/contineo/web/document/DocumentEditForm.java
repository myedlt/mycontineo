package org.contineo.web.document;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.document.CheckinDocUtil;
import org.contineo.core.document.Document;
import org.contineo.core.document.History;
import org.contineo.core.document.Version;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.HistoryDAO;
import org.contineo.core.i18n.DateBean;
import org.contineo.core.i18n.Language;
import org.contineo.core.i18n.LanguageManager;
import org.contineo.core.searchengine.SearchDocument;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.core.util.IconSelector;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;
import org.contineo.web.navigation.PageContentBean;
import org.contineo.web.upload.InputFileBean;

/**
 * Base form for document editing
 * 
 * @author Marco Meschieri
 * @version $Id: DocumentEditForm.java,v 1.9 2006/09/03 16:24:37 marco Exp $
 * @since 3.0
 */
public class DocumentEditForm {
	protected static Log log = LogFactory.getLog(DocumentEditForm.class);

	private String docName;

	private int menuParent;

	private int menuSort = 0;

	private String source;

	private String sourceAuthor;

	private Date sourceDate;

	private Date docDate;

	private String sourceType;

	private String coverage;

	private String language;

	private String keywords;

	private String versionDesc;

	private String filename;

	private Menu menu;

	private String[] menuGroup;

	private DocumentRecord record;

	private boolean readOnly = false;

	private DocumentNavigation documentNavigation;

	public DocumentEditForm() {
		reset();
	}

	public void reset() {
		docName = "";
		menuParent = -1;
		menuSort = 0;
		source = "";
		sourceAuthor = "";
		sourceDate = null;
		docDate = new Date();
		sourceType = "";
		coverage = "";
		language = "";
		keywords = "";
		versionDesc = "";
		filename = "";
		menu = null;
		menuGroup = null;
	}

	public void init(DocumentRecord record) {
		this.record = record;
		this.menu = record.getMenu();

		Document doc = record.getDocument();
		setDocName(doc.getDocName());

		setMenuParent(doc.getMenu().getMenuParent());
		setSource(doc.getSource());
		if (StringUtils.isEmpty(doc.getSource())) {
			SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
			setSource(settings.getValue("defaultSource"));
		}
		setSourceAuthor(doc.getSourceAuthor());

		if (StringUtils.isNotEmpty(doc.getSourceDate())) {
			setSourceDate(DateBean.dateFromCompactString(doc.getSourceDate()));
		}

		if (StringUtils.isNotEmpty(doc.getDocDate())) {
			setDocDate(DateBean.dateFromCompactString(doc.getDocDate()));
		}

		setLanguage(doc.getLanguage());
		setKeywords(doc.getKeywordsString());
		setCoverage(doc.getCoverage());
		setSourceType(doc.getSourceType());

		String[] groupNames = record.getMenu().getMenuGroupNames();
		setMenuGroup(groupNames);
	}

	/**
	 * @return Returns the docName.
	 */
	public String getDocName() {
		return docName;
	}

	/**
	 * @return Returns the menuParent.
	 */
	public int getMenuParent() {
		return menuParent;
	}

	/**
	 * @return Returns the menuSort.
	 */
	public int getMenuSort() {
		return menuSort;
	}

	/**
	 * @return Returns the source.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @return Returns the sourceAuthor.
	 */
	public String getSourceAuthor() {
		return sourceAuthor;
	}

	/**
	 * @return Returns the sourceDate.
	 */
	public Date getSourceDate() {
		return sourceDate;
	}

	/**
	 * @return Returns the sourceType.
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * @return Returns the coverage.
	 */
	public String getCoverage() {
		return coverage;
	}

	/**
	 * @return Returns the language.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @return Returns the keywords.
	 */
	public String getKeywords() {
		return keywords;
	}

	/**
	 * @return Returns the versionDesc.
	 */
	public String getVersionDesc() {
		return versionDesc;
	}

	/**
	 * @return Returns the menuGroup.
	 */
	public String[] getMenuGroup() {
		return menuGroup;
	}

	/**
	 * @param docName The docName to set.
	 */
	public void setDocName(String name) {
		docName = name;
	}

	/**
	 * @param menuParent The menuParent to set.
	 */
	public void setMenuParent(int parent) {
		menuParent = parent;
	}

	/**
	 * @param menuSort The menuSort to set.
	 */
	public void setMenuSort(int sort) {
		menuSort = sort;
	}

	/**
	 * @param source The source to set.
	 */
	public void setSource(String src) {
		source = src;
	}

	/**
	 * @param sourceAuthor The sourceAuthor to set.
	 */
	public void setSourceAuthor(String author) {
		sourceAuthor = author;
	}

	/**
	 * @param sourceDate The sourceDate to set.
	 */
	public void setSourceDate(Date date) {
		sourceDate = date;
	}

	/**
	 * @param sourceType The sourceType to set.
	 */
	public void setSourceType(String type) {
		sourceType = type;
	}

	/**
	 * @param coverage The coverage to set.
	 */
	public void setCoverage(String cover) {
		coverage = cover;
	}

	/**
	 * @param language The language to set.
	 */
	public void setLanguage(String lang) {
		language = lang;
	}

	/**
	 * @param keywords The keywords to set.
	 */
	public void setKeywords(String words) {
		keywords = words;
	}

	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * @return Returns the menu.
	 */
	public Menu getMenu() {
		return menu;
	}

	/**
	 * @param menu The menu to set.
	 */
	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	/**
	 * @param versionDesc The versionDesc to set.
	 */
	public void setVersionDesc(String desc) {
		versionDesc = desc;
	}

	/**
	 * @param menuGroups The menuGroup to set.
	 */
	public void setMenuGroup(String[] group) {
		menuGroup = group;
	}

	public Date getDocDate() {
		return docDate;
	}

	public void setDocDate(Date docDate) {
		this.docDate = docDate;
	}

	public String toString() {
		return (new ReflectionToStringBuilder(this) {
			protected boolean accept(java.lang.reflect.Field f) {
				return super.accept(f);
			}
		}).toString();
	}

	/**
	 * Saves data into a new Document. Saves the information provided in the
	 * document form. That also includes updating the search index for example.
	 * This method is invoked in the document's upload wizard
	 */
	public String save() {
		if (SessionManagement.isValid()) {
			try {
				MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
				DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);

				String userName = SessionManagement.getUsername();
				Menu parent = documentNavigation.getSelectedDir().getMenu();

				if (mdao.isWriteEnable(parent.getMenuId(), userName)) {
					Document doc = new Document();
					Version vers = new Version();

					// extract file extension of the new file and select a file
					// icon based on the extension
					FacesContext facesContext = FacesContext.getCurrentInstance();
					InputFileBean inputFile = ((InputFileBean) facesContext.getApplication().createValueBinding(
							"#{inputFile}").getValue(FacesContext.getCurrentInstance()));
					String filename = inputFile.getFileName();
					String ext = filename.substring(filename.lastIndexOf(".") + 1);
					String name = getDocName();

					if (StringUtils.isNotEmpty(name)) {
						menu.setMenuText(name);
					} else {
						menu.setMenuText(filename.substring(0, filename.lastIndexOf(".")));
					}

					String icon = IconSelector.selectIcon(ext);
					menu.setMenuIcon(icon.toString());
					menu.setMenuSort(getMenuSort());
					menu.setMenuType(Menu.MENUTYPE_FILE);
					menu.setMenuRef(filename);

					updateGroups(menu);
					mdao.store(menu);

					doc.setMenu(menu);

					if ((name != null) && !name.equals("")) {
						doc.setDocName(name);
					} else {
						doc.setDocName(filename.substring(0, filename.lastIndexOf(".")));
					}

					doc.setDocDate(DateBean.toCompactString());
					if (sourceDate != null)
						doc.setSourceDate(DateBean.toCompactString(sourceDate));
					else
						doc.setSourceDate(null);
					doc.setDocPublisher(userName);
					doc.setDocStatus(Document.DOC_CHECKED_IN);
					doc.setDocType(filename.substring(filename.lastIndexOf(".") + 1));
					doc.setDocVersion("1.0");
					doc.setSource(getSource());
					doc.setSourceAuthor(getSourceAuthor());

					doc.setSourceType(getSourceType());
					doc.setCoverage(getCoverage());
					doc.setLanguage(getLanguage());

					/* insert initial version 1.0 */
					vers.setVersion("1.0");
					vers.setVersionComment(getVersionDesc());
					vers.setVersionDate(DateBean.toCompactString());

					vers.setVersionUser(userName);
					doc.addVersion(vers);

					/* create keywords for the document */
					Set<String> keywords = ddao.toKeywords(getKeywords());
					doc.setKeywords(keywords);

					boolean stored = ddao.store(doc);

					/* create history entry */
					History history = new History();
					history.setDocId(doc.getDocId());
					history.setDate(DateBean.toCompactString());
					history.setUsername(userName);
					history.setEvent(History.STORED);

					HistoryDAO historyDAO = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);
					historyDAO.store(history);

					String menupath = menu.getMenuPath();
					SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
					String path = new StringBuilder(settings.getValue("docdir")).append("/").append(menupath).append(
							"/").append(String.valueOf(menu.getMenuId())).append("/").toString();

					/* create search index entry */
					String lang = doc.getLanguage();
					Indexer index = (Indexer) Context.getInstance().getBean(Indexer.class);
					int luceneId = index.addFile(new File(new StringBuilder(path).append("/").append(menu.getMenuRef())
							.toString()), doc, getDocumentContent(doc), lang);
					SearchDocument searchDoc = new SearchDocument();
					searchDoc.setLuceneId(luceneId);
					searchDoc.setMenuId(menu.getMenuId());

					// get the index for the document
					Language cLanguage = LanguageManager.getInstance().getLanguage(lang);
					if (cLanguage == null)
						cLanguage = LanguageManager.getInstance().getDefaultLanguage();
					
					searchDoc.setIndex(cLanguage.getIndex());

					SearchDocumentDAO searchDocDao = (SearchDocumentDAO) Context.getInstance().getBean(
							SearchDocumentDAO.class);
					searchDocDao.store(searchDoc);

					// Update file size
					mdao.store(menu);

					/* create messages */
					if (!stored) {
						Messages.addMessage(FacesMessage.SEVERITY_ERROR, Messages.getMessage("errors.action.savedoc"),
								Messages.getMessage("errors.action.savedoc"));
					} else {
						Messages.addMessage(FacesMessage.SEVERITY_INFO, Messages.getMessage("msg.jsp.docstored"),
								Messages.getMessage("msg.jsp.docstored"));
					}

					// Launch document re-indexing
					reindexDocument(doc, lang);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addMessage(FacesMessage.SEVERITY_ERROR, "errors.action.savedoc", "errors.action.savedoc");
			} finally {
				reset();
			}

			return null;
		} else {
			return "login";
		}
	}

	/**
	 * Updates data into a Document. Saves the information provided in the
	 * document form. That also includes updating the search index for example.
	 * This method is invoked for document's editing
	 */
	public String update() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		DocumentNavigation navigation = ((DocumentNavigation) facesContext.getApplication().createValueBinding(
				"#{documentNavigation}").getValue(FacesContext.getCurrentInstance()));
		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		HistoryDAO historyDAO = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);

		if (SessionManagement.isValid()) {
			try {
				Document doc = record.getDocument();
				Menu menu = mdao.findByPrimaryKey(doc.getMenuId());

				String name = getDocName();

				if (StringUtils.isNotEmpty(name)) {
					menu.setMenuText(name);
				}

				mdao.store(menu);

				doc.setMenu(menu);
				doc.setDocName(name);
				doc.setSource(getSource());
				doc.setSourceAuthor(getSourceAuthor());
				if (sourceDate != null)
					doc.setSourceDate(DateBean.toCompactString(sourceDate));
				else
					setSourceDate(null);
				doc.setSourceType(getSourceType());
				doc.setCoverage(getCoverage());

				// Intercept language changes
				String oldLang = doc.getLanguage();
				doc.setLanguage(getLanguage());

				doc.clearKeywords();

				Set<String> keywords = ddao.toKeywords(getKeywords());
				boolean stored = ddao.store(doc);
				doc.setKeywords(keywords);
				stored = ddao.store(doc);

				/* create history entry */
				String username = SessionManagement.getUsername();
				History history = new History();
				history.setDocId(doc.getDocId());
				history.setDate(DateBean.toCompactString());
				history.setUsername(username);
				history.setEvent(History.CHANGED);
				historyDAO.store(history);

				/* create messages */
				if (!stored) {
					Messages.addLocalizedError("errors.action.changedoc");
				} else {
					Messages.addLocalizedInfo("msg.action.changedoc");
				}

				// Launch document re-indexing
				reindexDocument(doc, oldLang);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addError(e.getMessage());
			} finally {
				reset();
			}

			navigation.setSelectedPanel(new PageContentBean("documents"));
			navigation.refresh();

			return null;
		} else {
			return "login";
		}
	}

	/**
	 * Executes a document's checkin creating a new version
	 */
	public String checkin() {
		Application application = FacesContext.getCurrentInstance().getApplication();
		InputFileBean fileForm = ((InputFileBean) application.createValueBinding("#{inputFile}").getValue(
				FacesContext.getCurrentInstance()));

		if (SessionManagement.isValid()) {
			String username = SessionManagement.getUsername();
			String versionDesc = fileForm.getDescription();
			Document document = record.getDocument();
			File file = fileForm.getFile();

			if (document.getDocStatus() == Document.DOC_CHECKED_OUT) {
				if (file != null) {
					// check that we have a valid file for storing as new
					// version
					String fileName = fileForm.getFileName();
					String type = fileForm.getVersionType();

					// determines the kind of version to create
					Version.VERSION_TYPE versionType;

					if (type.equals("release")) {
						versionType = Version.VERSION_TYPE.NEW_RELEASE;
					} else if (type.equals("subversion")) {
						versionType = Version.VERSION_TYPE.NEW_SUBVERSION;
					} else {
						versionType = Version.VERSION_TYPE.OLD_VERSION;
					}

					try {
						// checkin the document; throws an exception if
						// something goes wrong
						CheckinDocUtil.checkinDocument(document.getDocId(), new FileInputStream(file), fileName,
								username, versionType, versionDesc);

						/* create positive log message */
						Messages.addLocalizedInfo("msg.action.savedoc");
						fileForm.reset();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						Messages.addLocalizedError("errors.action.savedoc");
					}
				} else {
					Messages.addLocalizedError("errors.nofile");
				}
			}
			reset();
		} else {
			return "login";
		}

		DocumentNavigation documentNavigation = ((DocumentNavigation) application.createValueBinding(
				"#{documentNavigation}").getValue(FacesContext.getCurrentInstance()));
		documentNavigation.setSelectedPanel(new PageContentBean("documents"));

		return null;
	}

	/**
	 * Assigns the correct group associations on the passed menu
	 */
	private void updateGroups(Menu menu) {
		String[] docGroup = getMenuGroup();
		menu.setMenuGroup(docGroup);
	}

	private void reindexDocument(Document doc, String originalLanguage) throws Exception {
		/* get search index entry */
		String lang = doc.getLanguage();

		// Extract the content from the file
		String content = getDocumentContent(doc);

		Indexer indexer = (Indexer) Context.getInstance().getBean(Indexer.class);

		// Remove the document from the index
		indexer.deleteFile(String.valueOf(doc.getMenuId()), originalLanguage);

		// Add the document to the index (lucene 2.0 doesn't support the update
		// operation)
		File file = getDocumentFile(doc);
		int luceneId = indexer.addFile(file, doc, content, lang);

		// Update the reference to the luceneId
		SearchDocumentDAO searchDocDao = (SearchDocumentDAO) Context.getInstance().getBean(SearchDocumentDAO.class);
		searchDocDao.deleteByMenuId(doc.getMenuId());

		SearchDocument searchDoc = new SearchDocument();
		searchDoc.setLuceneId(luceneId);
		searchDoc.setMenuId(doc.getMenuId());

		Language cLanguage = LanguageManager.getInstance().getLanguage(lang);
		if (cLanguage == null)
			cLanguage = LanguageManager.getInstance().getDefaultLanguage();
		
		searchDoc.setIndex(cLanguage.getIndex());

		// Store the search document into the index
		searchDocDao.store(searchDoc);
	}

	private File getDocumentFile(Document doc) {
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		Menu menu = menuDao.findByPrimaryKey(doc.getMenuId());
		String path = conf.getValue("docdir") + "/";
		path += (menu.getMenuPath() + "/" + menu.getMenuId());

		String filename = menu.getMenuRef();

		return new File(path, filename);
	}

	private String getDocumentContent(Document doc) {
		String content = null;

		File file = getDocumentFile(doc);

		// Parses the file where it is already stored
		Parser parser = ParserFactory.getParser(file);

		// and gets some fields
		if (parser != null) {
			content = parser.getContent();
		}

		if (content == null) {
			content = "";
		}

		return content;
	}

	public DocumentRecord getRecord() {
		return record;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setDocumentNavigation(DocumentNavigation documentNavigation) {
		this.documentNavigation = documentNavigation;
	}
}
