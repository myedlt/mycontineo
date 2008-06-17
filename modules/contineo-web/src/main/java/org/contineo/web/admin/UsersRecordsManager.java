package org.contineo.web.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.communication.EMail;
import org.contineo.core.communication.dao.EMailDAO;
import org.contineo.core.document.History;
import org.contineo.core.document.dao.HistoryDAO;
import org.contineo.core.security.SecurityManager;
import org.contineo.core.security.User;
import org.contineo.core.security.UserDoc;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.security.dao.UserDAO;
import org.contineo.core.security.dao.UserDocDAO;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;

/**
 * <p>
 * The <code>UsersRecordsManager</code> class is responsible for constructing
 * the list of <code>User</code> beans which will be bound to a ice:dataTable
 * JSF component. <p/>
 * <p>
 * Large data sets could be handle by adding a ice:dataPaginator. Alternatively
 * the dataTable could also be hidden and the dataTable could be added to
 * scrollable ice:panelGroup.
 * </p>
 * 
 * @author Marco Meschieri
 * @version $Id: DocumentsRecordsManager.java,v 1.1 2007/06/29 06:28:29 marco
 *          Exp $
 * @since 3.0
 */
public class UsersRecordsManager {
	protected static Log log = LogFactory.getLog(UsersRecordsManager.class);

	private Collection<User> users = new ArrayList<User>();

	private String selectedPanel = "list";

	public UsersRecordsManager() {
	}

	public void reload() {
		users.clear();

		try {
			MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
			String uname = SessionManagement.getUsername();

			if (mdao.isReadEnable(6, uname)) {
				UserDAO dao = (UserDAO) Context.getInstance().getBean(UserDAO.class);
				users = dao.findAll();
			} else {
				Messages.addLocalizedError("errors.noaccess");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			Messages.addLocalizedError("errors.error");
		}
	}

	public String getSelectedPanel() {
		return selectedPanel;
	}

	public void setSelectedPanel(String panel) {
		this.selectedPanel = panel;
	}

	public String addUser() {
		selectedPanel = "add";

		UserForm userForm = ((UserForm) FacesContext.getCurrentInstance().getApplication().createValueBinding(
				"#{userForm}").getValue(FacesContext.getCurrentInstance()));
		userForm.setUser(new User());

		return null;
	}

	public String edit() {
		selectedPanel = "edit";

		UserForm userForm = ((UserForm) FacesContext.getCurrentInstance().getApplication().createValueBinding(
				"#{userForm}").getValue(FacesContext.getCurrentInstance()));
		User user = (User) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("item");
		userForm.setUser(user);

		return null;
	}

	public String password() {
		selectedPanel = "passwd";

		UserForm userForm = ((UserForm) FacesContext.getCurrentInstance().getApplication().createValueBinding(
				"#{userForm}").getValue(FacesContext.getCurrentInstance()));
		User user = (User) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("item");
		userForm.setUser(user);

		return null;
	}

	public String list() {
		selectedPanel = "list";
		reload();

		return null;
	}

	/**
	 * Gets the list of UserRecord which will be used by the ice:dataTable
	 * component.
	 */
	public Collection<User> getUsers() {
		if (users.size() == 0) {
			reload();
		}

		return users;
	}

	public int getCount() {
		return getUsers().size();
	}

	public String delete() {
		User user = (User) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("item");

		if (SessionManagement.isValid()) {
			try {
				MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
				String uname = SessionManagement.getUsername();
				UserDAO dao = (UserDAO) Context.getInstance().getBean(UserDAO.class);
				SecurityManager manager = (SecurityManager) Context.getInstance().getBean(SecurityManager.class);

				// get the user's groups and check if he is member of
				// "admin" group
				User toBeDeletedUser = dao.findByPrimaryKey(user.getUserName());
				boolean isAdmin = false;

				if (toBeDeletedUser != null) {
					toBeDeletedUser.initGroupNames();

					String[] userGroups = toBeDeletedUser.getGroupNames();

					if (userGroups != null) {
						for (int i = 0; i < userGroups.length; i++) {
							if (userGroups[i].equals("admin")) {
								isAdmin = true;

								break;
							}
						}
					}
				}

				// if the user is member of "admin", we have to check that
				// he is not the last user in that group;
				// here we count how many users still belong to group admin
				int adminsFound = 0;

				if (isAdmin) {
					Collection allUsers = dao.findAll();
					Iterator userIter = allUsers.iterator(); // get all
					// users

					while (userIter.hasNext()) {
						User currUser = (User) userIter.next();
						currUser.initGroupNames(); // we always to call
						// this before accessing
						// the groups

						String[] groups = currUser.getGroupNames();

						if (groups != null) {
							for (int i = 0; i < groups.length; i++) {
								if (groups[i].equals("admin")) {
									adminsFound++;

									break; // for performance reasons we
									// break if we found enough
									// users
								}
							}
						}

						// basically we are just interested that there are
						// at least 2 users,
						// so we can safely delete one
						if (adminsFound > 2) {
							break;
						}
					}
				}

				// now we can try to delete the user
				if (!isAdmin || (isAdmin && (adminsFound > 1))) {
					// delete emails and email accounts
					EMailDAO emailDao = (EMailDAO) Context.getInstance().getBean(EMailDAO.class);
					Collection coll = emailDao.findByUserName(user.getUserName());
					Iterator iter = coll.iterator();

					while (iter.hasNext()) {
						EMail email = (EMail) iter.next();
						emailDao.delete(email.getMessageId());
					}

					// delete user doc entries (recently accessed files)
					UserDocDAO userDocDao = (UserDocDAO) Context.getInstance().getBean(UserDocDAO.class);
					Collection userDocColl = userDocDao.findByUserName(user.getUserName());
					Iterator userDocIter = userDocColl.iterator();

					while (userDocIter.hasNext()) {
						UserDoc userDoc = (UserDoc) userDocIter.next();
						userDocDao.delete(user.getUserName(), userDoc.getMenuId());
					}

					// delete all history entries connected to this user
					HistoryDAO historyDAO = (HistoryDAO) Context.getInstance().getBean(HistoryDAO.class);
					Collection historyColl = historyDAO.findByUsername(user.getUserName());
					Iterator historyIter = historyColl.iterator();

					while (historyIter.hasNext()) {
						History history = (History) historyIter.next();
						historyDAO.delete(history.getHistoryId());
					}

					manager.removeUserFromAllGroups(toBeDeletedUser);

					boolean deleted = dao.delete(user.getUserName());

					if (!deleted) {
						Messages.addLocalizedError("errors.action.deleteuser");
					} else {
						Messages.addLocalizedInfo("msg.action.deleteuser");

						SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
						String userdir = conf.getValue("userdir") + "/" + user.getUserName();
						FileBean.deleteDir(userdir);
					}
				} else if (isAdmin && (adminsFound < 2)) {
					Messages.addLocalizedInfo("msg.action.deleteuser.admingroup");
				}

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addLocalizedError("errors.action.deleteuser");
			}
		} else {
			return "login";
		}

		setSelectedPanel("list");
		reload();

		return null;
	}
}
