package org.contineo.core.document.dao;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.document.DeleteItemUtil;
import org.contineo.core.security.dao.MenuDAO;

/**
 * Test case for <code>DeleteItemUtil</code>
 * 
 * @author     Marco Meschieri
 * @version    $Id: LogicalObjects_Code_Templates.xml,v 1.1 2005/01/21 17:56:30 marco Exp $
 * @since      ###release###
 */
public class DeleteItemUtilTest extends AbstractCoreTestCase {
    private MenuDAO menuDAO;
    
    
    public DeleteItemUtilTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        menuDAO=(MenuDAO)context.getBean("MenuDAO");
    }



    public void testDeleteFileMenu() {
        assertNotNull(menuDAO.findByPrimaryKey(99));
        assertTrue(DeleteItemUtil.deleteMenu(99, "admin"));
        assertNull(menuDAO.findByPrimaryKey(99));
    }
    
    public void testDeleteDirectoryMenu() {
        assertNotNull(menuDAO.findByPrimaryKey(100));
        assertNotNull(menuDAO.findByPrimaryKey(101));
        assertNotNull(menuDAO.findByPrimaryKey(102));
        assertTrue(DeleteItemUtil.deleteMenu(100, "admin"));
        assertNull(menuDAO.findByPrimaryKey(100));
        assertNull(menuDAO.findByPrimaryKey(101));
        assertNull(menuDAO.findByPrimaryKey(102));
    }
}