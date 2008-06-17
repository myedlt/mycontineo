package org.contineo.web.admin;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.communication.EMailAccount;
import org.contineo.core.communication.dao.EMailAccountDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.contineo.web.SessionManagement;
import org.contineo.web.document.DirectoryTreeModel;
import org.contineo.web.i18n.Messages;

import com.icesoft.faces.component.tree.IceUserObject;
import com.icesoft.faces.component.tree.Tree;


/**
 * This is the account editing form
 *
 * @author Marco Meschieri
 * @version $Id:$
 * @since ###release###
 */
public class AccountForm {
    protected static Log log = LogFactory.getLog(AccountForm.class);
    private EMailAccount account;
    private DirectoryTreeModel directoryModel;

    private UIComponent mailAddress;
    private UIComponent accountUser;
    private UIComponent accountPassword;
    private UIComponent provider;
    private UIComponent deleteFromMailbox;
    private UIComponent host;
    private UIComponent port;
    private UIComponent allowedTypes;
    private UIComponent language;
    
    private String password;
    
    
    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	private void clear(){
    		((UIInput)mailAddress).setSubmittedValue(null);
    		((UIInput)accountUser).setSubmittedValue(null);
    		((UIInput)accountPassword).setSubmittedValue(null);
    		((UIInput)provider).setSubmittedValue(null);
    		((UIInput)deleteFromMailbox).setSubmittedValue(null);
    		((UIInput)host).setSubmittedValue(null);
    		((UIInput)port).setSubmittedValue(null);
    		((UIInput)allowedTypes).setSubmittedValue(null);
    		((UIInput)language).setSubmittedValue(null);
    		password=null;
    } 
    
    
    // binding to component
    private Tree treeComponent;
	private boolean deletePassword;

    public EMailAccount getAccount() {
        return account;
    }

    public void setAccount(EMailAccount account) {
        this.account = account;
        getDirectoryModel().reload();

        if (account.getTargetFolderId() != null) {
            directoryModel.reloadAll();
            directoryModel.selectDirectory(account.getTargetFolderId());
            treeComponent.setNavigatedNode(directoryModel.getSelectedNode());
        }
        clear();
        
        this.password=account.getAccountPassword();
    }

    public void nodeClicked(ActionEvent event) {
        Tree tree = (Tree) event.getSource();
        DefaultMutableTreeNode node = tree.getNavigatedNode();
        IceUserObject userObject = (IceUserObject) node.getUserObject();

        if (userObject.isExpanded()) {
            directoryModel.reload(node);
        }
    }

    public DirectoryTreeModel getDirectoryModel() {
        if (directoryModel == null) {
            loadTree();
        }

        return directoryModel;
    }

    public String save() {
        if (SessionManagement.isValid()) {
            try {
                account.setTargetFolderId(directoryModel.getSelectedDir()
                                                        .getMenuId());
                account.setTargetFolder(directoryModel.getSelectedDir().getMenu());

                EMailAccountDAO accountDao = (EMailAccountDAO) Context.getInstance()
                                                                      .getBean(EMailAccountDAO.class);
                MenuDAO menuDao = (MenuDAO) Context.getInstance()
                                                   .getBean(MenuDAO.class);

                if (account.getTargetFolderId() != null) {
                    Menu menu = menuDao.findByPrimaryKey(account.getTargetFolderId()
                                                                .intValue());
                    account.setTargetFolder(menu);
                } else {
                    account.setTargetFolder(null);
                }

                account.setUserName(SessionManagement.getUsername());

                if(StringUtils.isNotEmpty(password)){
                	account.setAccountPassword(password);
                }else if(deletePassword){
                	account.setAccountPassword(null);
                }
                
                boolean stored = accountDao.store(account);

                if (stored) {
                    Messages.addLocalizedInfo("msg.action.saveemail");
                } else {
                    Messages.addLocalizedError("errors.action.saveemail");
                }

                AccountsRecordsManager recordsManager = ((AccountsRecordsManager) FacesContext.getCurrentInstance()
                                                                                              .getApplication()
                                                                                              .createValueBinding("#{accountsRecordsManager}")
                                                                                              .getValue(FacesContext.getCurrentInstance()));
                recordsManager.reload();
                recordsManager.setSelectedPanel("list");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                Messages.addLocalizedError("errors.action.saveemail");
            }
        } else {
            return "login";
        }

        return null;
    }

    void loadTree() {
        directoryModel = new DirectoryTreeModel();
    }

    public void onSelectDirectory(ActionEvent event) {
        int directoryId = Integer.parseInt((String) FacesContext.getCurrentInstance()
                                                                .getExternalContext()
                                                                .getRequestParameterMap()
                                                                .get("directoryId"));
        directoryModel.selectDirectory(directoryId);
        directoryModel.reload(directoryModel.getSelectedNode());
    }

    public String selectDirectory() {
        return null;
    }

    public Tree getTreeComponent() {
        return treeComponent;
    }

    public void setTreeComponent(Tree treeComponent) {
        this.treeComponent = treeComponent;
    }

	public UIComponent getMailAddress() {
		return mailAddress;
	}

	public void setMailAddress(UIComponent mailAddress) {
		this.mailAddress = mailAddress;
	}

	public UIComponent getAccountUser() {
		return accountUser;
	}

	public void setAccountUser(UIComponent accountUser) {
		this.accountUser = accountUser;
	}

	public UIComponent getAccountPassword() {
		return accountPassword;
	}

	public void setAccountPassword(UIComponent accountPassword) {
		this.accountPassword = accountPassword;
	}

	public UIComponent getProvider() {
		return provider;
	}

	public void setProvider(UIComponent provider) {
		this.provider = provider;
	}

	public UIComponent getDeleteFromMailbox() {
		return deleteFromMailbox;
	}

	public void setDeleteFromMailbox(UIComponent deleteFromMailbox) {
		this.deleteFromMailbox = deleteFromMailbox;
	}

	public UIComponent getHost() {
		return host;
	}

	public void setHost(UIComponent host) {
		this.host = host;
	}

	public UIComponent getPort() {
		return port;
	}

	public void setPort(UIComponent port) {
		this.port = port;
	}

	public UIComponent getAllowedTypes() {
		return allowedTypes;
	}

	public void setAllowedTypes(UIComponent allowedTypes) {
		this.allowedTypes = allowedTypes;
	}

	public UIComponent getLanguage() {
		return language;
	}

	public void setLanguage(UIComponent language) {
		this.language = language;
	}
	
	public String removePassword() {
		setPassword(null);
		deletePassword = true;
		return null;
	}
	
	public boolean isEmptyPassword(){
		return StringUtils.isEmpty(password);
	}
}
