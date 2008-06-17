package org.contineo.core.security.dao;

import java.util.Collection;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.security.ExtMenu;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;

/**
 * Test case for <code>HibernateMenuDAOTest</code>
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class HibernateMenuDAOTest extends AbstractCoreTestCase {

	// Instance under test
	private MenuDAO dao;

	public HibernateMenuDAOTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		// Retrieve the instance under test from spring context. Make sure that
		// it is an HibernateMenuDAO
		dao = (MenuDAO) context.getBean("MenuDAO");
	}

	public void testStore() {
		Menu menu = new Menu();
		menu.setMenuText("text");
		menu.setMenuPath("path");
		menu.setMenuSort(1);
		menu.setMenuGroup(new String[] { "admin", "author" });
		assertTrue(dao.store(menu));
		menu = dao.findByPrimaryKey(100);
		assertEquals("db.admin", menu.getMenuText());
		assertEquals("ROOT", menu.getMenuPath());
		assertEquals(1, menu.getMenuSort());
		assertEquals(2, menu.getMenuGroups().size());

		// Load an existing menu and modify it
		menu = dao.findByPrimaryKey(Menu.MENUID_HOME);
		assertEquals("db.home", menu.getMenuText());
		menu.setMenuText("xxxx");
		assertTrue(dao.store(menu));
		menu = dao.findByPrimaryKey(Menu.MENUID_HOME);
		assertEquals("xxxx", menu.getMenuText());
	}

	public void testDelete() {
		assertTrue(dao.delete(99));
		Menu menu = dao.findByPrimaryKey(99);
		assertNull(menu);

        DocumentDAO docDao=(DocumentDAO)context.getBean("DocumentDAO");
        docDao.delete(1);
        assertTrue(dao.delete(103));
        menu = dao.findByPrimaryKey(103);
        assertNull(menu);
	}

	public void testFindByPrimaryKey() {
		// Try with a menu id
		Menu menu = dao.findByPrimaryKey(1);
		assertNotNull(menu);
		assertEquals(Menu.MENUID_HOME, menu.getMenuId());
		assertEquals("db.home", menu.getMenuText());
		assertEquals("home.png", menu.getMenuIcon());
		assertEquals(1, menu.getMenuSort());
		assertEquals(3, menu.getMenuGroups().size());

		// Try with unexisting id
		menu = dao.findByPrimaryKey(99999);
		assertNull(menu);
	}

	@SuppressWarnings("unchecked")
	public void testFindByMenuText() {
		// Try with existing text
		Collection<Menu> menues = (Collection<Menu>) dao.findByMenuText("db.admin");
		assertNotNull(menues);
		assertEquals(5, menues.size());
		Menu menu = menues.iterator().next();
		assertEquals("db.admin", menu.getMenuText());

		// Try with unexisting text
		menues = dao.findByMenuText("xxxxx");
		assertNotNull(menues);
		assertTrue(menues.isEmpty());
	}

	public void testFindByUserNameString() {
		Collection<Menu> menues = dao.findByUserName("admin");
		assertNotNull(menues);
		assertEquals(20, menues.size());

		menues = dao.findByUserName("sebastian");
		assertNotNull(menues);
		assertEquals(20, menues.size());

		// Try with unexisting username
		menues = dao.findByUserName("xxx");
		assertNotNull(menues);
		assertEquals(0, menues.size());
	}

	public void testFindByUserNameAndKeyword() {
		Collection<Menu> menues = dao.findByUserNameAndKeyword("admin", "abc");
		assertNotNull(menues);
		assertEquals(1, menues.size());
		assertEquals(103, menues.iterator().next().getMenuId());
	}

	public void testFindByUserNameStringInt() {
		Collection<Menu> menues = dao.findByUserName("admin", Menu.MENUID_HOME);
		assertNotNull(menues);
		assertEquals(6, menues.size());

		// Try with unexisting usernames and menues
		menues = dao.findByUserName("admin", 70);
		assertNotNull(menues);
		assertEquals(0, menues.size());

		menues = dao.findByUserName("xxxx", Menu.MENUID_HOME);
		assertNotNull(menues);
		assertEquals(0, menues.size());
	}

	public void testFindByParentId() {
		Collection<Menu> menues = dao.findByParentId(Menu.MENUID_HOME);
		assertNotNull(menues);
		assertEquals(21, menues.size());

		// Try with unexisting parent
		menues = dao.findByParentId(999);
		assertNotNull(menues);
		assertEquals(0, menues.size());
	}

	public void testIsWriteEnable() {
		assertFalse(dao.isWriteEnable(Menu.MENUID_HOME, "admin"));
		assertTrue(dao.isWriteEnable(26, "admin"));
		assertFalse(dao.isWriteEnable(Menu.MENUID_HOME, "guest"));
		assertFalse(dao.isWriteEnable(Menu.MENUID_HOME, "xxxx"));
	}

	public void testIsReadEnable() {
		assertTrue(dao.isReadEnable(Menu.MENUID_HOME, "admin"));
		assertTrue(dao.isReadEnable(26, "admin"));
		assertFalse(dao.isReadEnable(Menu.MENUID_HOME, "guest"));
		assertFalse(dao.isReadEnable(Menu.MENUID_HOME, "xxxx"));
	}

	public void testFindMenuIdByUserName() {
		Collection<Integer> ids = dao.findMenuIdByUserName("admin");
		assertNotNull(ids);
		assertEquals(20,ids.size());

		// Try with unexisting username
		ids = dao.findMenuIdByUserName("xxxx");
		assertNotNull(ids);
		assertEquals(0, ids.size());
	}

	public void testGetContainedMenus() {
		Collection<ExtMenu> coll = dao.getContainedMenus(2, "admin");
		assertEquals(7, coll.size());
		coll = dao.getContainedMenus(2, "sebastian");
		assertEquals(7, coll.size());
	}

	public void testHasWriteAccess() {
		Menu menu = dao.findByPrimaryKey(103);
		assertTrue(dao.hasWriteAccess(menu, "admin"));
		assertTrue(dao.hasWriteAccess(menu, "sebastian"));
		assertFalse(dao.hasWriteAccess(menu, "test"));
	}

	public void testFindByGroupName() {
		Collection<Menu> menues = dao.findByGroupName("admin");
		assertEquals(20, menues.size());
		menues = dao.findByGroupName("testGroup");
		assertEquals(0, menues.size());
	}
}