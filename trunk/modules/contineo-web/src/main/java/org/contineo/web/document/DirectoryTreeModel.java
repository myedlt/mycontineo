package org.contineo.web.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.document.Document;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.doxter.Storer;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.ExtMenu;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.security.dao.UserDocDAO;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.SessionManagement;
import org.contineo.web.StyleBean;
import org.contineo.web.i18n.Messages;

import com.icesoft.faces.component.tree.IceUserObject;

/**
 * A tree model specialized for Contineo's directories
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since ###release###
 */
public class DirectoryTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = 1L;

	protected static Log log = LogFactory.getLog(DirectoryTreeModel.class);

	// Utility map of all directories (key is the menuId)
	private Map<Integer, Directory> directories = new HashMap<Integer, Directory>();

	private Directory selectedDir;

	private DefaultMutableTreeNode selectedNode;

	public DirectoryTreeModel() {
		super(new DefaultMutableTreeNode());
		reload();
	}

	public void reloadAll() {
		init();
		reload((DefaultMutableTreeNode) getRoot(), -1);
	}

	public void reload(DefaultMutableTreeNode node, int depth) {
		Directory dir = ((Directory) node.getUserObject());
		String username = SessionManagement.getUsername();
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);

		try {
			if (username != null) {
				Collection<Menu> menus = menuDao.findByUserName(username, dir.getMenuId());

				for (Menu menu : menus) {
					if (menu.getMenuType() == Menu.MENUTYPE_DIRECTORY) {
						addDir(username, node, menu, depth);
						dir.setLeaf(false);
					}
				}

				dir.setCount(menus.size());
			}
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}

	public void reload(DefaultMutableTreeNode node) {
		reload(node, 1);
	}

	private void init() {
		directories.clear();

		// build root node so that children can be attached
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu rootMenu = menuDao.findByPrimaryKey(Menu.MENUID_DOCUMENTS);
		Directory rootObject = new Directory(rootMenu);
		rootObject.setIcon(StyleBean.XP_BRANCH_CONTRACTED_ICON);
		rootObject.setDisplayText(null);
		rootObject.setContentTitle(null);
		rootObject.setPageContent(true);

		String label = Messages.getMessage(rootMenu.getMenuText());
		rootObject.setDisplayText(label);
		rootObject.setContentTitle(label);

		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootObject);
		rootObject.setWrapper(rootTreeNode);
		setRoot(rootTreeNode);
		rootTreeNode.setUserObject(rootObject);
		directories.put(rootMenu.getMenuId(), rootObject);
	}

	public void reload() {
		init();
		reload((DefaultMutableTreeNode) getRoot());
	}

	public Map<Integer, Directory> getDirectories() {
		return directories;
	}

	public Directory getDirectory(int id) {
		return directories.get(id);
	}

	/**
	 * Adds a new directory in the specified place
	 * 
	 * @param dir The new directory's menu
	 * @param parent The parent directory
	 */
	public void addNewDir(Menu dir, Directory parent) {
		Directory parentDir = parent;

		if (parentDir == null) {
			parentDir = selectedDir;
		}

		parentDir.setCount(parentDir.getCount() + 1);
		parentDir.setLeaf(false);

		DefaultMutableTreeNode parentNode = findDirectoryNode(parentDir.getMenuId(), (DefaultMutableTreeNode) getRoot());
		addDir(SessionManagement.getUsername(), parentNode, dir);
	}

	private void addDir(String username, DefaultMutableTreeNode parentNode, Menu dir) {
		addDir(SessionManagement.getUsername(), parentNode, dir, -1);
	}

	/**
	 * Changes the currently selected directory and updates the documents list.
	 * 
	 * @param directoryId
	 */
	public void selectDirectory(Directory directory) {
		selectedDir = directory;
		selectedDir.setSelected(true);
		setTreeSelectedState((DefaultMutableTreeNode) getRoot());
		expandNodePath(selectedNode);
	}

	/**
	 * Finds the directory node with the specified identifier contained in a
	 * sublevel of the the passed tree node
	 * 
	 * @param direcoryId The directory identifier
	 * @param parent The node in which the directory must be searched
	 * @return The found tree node, null if not found
	 */
	private DefaultMutableTreeNode findDirectoryNode(int direcoryId, DefaultMutableTreeNode parent) {
		Directory dir = (Directory) parent.getUserObject();

		if (dir.getMenu().getMenuId() == direcoryId) {
			return parent;
		} else {
			Enumeration<DefaultMutableTreeNode> enumer = (Enumeration<DefaultMutableTreeNode>) parent.children();

			while (enumer.hasMoreElements()) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumer.nextElement();
				DefaultMutableTreeNode node = findDirectoryNode(direcoryId, childNode);

				if (node != null) {
					return node;
				}
			}
		}

		return null;
	}

	/**
	 * Adds a directory and all it's childs
	 * 
	 * @param username
	 * @param parent
	 * @param dir
	 * @param depth the maximum depth
	 * @return
	 */
	private DefaultMutableTreeNode addDir(String username, DefaultMutableTreeNode parent, Menu dir, int depth) {
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);

		Directory cachedDir = directories.get(dir.getMenuId());

		if ((cachedDir != null) && cachedDir.isLoaded()) {
			DefaultMutableTreeNode node = findDirectoryNode(dir.getMenuId(), parent);

			if (node != null) {
				return node;
			}
		}

		// Component menu item
		Directory branchObject = new Directory(dir);
		branchObject.setDisplayText(dir.getMenuText());
		branchObject.setContentTitle(dir.getMenuText());
		branchObject.setIcon(StyleBean.XP_BRANCH_CONTRACTED_ICON);

		DefaultMutableTreeNode branchNode = new DefaultMutableTreeNode(branchObject);
		branchObject.setWrapper(branchNode);
		branchObject.setPageContent(false);
		branchObject.setLeaf(true);
		branchObject.setExpanded(false);
		branchNode.setUserObject(branchObject);

		// Iterate over subdirs
		Collection<Menu> children = menuDao.findByUserName(username, dir.getMenuId());

		for (Menu child : children) {
			if (child.getMenuType() == Menu.MENUTYPE_DIRECTORY) {
				branchObject.setLeaf(false);

				if (depth > 0) {
					addDir(username, branchNode, child, depth - 1);
				} else if (depth == -1) {
					addDir(username, branchNode, child, depth);
				}
			}
		}

		if ((depth > 0) || (depth == -1)) {
			branchObject.setLoaded(true);
			directories.put(dir.getMenuId(), branchObject);
			parent.add(branchNode);
			branchObject.setCount(children.size());
			log.debug("added dir " + branchObject.getDisplayText());
		}

		return branchNode;
	}

	public void selectDirectory(int id) {
		selectedDir = directories.get(id);
		selectDirectory(selectedDir);
	}

	/**
	 * Set the selection state for all directories in the tree
	 */
	protected void setTreeSelectedState(DefaultMutableTreeNode node) {
		if ((node.getUserObject() != null) && node.getUserObject() instanceof Directory) {
			Directory dir = (Directory) node.getUserObject();

			if ((selectedDir != null) && (dir.getMenu() != null) && dir.getMenu().equals(selectedDir.getMenu())) {
				dir.setSelected(true);
				dir.setIcon(dir.getBranchExpandedIcon());
				selectedNode = node;
			} else {
				dir.setSelected(false);
				dir.setIcon(dir.getBranchContractedIcon());
			}
		}

		Enumeration<DefaultMutableTreeNode> enumer = (Enumeration<DefaultMutableTreeNode>) node.children();

		while (enumer.hasMoreElements()) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumer.nextElement();
			setTreeSelectedState(childNode);
		}
	}

	public Directory getSelectedDir() {
		return selectedDir;
	}

	public DefaultMutableTreeNode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * Deletes a directory form the tree and from the database
	 * 
	 * @param directory The element to be deleted
	 */
	public void deleteDirectory(Directory directory) {
		DefaultMutableTreeNode dirNode = findDirectoryNode(directory.getMenuId(), (DefaultMutableTreeNode) getRoot());

		if (dirNode == null) {
			return;
		}

		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) dirNode.getParent();

		if (SessionManagement.isValid()) {
			int id = directory.getMenuId();
			MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
			String userName = SessionManagement.getUsername();
			int parentId = -1;

			try {
				boolean sqlop = true;

				if (mdao.isWriteEnable(id, userName)) {
					Menu menu = mdao.findByPrimaryKey(id);
					int type = menu.getMenuType();

					if (type == Menu.MENUTYPE_FILE) {
						deleteFile(menu, id, userName);
					}

					// remove sub-elements
					Collection children = mdao.findByParentId(id);
					Iterator childIter = children.iterator();

					while (childIter.hasNext()) {
						Menu m = (Menu) childIter.next();
						deleteFile(m, m.getMenuId(), userName);
					}

					boolean deleted = mdao.delete(id);

					if (!deleted) {
						sqlop = false;
					}

					parentId = menu.getMenuParent();

					Collection<ExtMenu> coll2 = new ArrayList<ExtMenu>();
					Collection coll = mdao.findByUserName(userName, parentId);
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
							long sz = FileBean.getSize(docpath + "/" + m.getMenuPath() + "/" + m.getMenuId() + "/"
									+ m.getMenuRef());
							sz = sz / 1024;
							size = (int) sz;
						} else {
							size = menuDao.findByUserName(userName, m.getMenuId()).size();
						}

						xmenu.setSize(size);

						// check if menu is writable
						boolean writable = false;

						if (menuDao.isWriteEnable(m.getMenuId(), userName)) {
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
						Messages.addLocalizedError("errors.action.deleteitem");
					} else {
						Messages.addLocalizedInfo("msg.action.deleteitem");
					}

					Directory parentDirectory = (Directory) parentNode.getUserObject();
					parentDirectory.setCount(parentDirectory.getCount() - 1);
					parentNode.remove(dirNode);
					selectDirectory(parentDirectory);
				} else {
					Messages.addLocalizedError("document.write.nopermission");
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addError(e.getMessage());
			}
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
	 * Expand all nodes from the passed one back to the root following the
	 * parent/child relation.
	 * 
	 * @param node the leaf node to expand
	 */
	private void expandNodePath(DefaultMutableTreeNode node) {
		IceUserObject obj = (IceUserObject) node.getUserObject();
		obj.setExpanded(true);

		if (!node.equals(getRoot())) {
			expandNodePath((DefaultMutableTreeNode) node.getParent());
		}
	}

	/**
	 * Opens and selects the specified folder. The algorithm is optimised so
	 * that the minimal path is explored and database accesses are reduced.
	 * 
	 * @param menuId The folder menuId
	 */
	public void openFolder(int menuId) {
		// Reset the tree
		init();

		// Now try to construct the minimal portion of the tree, just to show
		// opened folder
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu folderMenu = menuDao.findByPrimaryKey(menuId);
		Collection<Menu> parents = folderMenu.getParents();
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) getRoot();
		reload(parentNode);
		for (Menu folderParent : parents) {
			if (folderParent.getMenuId() == Menu.MENUID_HOME || folderParent.getMenuId() == Menu.MENUID_DOCUMENTS)
				continue;
			Enumeration enumeration = parentNode.children();
			while (enumeration.hasMoreElements()) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) enumeration.nextElement();
				Directory dir = (Directory) child.getUserObject();
				if (folderParent.getMenuId() == dir.getMenuId()) {
					reload(child);
					parentNode = child;
					break;
				}
			}
		}
		expandNodePath(parentNode);
	}
}
