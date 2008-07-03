package org.contineo.core.communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.communication.dao.EMailAccountDAO;
import org.contineo.core.communication.dao.EMailDAO;
import org.contineo.core.document.Document;
import org.contineo.core.document.History;
import org.contineo.core.document.Version;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.HistoryDAO;
import org.contineo.core.doxter.Storer;
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
import org.contineo.util.config.SettingsConfig;

/**
 * This component downloads new emails from one or more e-mail accounts
 * 
 * @author Michael Scholz, Marco Meschieri
 */
public class EMailReceiver {
	protected static Log log = LogFactory.getLog(EMailReceiver.class);

	// The default username that owns downloaded documents
	private String defaultOwner = "admin";

	private EMailAccountDAO accountDao;

	private EMailDAO emailDao;

	private MenuDAO menuDao;

	private SettingsConfig settingsConfig;

	private Storer storer;

	private DocumentDAO documentDao;

	private HistoryDAO historyDao;

	private SearchDocumentDAO searchDocDao;

	private Indexer indexer;

	private EMailReceiver() {
	}

	public Indexer getIndexer() {
		return indexer;
	}

	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public SearchDocumentDAO getSearchDocDao() {
		return searchDocDao;
	}

	public void setSearchDocDao(SearchDocumentDAO searchDocDao) {
		this.searchDocDao = searchDocDao;
	}

	public DocumentDAO getDocumentDao() {
		return documentDao;
	}

	public void setDocumentDao(DocumentDAO documentDao) {
		this.documentDao = documentDao;
	}

	public HistoryDAO getHistoryDao() {
		return historyDao;
	}

	public void setHistoryDao(HistoryDAO historyDAO) {
		this.historyDao = historyDAO;
	}

	public MenuDAO getMenuDao() {
		return menuDao;
	}

	public void setMenuDao(MenuDAO menuDao) {
		this.menuDao = menuDao;
	}

	public Storer getStorer() {
		return storer;
	}

	public void setStorer(Storer storer) {
		this.storer = storer;
	}

	public EMailDAO getEmailDao() {
		return emailDao;
	}

	public void setEmailDao(EMailDAO emailDao) {
		this.emailDao = emailDao;
	}

	public EMailAccountDAO getAccountDao() {
		return accountDao;
	}

	public void setAccountDao(EMailAccountDAO accountDao) {
		this.accountDao = accountDao;
	}

	public String getDefaultOwner() {
		return defaultOwner;
	}

	public void setDefaultOwner(String defaultOwner) {
		this.defaultOwner = defaultOwner;
	}

	public SettingsConfig getSettingsConfig() {
		return settingsConfig;
	}

	public void setSettingsConfig(SettingsConfig settingsConfig) {
		this.settingsConfig = settingsConfig;
	}

	/**
	 * Downloads all new mails from all accounts. The stored document will be
	 * owned by the specified default owner
	 * 
	 * @throws Exception
	 */
	public synchronized void receiveMails() {
		receiveMails(defaultOwner);
	}

	/**
	 * Downloads all new mails from all accounts. The stored document will be
	 * owned by the specified username
	 * 
	 * @throws Exception
	 */
	public synchronized void receiveMails(String username) {
		log.info("Receive all mails");

		Collection<EMailAccount> accounts = accountDao.findAll();

		for (EMailAccount account : accounts) {
			if (account.getEnabled() == 0) {
				log.warn("Skip account " + account.getMailAddress() + " because disabled");
				continue;
			}
			try {
				receive(account, defaultOwner);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public synchronized void receive(EMailAccount account, String username) throws MessagingException {
		// Connect to POP3-Server
		Session sess = Session.getDefaultInstance(new Properties());
		Store store = sess.getStore(account.getProvider());
		store.connect(account.getHost(), account.getAccountUser(), account.getAccountPassword());

		// Open Folder INBOX
		Folder inbox = store.getFolder("INBOX");
		if (inbox != null) {
			inbox.open(Folder.READ_WRITE);

			// fetch messages from server
			javax.mail.Message[] messages = inbox.getMessages();

			for (int i = 0; i < messages.length; i++) {
				EMail email = new EMail();
				try {
					javax.mail.Message message = messages[i];
					message.setFlag(Flags.Flag.DELETED, account.getDeleteFromMailbox() > 0);

					if (message.getHeader("Message-ID") == null)
						continue;

					String mailId = message.getHeader("Message-ID")[0];

					Collection<String> alreadyRetrievedIds = this.emailDao.collectEmailIds(account.getAccountId());
					if (alreadyRetrievedIds.contains(mailId)) {
						if (log.isDebugEnabled())
							log.debug("Skip message " + mailId + " because already fetched from " + account.toString());
						continue;
					}

					InternetAddress from = ((InternetAddress) message.getFrom()[0]);
					Address[] recipients = new Address[] {};
					try {
						recipients = message.getAllRecipients();
					} catch (AddressException e) {
						log.error(e);
					}

					// store message in database
					if (from != null) {
						email.setAuthor(from.getPersonal());
						email.setAuthorAddress(from.getAddress());
					}

					if (recipients != null)
						for (int j = 0; j < recipients.length; j++) {
							Address rec = recipients[j];
							Recipient recipient = new Recipient();
							recipient.setAddress(rec.toString());
							email.addRecipient(recipient);
						}

					email.setSubject(message.getSubject());
					email.setRead(0);
					email.setUserName(username);
					email.setFolder("inbox");
					if (message.getSentDate() != null)
						email.setSentDate(String.valueOf(message.getSentDate().getTime()));
					else
						email.setSentDate(String.valueOf(new Date().getTime()));
					email.setEmailId(mailId);
					email.setAccountId(account.getAccountId());
					getEmailDao().store(email);

					if (log.isDebugEnabled())
						log.debug("Store email " + email.getSubject());
					// Cleanup the mails directory
					File mailsdir = new File(settingsConfig.getValue("userdir") + "/mails/");
					if (mailsdir.exists())
						FileUtils.forceDelete(mailsdir);
					FileUtils.forceMkdir(mailsdir);
					dumpPart(message, 0, account, email, null);
				} catch (Throwable e) {
					log.error("Error on email " + email.getSubject(), e);
				}
			}
			inbox.close(true);
		}

		store.close();
	}

	private Menu dumpPart(Part p, int partCount, EMailAccount account, EMail email, Menu parent)
			throws MessagingException, Exception {
		String mailsdir = settingsConfig.getValue("userdir") + "/mails/";
		File mailDir = new File(FilenameUtils.normalize(mailsdir + "/" + email.getMessageId()));
		FileUtils.forceMkdir(mailDir);

		if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			int count = mp.getCount();

			int partId = 0;
			boolean textBodyFound = false;

			Menu mailMenu = null;

			// Search for text mail body
			for (int i = 0; i < count; i++) {
				Part part = mp.getBodyPart(i);
				if (StringUtils.isEmpty(part.getFileName()) && part.getContentType().startsWith("text/plain")) {
					mailMenu = dumpPart(mp.getBodyPart(i), partId++, account, email, null);
					textBodyFound = true;
				}
			}

			// Search for html mail body
			for (int i = 0; i < count && !textBodyFound; i++) {
				Part part = mp.getBodyPart(i);
				if (StringUtils.isEmpty(part.getFileName()) && part.getContentType().startsWith("text/html")
						&& !textBodyFound) {
					// This is an HTML-only mail
					mailMenu = dumpPart(mp.getBodyPart(i), partId++, account, email, null);
				}
			}

			// Dump other parts skipping not-allowed extensions
			for (int i = 0; i < count; i++) {
				Part part = mp.getBodyPart(i);
				if (!StringUtils.isEmpty(part.getFileName())
						&& account.isAllowed(FilenameUtils.getExtension(part.getFileName()))) {
					dumpPart(mp.getBodyPart(i), partId++, account, email, mailMenu);
				}
			}
		} else {
			Attachment attachment = new Attachment();
			String cType = p.getContentType();
			String filename = p.getFileName();
			String docName = filename;

			// Check if this is the email body or an attachment
			if (StringUtils.isEmpty(filename)) {
				filename = "email";

				if (cType.startsWith("text/plain")) {
					filename += ".mail";
				}

				if (cType.startsWith("text/html")) {
					filename += ".html";
				}
				docName = StringUtils.abbreviate(email.getSubject(), 100);
			}

			int end = cType.indexOf(";");
			String mimeType = "";

			if (end != -1) {
				mimeType = cType.substring(0, cType.indexOf(";"));
			} else {
				mimeType = cType;
			}

			InputStream is = p.getInputStream();
			File file = new File(mailDir, filename);
			FileOutputStream fos = new FileOutputStream(file);
			int letter = 0;

			while ((letter = is.read()) != -1) {
				fos.write(letter);
			}

			is.close();
			fos.close();

			String icon = "";
			// zml edit 分离出常用的文件格式及对应图片
			if (mimeType.equals("text/rtf") || mimeType.equals("application/msword")
					|| mimeType.equals("application/vnd.sun.xml.writer")) {
				icon = "textdoc.gif";
			} else if (mimeType.equals("application/msexcel") || mimeType.equals("application/vnd.sun.xml.calc")) {
				icon = "tabledoc.gif";
			} else if (mimeType.equals("application/mspowerpoint")
					|| mimeType.equals("application/vnd.sun.xml.impress")) {
				icon = "presentdoc.gif";
			} else if (mimeType.equals("application/pdf")) {
				icon = "pdf.gif";
			} else if (mimeType.equals("text/plain")) {
				icon = "txt.gif";
			} else if (mimeType.equals("application/doc")) {
				icon = "doc.gif";
			} else if (mimeType.equals("application/xls")) {
				icon = "xls.gif";
			} else if (mimeType.equals("application/ppt")) {
				icon = "ppt.gif";
			} else if (mimeType.equals("text/html")) {
				icon = "internet.gif";
			} else {
				icon += "document.gif";
			}

			attachment.setIcon(icon);
			attachment.setMimeType(mimeType);
			attachment.setFilename(filename);
			email.addAttachment(partCount, attachment);

			Menu parentMenu = parent;
			if (parentMenu == null)
				parentMenu = account.getTargetFolder();

			return storeDocument(account, parentMenu, file, docName, email.getSentDateAsDate());
		}
		return null;
	}

	/**
	 * Stores a document file in the archive
	 * 
	 * @param account
	 * @param file File to be stored
	 * @param folder The folder in which the document must be created, if null
	 *        account target folder is used
	 * @param docName Name of the document to be created
	 * @param srcDate
	 * @return The newly created menu
	 * @throws Exception
	 */
	private Menu storeDocument(EMailAccount account, Menu folder, File file, String docName, Date srcDate)
			throws Exception {
		log.info("Store email document " + file);

		// Gets file name
		String filename = file.getName();
		String ext = filename.substring(filename.lastIndexOf(".") + 1);

		Menu parent = account.getTargetFolder();
		if (folder != null)
			parent = folder;

		// Makes menuPath
		String menupath = new StringBuilder(parent.getMenuPath()).append("/").append(parent.getMenuId()).toString();
		int menuhier = parent.getMenuHier();

		// Makes new Menu
		Menu menu = new Menu();
		menu.setMenuParent(parent.getMenuId());
		menu.setMenuPath(menupath);
		menu.setMenuHier(menuhier++);
		menu.setMenuText(docName);
		menu.setMenuType(Menu.MENUTYPE_FILE);
		menu.setMenuRef(filename);

		String icon = IconSelector.selectIcon(ext);
		menu.setMenuIcon(icon.toString());

		// Set permissions from parent folder
		menu.setMenuGroup(parent.getMenuGroupNames());

		// and stores it
		menuDao.store(menu);

		// Stores the document in the repository
		String path = new StringBuilder(settingsConfig.getValue("docdir")).append(menupath).append("/").append(
				String.valueOf(menu.getMenuId())).append("/").toString();
		String mpath = new StringBuilder(menupath).append("/").append(String.valueOf(menu.getMenuId())).toString();

		// Get file to upload inputStream
		InputStream stream = null;
		stream = new FileInputStream(file);

		// stores it in folder
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
		String name = docName;
		String author = "";
		String sourceDate = "";
		String keywords = "";
		// and gets some fields
		if (parser != null) {
			content = parser.getContent();
			if (docName == null)
				name = parser.getTitle();
			author = parser.getAuthor();
			sourceDate = parser.getSourceDate();
			keywords = parser.getKeywords();
		}
		if (StringUtils.isEmpty(sourceDate))
			sourceDate = DateBean.toCompactString(srcDate);

		if (content == null) {
			content = "";
		}

		String language = account.getLanguage();

		Document doc = new Document();
		doc.setMenu(menu);
		doc.setDocDate(DateBean.toCompactString());
		doc.setDocPublisher("mailer");
		doc.setDocStatus(Document.DOC_CHECKED_IN);
		doc.setDocType(filename.substring(filename.lastIndexOf(".") + 1));
		doc.setDocVersion("1.0");
		doc.setSource(account.getMailAddress());
		doc.setSourceAuthor(author);
		doc.setKeywords(documentDao.toKeywords(keywords));
		doc.setSourceType("mail");
		doc.setCoverage("");
		doc.setLanguage(language);
		doc.setDocName(name);

		sourceDate = DateBean.toCompactString(sourceDate, language);
		if (sourceDate != null)
			doc.setSourceDate(sourceDate);

		/* insert initial version 1.0 */
		Version vers = new Version();
		vers.setVersion("1.0");
		vers.setVersionComment("");
		vers.setVersionDate(DateBean.toCompactString());

		vers.setVersionUser(account.getUserName());
		doc.addVersion(vers);

		documentDao.store(doc);

		/* create history entry */
		History history = new History();
		history.setDocId(doc.getDocId());
		history.setDate(DateBean.toCompactString());
		history.setUsername(account.getUserName());
		history.setEvent(History.STORED);

		historyDao.store(history);

		menupath = menu.getMenuPath();

		/* create search indexer entry */
		int luceneId = indexer.addFile(new File(new StringBuilder(path).append("/").append(filename).toString()), doc,
				content, language);
		SearchDocument searchDoc = new SearchDocument();
		searchDoc.setLuceneId(luceneId);
		searchDoc.setMenuId(menu.getMenuId());

	
		Language cLanguage = LanguageManager.getInstance().getLanguage(language);
		if (cLanguage == null)
			cLanguage = LanguageManager.getInstance().getDefaultLanguage();
		
		searchDoc.setIndex(cLanguage.getIndex());

		searchDocDao.store(searchDoc);

		// Update the file size
		menuDao.store(menu);

		return menu;
	}
}