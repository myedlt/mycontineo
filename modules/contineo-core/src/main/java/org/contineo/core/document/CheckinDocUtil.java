package org.contineo.core.document;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.contineo.core.FileBean;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.HistoryDAO;
import org.contineo.core.doxter.Storer;
import org.contineo.core.i18n.DateBean;
import org.contineo.core.searchengine.SearchDocument;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.security.MenuGroup;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.core.util.IconSelector;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;

/**
 * utility class to checkin/create a document
 * 
 * @author Sebastian Stein
 */
public class CheckinDocUtil {
    /**
     * checks in the given document
     * 
     * @param docId the document to be checked in
     * @param fileInputStream input stream pointing to the new document version
     * @param filename new filename (can also be the old one)
     * @param username user uploading the new document version
     * @param versionType specifies if this is a new release, a subversion or
     *            the old version
     * @param versionDesc a change description
     * @throws Exception if an error occurs, this exception is thrown
     */
    public static void checkinDocument(int docId, InputStream fileInputStream, String filename, String username,
            Version.VERSION_TYPE versionType, String versionDesc) throws Exception {
        // identify the document and menu
        DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
        Document document = docDao.findByPrimaryKey(docId);
        int menuId = document.getMenuId();
        MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
        Menu menu = menuDao.findByPrimaryKey(menuId);

        // create some strings containing paths
        String menuPath = menu.getMenuPath() + "/" + String.valueOf(menuId);
        SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
        String completeDocPath = settings.getValue("docdir") + menuPath + "/";

        // rename the old current version file to the version name: "quelle.txt"
        // -> "2.0"
        if (!document.getDocType().equals("zip") || !document.getDocType().equals("jar")) {
            FileBean.renameFile(completeDocPath + menu.getMenuRef(), completeDocPath + document.getDocVersion());
        }

        // extract file extension of the new file and select a file icon based
        // on the extension
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        menu.setMenuRef(filename);
        String icon = IconSelector.selectIcon(extension);
        menu.setMenuIcon(icon);

        // create new version
        Version version = createNewVersion(versionType, username, versionDesc, document.getDocVersion());
        String newVersion = version.getVersion();

        // set other properties of the document
        document.setDocDate(DateBean.toCompactString());
        document.setDocPublisher(username);
        document.setDocStatus(Document.DOC_CHECKED_IN);
        document.setDocType(extension);
        document.setCheckoutUser("");
        document.setMenu(menu);
        document.addVersion(version);
        document.setDocVersion(newVersion);
        DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
        if (ddao.store(document) == false)
            throw new Exception();

        // store the document in the repository (on the file system)
        Storer storer = (Storer) Context.getInstance().getBean(Storer.class);
        storer.store(fileInputStream, menuPath, filename, newVersion);

        // create search index entry
        createIndexEntry(document, menuId, filename, completeDocPath);

        // create history entry for this checkin event
        createHistoryEntry(docId, username, History.CHECKIN);
        
        //Update file size
        menuDao.store(menu);
    }

    /**
     * creates a new version object and fills in the provided attributes
     * 
     * @param versionType either a new release, a new subversion or just the old
     *            version
     * @param username user creating the new version
     * @param description change description
     * @param docId version should belong to this document
     * @param oldVersionName the previous version name
     */
    private static Version createNewVersion(Version.VERSION_TYPE versionType, String username, String description,
            String oldVersionName) {
        Version version = new Version();
        String newVersionName = version.getNewVersionName(oldVersionName, versionType);

        version.setVersion(newVersionName);
        version.setVersionComment(description);
        version.setVersionDate(DateBean.toCompactString());
        version.setVersionUser(username);

        return version;
    }

    /** creates a new search index entry for the given document */
    private static void createIndexEntry(Document document, int menuId, String filename, String path) throws Exception {
        Indexer index = (Indexer) Context.getInstance().getBean(Indexer.class);
        index.deleteFile(String.valueOf(menuId), document.getLanguage());
        index.addDirectory(new File(path + filename), document);
    }

    /** creates history entry saying username has checked in document (id) */
    private static void createHistoryEntry(int docId, String username, String eventType) {
        History history = new History();
        history.setDocId(docId);
        history.setDate(DateBean.toCompactString());
        history.setUsername(username);
        history.setEvent(eventType);
        HistoryDAO historyDao = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);
        historyDao.store(history);
    }

    /**
     * creates a new document in the parent menu
     */
    public static Menu createDocument(File file, Menu parent, String userName, String language) throws Exception {
        // extract content
        Parser parser = ParserFactory.getParser(file);
        String content = null;
        if (parser != null)
            content = parser.getContent();
        if (content == null)
            content = "";

        // store in database
        String filename = file.getName();
        Document doc = new Document();
        Version vers = new Version();
        Menu menu = new Menu();
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        ext = ext.toLowerCase();
        String name = "";
        if (parser != null) {
            if (parser.getTitle().length() == 0)
                name = filename.substring(0, filename.lastIndexOf("."));
            else
                name = parser.getTitle();
        } else {
            name = filename;
        }
        menu.setMenuText(name);
        menu.setMenuParent(parent.getMenuId());

        // select a file icon based on the extension
        String icon = IconSelector.selectIcon(ext);
        menu.setMenuIcon(icon);

        menu.setMenuSort(0);
        menu.setMenuPath(parent.getMenuPath() + "/" + parent.getMenuId());
        menu.setMenuType(Menu.MENUTYPE_FILE);
        menu.setMenuHier(parent.getMenuHier() + 1);
        menu.setMenuRef(filename);
        for (MenuGroup mg : parent.getMenuGroups()) {
            menu.getMenuGroups().add(mg);
        }

        MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
        menuDao.store(menu);

        doc.setMenu(menu);
        doc.setDocName(name);
        doc.setDocDate(DateBean.toCompactString());
        doc.setDocPublisher(userName);
        doc.setDocStatus(Document.DOC_CHECKED_IN);
        doc.setDocType(filename.substring(filename.lastIndexOf(".") + 1));
        doc.setDocVersion("1.0");
        doc.setSource("");
        if (parser != null) {
            doc.setSourceAuthor(parser.getAuthor());
            String srcDate = DateBean.toCompactString(parser.getSourceDate(), language);
            if (srcDate != null)
                doc.setSourceDate(srcDate);
            String keywords = parser.getKeywords();
            if (keywords != null && keywords.length() > 0) {
                DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
                doc.setKeywords(docDao.toKeywords(keywords));
            }
        }
        doc.setSourceType("");
        doc.setCoverage("");
        doc.setLanguage(language);

        /* insert initial version 1.0 */
        vers.setVersion("1.0");
        vers.setVersionComment("");
        vers.setVersionDate(DateBean.toCompactString());
        vers.setVersionUser(userName);
        doc.addVersion(vers);
        DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
        docDao.store(doc);

        // create history entry
        createHistoryEntry(doc.getDocId(), userName, History.STORED);

        // store document in the repository
        SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
        String path = settings.getValue("docdir");
        if (!path.endsWith(File.pathSeparator))
            path += "/";
        path += menu.getMenuPath() + "/" + doc.getMenuId();
        FileBean.createDir(path);
        FileBean.copyFile(file.getAbsolutePath(), path + "/" + filename);

        /* create search index entry */
        String lang = doc.getLanguage();
        Indexer index = (Indexer) Context.getInstance().getBean(Indexer.class);
        int luceneId = index.addFile(new File(path + "/" + filename), doc, content, language);
        SearchDocument searchDoc = new SearchDocument();
        searchDoc.setLuceneId(luceneId);
        searchDoc.setMenuId(menu.getMenuId());

        String luceneIndex = new Locale(lang).getDisplayLanguage(Locale.ENGLISH).toLowerCase();
        if (StringUtils.isEmpty(luceneIndex))
            luceneIndex = "english";

        searchDoc.setIndex(luceneIndex);

        SearchDocumentDAO searchDocDao = (SearchDocumentDAO) Context.getInstance().getBean(SearchDocumentDAO.class);
        searchDocDao.store(searchDoc);

        //Update file size
        menuDao.store(menu);
        
        return menu;
    }

    /**
     * creates a new folder in the parent menu
     */
    public static Menu createFolder(Menu parentMenu, String menuName) {
        Menu menu = new Menu();
        menu.setMenuText(menuName);
        menu.setMenuParent(parentMenu.getMenuId());
        menu.setMenuSort(0);
        menu.setMenuIcon("folder.gif");
        menu.setMenuPath(parentMenu.getMenuPath() + "/" + parentMenu.getMenuId());
        menu.setMenuType(Menu.MENUTYPE_DIRECTORY);
        menu.setMenuHier(parentMenu.getMenuHier() + 1);
        menu.setMenuRef("");
        for (MenuGroup mg : parentMenu.getMenuGroups()) {
            menu.getMenuGroups().add(mg);
        }

        MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
        if (menuDao.store(menu) == false)
            return null;
        return menu;
    }
}