package org.contineo.core.document;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.doxter.Storer;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.security.dao.UserDocDAO;
import org.contineo.util.Context;

/**
 * utility class for deleting menus
 * 
 * @author Sebastian Stein
 */
public class DeleteItemUtil {
	protected static Log log = LogFactory.getLog(DeleteItemUtil.class);

	/**
	 * deletes the given menu and all sub-menus; does not perform an access
	 * check
	 * 
	 * @param menuId menu to be deleted; can be a folder, menu or document
	 * @param userName the user requesting the delete action
	 */
	public static boolean deleteMenu(int menuId, String userName) {
		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu menu = mdao.findByPrimaryKey(menuId);
		if (menu == null)
			return true;
		int type = menu.getMenuType();
		try {
			if (type == Menu.MENUTYPE_FILE) {
				deleteFile(menu, menuId, userName);
			} else if (type == Menu.MENUTYPE_DIRECTORY) {
				Collection children = mdao.findByParentId(menuId);

				Iterator childIter = children.iterator();
				while (childIter.hasNext()) {
					Menu m = (Menu) childIter.next();
					deleteFile(m, m.getMenuId(), userName);
				}
			}
			return mdao.delete(menuId);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * deletes the given menu; does not perform an access check; only used
	 * internally
	 */
	private static void deleteFile(Menu menu, int id, String username) throws Exception {
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
		// int docId = doc.getDocId();
		boolean deleted1 = ddao.deleteByMenuId(id);
		TermDAO termDao = (TermDAO) Context.getInstance().getBean(TermDAO.class);
		boolean deleted2 = termDao.delete(id);
		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		boolean deleted = mdao.delete(id);
		// String path = conf.getValue("docdir");
		String menupath = menu.getMenuPath() + "/" + String.valueOf(id);
		// FileBean.deleteDir(path);
		Storer storer = (Storer) Context.getInstance().getBean(Storer.class);
		if (!storer.delete(menupath) || !deleted || !deleted1 || !deleted2)
			throw new Exception();
	}
}