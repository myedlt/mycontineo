package org.contineo.core.security;

import java.security.AccessControlException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.document.Document;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.doxter.Storer;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.security.dao.UserDocDAO;
import org.contineo.util.config.SettingsConfig;

/**
 * Basic menu manager implementation
 * 
 * @author Marco Meschieri
 * @version $Id: MenuManagerImpl.java,v 1.1 2007/07/10 16:36:26 marco Exp $
 * @since 3.0
 */
public class MenuManagerImpl implements MenuManager {
    protected static Log log = LogFactory.getLog(MenuManagerImpl.class);

    private MenuDAO menuDao;

    private SettingsConfig settings;

    private DocumentDAO documentDao;

    private TermDAO termDao;

    private Indexer indexer;

    private Storer storer;

    private SearchDocumentDAO searchDocDao;

    private UserDocDAO userDocDao;

    /**
     * @see org.contineo.core.security.MenuManager#deleteMenu(org.contineo.core.security.Menu,
     *      java.lang.String)
     */
    public void deleteMenu(Menu menu, String userName) throws Exception {
        log.debug("User " + userName + " required the deletion of item " + menu.getMenuId());

        try {
            boolean sqlop = true;
            int id = menu.getMenuId();

            if (menuDao.isWriteEnable(id, userName)) {
                int type = menu.getMenuType();

                if (type == Menu.MENUTYPE_FILE) {
                    deleteFile(menu, id, userName);
                }

                // remove sub-elements
                Collection children = menuDao.findByParentId(id);
                Iterator childIter = children.iterator();
                while (childIter.hasNext()) {
                    Menu m = (Menu) childIter.next();
                    deleteFile(m, m.getMenuId(), userName);
                }

                boolean deleted = menuDao.delete(id);
                if (!deleted) {
                    sqlop = false;
                }

                //int parentId = menu.getMenuParent();

                //Collection<ExtMenu> coll2 = new ArrayList<ExtMenu>();
                //Collection coll = menuDao.findByUserName(userName, parentId);
                //Iterator iter = coll.iterator();
                //String docpath = settings.getValue("docdir");

               // while (iter.hasNext()) {
                    //Menu m = (Menu) iter.next();
                    //ExtMenu xmenu = new ExtMenu(m);

                    // calculate size of menu
//                    int size = 0;
//
//                    if (m.getMenuType() == Menu.MENUTYPE_FILE) {
//                        long sz = FileBean.getSize(docpath + "/" + m.getMenuPath() + "/" + m.getMenuId() + "/"
//                                + m.getMenuRef());
//                        sz = sz / 1024;
//                        size = (int) sz;
//                    } else {
//                        size = menuDao.findByUserName(userName, m.getMenuId()).size();
//                    }
//
//                    xmenu.setSize(size);

                    // check if menu is writable
//                    boolean writable = false;
//
//                    if (menuDao.isWriteEnable(m.getMenuId(), userName)) {
//                        writable = true;
//                    } else {
//                        writable = false;
//                    }
//
//                    xmenu.setWritable(writable);

                    // only done on documents
                    // set the checkout/checkin status of the document
                    // set the checkout user, if the document is checked out
//                    if (m.getMenuType() == Menu.MENUTYPE_FILE) {
//                        Document doc = documentDao.findByMenuId(m.getMenuId());
//
//                        if (doc != null) {
//                            xmenu.setDocStatus(doc.getDocStatus());
//                            xmenu.setCheckoutUser(doc.getCheckoutUser());
//                        }
//                    }

                    //coll2.add(xmenu);
                //}

                if (!sqlop) {
                    String message = "An error has occurred while deleting the item";
                    log.error(message);
                    throw new SQLException(message);
                } else {
                    log.info("The item has been deleted");
                }
            } else {
                String message = "User not allowed to delete item";
                log.error(message);
                throw new AccessControlException(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    private boolean deleteFile(Menu menu, int id, String username) {
        boolean sqlop = true;

        try {
            searchDocDao.deleteByMenuId(id);

            userDocDao.delete(username, id);

            Document doc = documentDao.findByMenuId(id);

            if (doc != null) {
                indexer.deleteFile(String.valueOf(id), doc.getLanguage());
            }

            boolean deleted2 = termDao.delete(id);

            boolean deleted1 = documentDao.deleteByMenuId(id);

            if (!deleted1 || !deleted2) {
                sqlop = false;
            }

            boolean deleted = menuDao.delete(id);

            if (!deleted) {
                sqlop = false;
            }

            // String path = conf.getValue("docdir");
            String menupath = menu.getMenuPath() + "/" + String.valueOf(id);

            // FileBean.deleteDir(path);
            storer.delete(menupath);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sqlop = false;
        }

        return sqlop;
    }

    public void setDocumentDao(DocumentDAO documentDao) {
        this.documentDao = documentDao;
    }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public void setMenuDao(MenuDAO menuDao) {
        this.menuDao = menuDao;
    }

    public void setSearchDocDao(SearchDocumentDAO searchDocDao) {
        this.searchDocDao = searchDocDao;
    }

    public void setSettings(SettingsConfig settings) {
        this.settings = settings;
    }

    public void setStorer(Storer storer) {
        this.storer = storer;
    }

    public void setTermDao(TermDAO termDao) {
        this.termDao = termDao;
    }

    public void setUserDocDao(UserDocDAO userDocDao) {
        this.userDocDao = userDocDao;
    }

}
