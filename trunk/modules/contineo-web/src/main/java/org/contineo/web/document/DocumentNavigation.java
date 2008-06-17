package org.contineo.web.document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.searchengine.comparision.SearchResult;
import org.contineo.core.searchengine.search.Result;
import org.contineo.core.security.ExtMenu;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;
import org.contineo.web.navigation.NavigationBean;
import org.contineo.web.navigation.PageContentBean;

import com.icesoft.faces.component.tree.IceUserObject;
import com.icesoft.faces.component.tree.Tree;

/**
 * <p>
 * The TreeNavigation class is the backing bean for the documents navigation
 * tree on the left hand side of the application. Each node in the tree is made
 * up of a PageContent which is responsible for the navigation action when a
 * tree node is selected.
 * </p>
 * <p>
 * When the Tree component binding takes place the tree nodes are initialised
 * and the tree is built. Any addition to the tree navigation must be made to
 * this class.
 * </p>
 * <p>
 * This bean also controls which panel is shown on the right side of the
 * documents view
 * </p>
 * 
 * @author Marco Meschieri
 * @version $Id: DocumentNavigation.java,v 1.14 2006/08/29 16:33:46 marco Exp $
 * @since 3.0
 */
public class DocumentNavigation extends NavigationBean {
	protected static Log log = LogFactory.getLog(DocumentNavigation.class);

	// binding to component
	private Tree treeComponent;

	// bound to components value attribute
	private DirectoryTreeModel model;

	/**
	 * Default constructor of the tree. The root node of the tree is created at
	 * this point.
	 */
	public DocumentNavigation() {
		model = new DirectoryTreeModel();
	}

	/**
	 * Gets the default tree model. This model is needed for the value attribute
	 * of the tree component.
	 * 
	 * @return default tree model used by the navigation tree
	 */
	public DefaultTreeModel getModel() {
		return model;
	}

	/**
	 * Gets the tree component binding.
	 * 
	 * @return tree component binding
	 */
	public Tree getTreeComponent() {
		return treeComponent;
	}

	/**
	 * Sets the tree component binding.
	 * 
	 * @param treeComponent
	 *            tree component to bind to
	 */
	public void setTreeComponent(Tree treeComponent) {
		this.treeComponent = treeComponent;
	}

	public Directory getSelectedDir() {
		return model.getSelectedDir();
	}

	public List<Directory> getBreadcrumb() {
		
		List<Directory> breadcrumb = new ArrayList<Directory>();

		try {
			if (getSelectedDir() != null) {
				Directory currentDir = getSelectedDir();
				while(currentDir != null) {
					breadcrumb.add(currentDir);
					currentDir = getDirectory(currentDir.getMenu().getMenuParent());
				}
			}
		} catch (RuntimeException e) {
			log.error("getBreadcrumb() Eccezione: " + e.getMessage(), e);
		}

		// giro la lista ottenuta
		Collections.reverse(breadcrumb);

		return breadcrumb;
	}

	public void refresh() {
		selectDirectory(getSelectedDir());
		model.reload(model.getSelectedNode());
	}

    public void onSelectDirectory(ActionEvent event) {
        int directoryId = Integer.parseInt((String) FacesContext.getCurrentInstance()
                                                                .getExternalContext()
                                                                .getRequestParameterMap()
                                                                .get("directoryId"));
        Directory directory = model.getDirectory(directoryId);
        selectDirectory(directory);
    }
	
	
	/**
	 * Changes the currently selected directory and updates the documents list.
	 * 
	 * @param directoryId
	 */
	public void selectDirectory(Directory directory) {

		model.selectDirectory(directory);
		model.reload(model.getSelectedNode());
		
		// Notify the records manager
		Application application = FacesContext.getCurrentInstance().getApplication();
		
		DocumentsRecordsManager recordsManager = ((DocumentsRecordsManager) application.createValueBinding(
				"#{documentsRecordsManager}").getValue(FacesContext.getCurrentInstance()));
		recordsManager.selectDirectory(directory.getMenu().getMenuId());
		
		setSelectedPanel(new PageContentBean("documents"));
	}

	/**
	 * Opens the directory containing the selected search entry
	 */
	public String openInFolder() {
		//model.reloadAll();

		Map map = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();

		Object entry = (Object) map.get("entry");

		int menuId = Menu.MENUID_DOCUMENTS;

		if (entry instanceof Result) {
			menuId = ((Result) entry).getMenuId();
		} else if (entry instanceof SearchResult) {
			menuId = ((SearchResult) entry).getMenuId();
		} else if (entry instanceof ExtMenu) {
			menuId = ((ExtMenu) entry).getMenuId();
		} else if (entry instanceof DocumentRecord) {
			menuId = ((DocumentRecord) entry).getMenuId();
		}

		model.openFolder(menuId);
		
		// Get the parent directory
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu menu = menuDao.findByPrimaryKey(menuId);
		
		// Patch for nested documents
		Menu menuParent = menuDao.findByPrimaryKey(menu.getMenuParent());
		
		int directoryId = menu.getMenuParent();
		if (menuParent != null && menuParent.getMenuType() == Menu.MENUTYPE_FILE) {
			directoryId = menuParent.getMenuParent();
		}
		
		Directory dir = model.getDirectory(directoryId);
		selectDirectory(dir);
		
		highlightsDocument(menu.getMenuId());
		setSelectedPanel(new PageContentBean("documents"));

		// Show the documents browsing panel
		Application application = FacesContext.getCurrentInstance().getApplication();
		NavigationBean navigation = ((NavigationBean) application.createValueBinding("#{navigation}").getValue(
				FacesContext.getCurrentInstance()));
		Menu documentsMenu = menuDao.findByPrimaryKey(Menu.MENUID_DOCUMENTS);
		
		PageContentBean panel = new PageContentBean("m-" + documentsMenu.getMenuId(), "document/browse");
		panel.setContentTitle(Messages.getMessage(documentsMenu.getMenuText()));
		navigation.setSelectedPanel(panel);

		return null;
	}

	private void highlightsDocument(int menuId) {
		// Notify the records manager
		Application application = FacesContext.getCurrentInstance().getApplication();
		DocumentsRecordsManager recordsManager = ((DocumentsRecordsManager) application.createValueBinding(
				"#{documentsRecordsManager}").getValue(FacesContext.getCurrentInstance()));
		recordsManager.selectHighlightedDocument(menuId);
	}

	/**
	 * Retrieves the directory with the specified identifier
	 * 
	 * @param direcoryId
	 *            The directory identifier
	 * @return The found directory, null if not found
	 */
	public Directory getDirectory(int direcoryId) {
		return model.getDirectory(direcoryId);
	}

	public String delete() {
		model.deleteDirectory(model.getSelectedDir());

		// Notify the records manager
		Application application = FacesContext.getCurrentInstance().getApplication();
		DocumentsRecordsManager recordsManager = ((DocumentsRecordsManager) application.createValueBinding(
				"#{documentsRecordsManager}").getValue(FacesContext.getCurrentInstance()));
		recordsManager.selectDirectory(model.getSelectedDir().getMenuId());
		setSelectedPanel(new PageContentBean("documents"));

		return null;
	}

	public String edit() {
		setSelectedPanel(new PageContentBean("updateDir"));

		Application application = FacesContext.getCurrentInstance().getApplication();
		DirectoryEditForm form = ((DirectoryEditForm) application.createValueBinding("#{directoryForm}").getValue(
				FacesContext.getCurrentInstance()));
		form.setDirectory(model.getSelectedDir());
		
		return null;
	}

	public String rights() {
		setSelectedPanel(new PageContentBean("rights"));

		Application application = FacesContext.getCurrentInstance().getApplication();
		RightsRecordsManager form = (RightsRecordsManager) application.createValueBinding("#{rightsRecordsManager}")
				.getValue(FacesContext.getCurrentInstance());
		form.selectDirectory(model.getSelectedDir());

		return null;
	}

	public String newDirectory() {
		setSelectedPanel(new PageContentBean("newDir"));

		Application application = FacesContext.getCurrentInstance().getApplication();
		DirectoryEditForm form = (DirectoryEditForm) application.createValueBinding("#{directoryForm}").getValue(
				FacesContext.getCurrentInstance());
		
		String[] groups = SessionManagement.getUser().getGroupNames();
		// Add the admin group if not specified
		boolean found = false;
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].equals("admin"))
				found = true;
		}
		if(!found){
			String[] tmp=new String[groups.length+1];
			for (int i = 0; i < groups.length; i++)
				tmp[i]=groups[i];
			tmp[groups.length]="admin";
			groups=tmp;
		}

		form.setMenuGroup(groups);
		form.setFolderName("");
		return null;
	}

	public void addNewDir(Menu dir, Directory parent) {
		model.addNewDir(dir, parent);
	}

	public void nodeClicked(ActionEvent event) {
		Tree tree = (Tree) event.getSource();
		DefaultMutableTreeNode node = tree.getNavigatedNode();
		IceUserObject userObject = (IceUserObject) node.getUserObject();

		if (userObject.isExpanded()) {
			model.reload(node);
		}
	}
}
