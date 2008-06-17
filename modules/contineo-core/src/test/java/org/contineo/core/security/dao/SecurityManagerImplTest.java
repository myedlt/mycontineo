package org.contineo.core.security.dao;

import java.util.ArrayList;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.security.Group;
import org.contineo.core.security.SecurityManager;
import org.contineo.core.security.User;
import org.contineo.core.security.dao.GroupDAO;
import org.contineo.core.security.dao.UserDAO;

/**
 * Test case for the manager <code>SecurityManager<code>
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 *
 */
public class SecurityManagerImplTest extends AbstractCoreTestCase {
	// Instance under test
	private SecurityManager manager;

	private UserDAO userDAO;

	private GroupDAO groupDAO;

	public SecurityManagerImplTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		manager = (SecurityManager) context.getBean("SecurityManager");
		userDAO = (UserDAO) context.getBean("UserDAO");
		groupDAO = (GroupDAO) context.getBean("GroupDAO");
	}

	public void testAssignUsersToGroup() {
		ArrayList<User> users = new ArrayList<User>();
		users.add(userDAO.findByPrimaryKey("test"));
		users.add(userDAO.findByPrimaryKey("admin"));
		Group group = groupDAO.findByPrimaryKey("author");
		manager.assignUsersToGroup(users, group);
		User user = userDAO.findByPrimaryKey("test");
		assertTrue(user.getGroups().contains(group));
		user = userDAO.findByPrimaryKey("admin");
		assertTrue(user.getGroups().contains(group));

		group = groupDAO.findByPrimaryKey("guest");
		manager.assignUsersToGroup(users, group);
		user = userDAO.findByPrimaryKey("test");
		assertTrue(user.getGroups().contains(group));
		user = userDAO.findByPrimaryKey("admin");
		assertTrue(user.getGroups().contains(group));
	}

	public void testRemoveUsersFromGroup() {
		ArrayList<User> users = new ArrayList<User>();
		users.add(userDAO.findByPrimaryKey("test"));
		users.add(userDAO.findByPrimaryKey("admin"));
		Group group = groupDAO.findByPrimaryKey("author");
		manager.removeUsersFromGroup(users, group);
		User user = userDAO.findByPrimaryKey("test");
		assertFalse(user.getGroups().contains(group));
		user = userDAO.findByPrimaryKey("admin");
		assertFalse(user.getGroups().contains(group));

		group = groupDAO.findByPrimaryKey("guest");
		manager.removeUsersFromGroup(users, group);
		user = userDAO.findByPrimaryKey("test");
		assertFalse(user.getGroups().contains(group));
		user = userDAO.findByPrimaryKey("admin");
		assertFalse(user.getGroups().contains(group));
	}
}
