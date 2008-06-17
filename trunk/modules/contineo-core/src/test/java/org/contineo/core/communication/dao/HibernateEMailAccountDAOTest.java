package org.contineo.core.communication.dao;

import java.util.Collection;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.communication.EMailAccount;
import org.contineo.core.communication.dao.EMailAccountDAO;

public class HibernateEMailAccountDAOTest extends AbstractCoreTestCase {

    // Instance under test
    private EMailAccountDAO dao;    
    
    public HibernateEMailAccountDAOTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        // Retrieve the instance under test from spring context.
        // Make sure that it is an HibernateEMailAccountDAO
        dao = (EMailAccountDAO) context.getBean("EMailAccountDAO");
    }
    
    public void testStore() {
        EMailAccount account = new EMailAccount();
        account.setMailAddress("author@contineo.sf.net");
        account.setProvider("Aruba");
        account.setHost("pcalle");
        account.setAccountUser("author@contineo.sf.net");
        account.setAccountPassword("fghfgh");
        account.setUserName("admin");
        account.setDeleteFromMailbox(0);
        
        assertTrue(dao.store(account));
        
        // Load an existing account and modify it
        EMailAccount account02 = dao.findByPrimaryKey(account.getAccountId());
        assertNotNull(account02);
        assertEquals("author@contineo.sf.net", account02.getMailAddress());
        assertEquals("Aruba", account02.getProvider());
        assertEquals("pcalle", account02.getHost());
        assertEquals("author@contineo.sf.net", account02.getAccountUser());
        assertEquals("fghfgh", account02.getAccountPassword());
                              
        account02.setMailAddress("updated@contineo.sf.net");
        assertTrue(dao.store(account02));
        
        // Verify the stored account
        account = dao.findByPrimaryKey(account.getAccountId());
        assertNotNull(account);
        assertEquals("updated@contineo.sf.net", account.getMailAddress());       
    }

    public void testDelete() {
        EMailAccount account = dao.findByPrimaryKey(2);
        assertNotNull(account);
        assertTrue(dao.delete(2));
        
        account = dao.findByPrimaryKey(2);
        assertNull(account);
    }

    public void testFindByPrimaryKey() {
        EMailAccount account = dao.findByPrimaryKey(1);
        assertNotNull(account);
        assertEquals(1, account.getAccountId());
        assertTrue(account.isAllowed("pdf"));
        assertFalse(account.isAllowed("xxx"));

        // Try with unexisting article
        account = dao.findByPrimaryKey(99);
        assertNull(account);
    }
    
    public void testDeleteByUsername() {
        assertTrue(dao.deleteByUsername("author"));
        
        Collection accounts = dao.findByUserName("author");
        assertTrue(accounts.isEmpty());
    }

    public void testFindAll() {
        Collection accounts = dao.findAll();
        assertNotNull(accounts);
        assertEquals(2, accounts.size());
    }


    public void testFindByUserName() {
        Collection accounts = dao.findByUserName("admin");
        assertNotNull(accounts);
        assertEquals(1, accounts.size());
        
        accounts = dao.findByUserName("author");
        assertNotNull(accounts);
        assertEquals(1, accounts.size());

        // Try with unexisting user
        accounts = dao.findByUserName("xxx");
        assertNotNull(accounts);
        assertEquals(0, accounts.size());
    }
}
