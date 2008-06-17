package org.contineo.core.searchengine.dao;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.searchengine.SearchDocument;
import org.contineo.core.searchengine.dao.SearchDocumentDAO;
import org.contineo.core.security.Menu;

/**
 * Test case for <code>HibernateSearchDocumentDAO</code>
 * 
 * @author Alessandro Gasparini
 * @version $Id:$
 * @since 3.0
 */
public class HibernateSearchDocumentDAOTest extends AbstractCoreTestCase {

	// Instance under test
	private SearchDocumentDAO dao;

	public HibernateSearchDocumentDAOTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();

		// Retrieve the instance under test from spring context. Make sure that
		// is an HibernateSearchDocumentDAO
		dao = (SearchDocumentDAO) context.getBean("SearchDocumentDAO");
	}

	public void testStore() {
		// Store a new settings
		SearchDocument sdocument = new SearchDocument();
		sdocument.setLuceneId(2);
		/**
		 * Pay particular attenction: the property menuId is a foreign key. 
		 * FOREIGN KEY(co_menuid) REFERENCES co_menus(co_menuid)
		 */		
		sdocument.setMenuId(Menu.MENUID_PERSONAL);
		sdocument.setIndex("testIndex");
		assertTrue("Unable to store", dao.store(sdocument));

		// Check database status
		sdocument = dao.findByMenuId(Menu.MENUID_PERSONAL);
		assertNotNull(sdocument);
		assertEquals(Menu.MENUID_PERSONAL, sdocument.getMenuId());
	}

	public void testFindByMenuId() {
		// Try with unexisting settings
		SearchDocument sdocument = dao.findByMenuId(999);
		assertNull(sdocument);

		// Try with an existing settings
		sdocument = dao.findByMenuId(Menu.MENUID_PERSONAL);
		assertNotNull(sdocument);
		assertEquals(Menu.MENUID_PERSONAL, sdocument.getMenuId());
	}

	public void testDeleteByMenuId() {
		// Try with unexisting search document
		boolean settings = dao.deleteByMenuId(999);
		assertTrue(settings);

		// Try with an existing settings
		settings = dao.deleteByMenuId(Menu.MENUID_PERSONAL);
		assertTrue(settings);
		
		// Verify that the search document has been deleted
		SearchDocument sdocument = dao.findByMenuId(Menu.MENUID_PERSONAL);
		assertNull(sdocument);
	}
}