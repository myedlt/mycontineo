package org.contineo.core.document.dao;

import java.util.Collection;

import org.contineo.core.AbstractCoreTestCase;
import org.contineo.core.document.Term;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.security.Menu;

/**
 * Test case for <code>HibernateTermDAO</code>
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class HibernateTermDAOTest extends AbstractCoreTestCase {
	// Instance under test
	private TermDAO dao;
	
	public HibernateTermDAOTest(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		// Retrieve the instance under test from spring context. Make sure that
		// it is an HibernateTermDAO
		dao = (TermDAO) context.getBean("TermDAO");
	}

	public void testDelete() {
		Collection<Term> terms=dao.findByMenuId(1);
		assertNotNull(terms);
		assertEquals(2, terms.size());
		
		assertTrue(dao.delete(1));
		terms=dao.findByMenuId(1);
		assertNotNull(terms);
		assertEquals(0, terms.size());
	}

	public void testFindByMenuId() {
		Collection<Term> terms=dao.findByMenuId(1);
		assertNotNull(terms);
		assertEquals(2, terms.size());
		
		terms=dao.findByMenuId(9);
		assertNotNull(terms);
		assertEquals(0, terms.size());
		
		terms=dao.findByMenuId(2);
		assertNotNull(terms);
		assertEquals(1, terms.size());
		assertEquals(0.3, terms.iterator().next().getValue());
		
		terms=dao.findByMenuId(999);
		assertNotNull(terms);
		assertEquals(0, terms.size());
	}

	public void testFindByStem() {
		Collection<Term> terms=dao.findByStem(1);
		assertNotNull(terms);
		assertEquals(3, terms.size());
		
		terms=dao.findByStem(2);
		assertNotNull(terms);
		assertEquals(3, terms.size());
		
		terms=dao.findByStem(9);
		assertNotNull(terms);
		assertEquals(5, terms.size());
	}

	public void testStore() {
		Term term=new Term();
		term.setMenuId(Menu.MENUID_DOCUMENTS);
		term.setStem("d");
		term.setOriginWord("xxx");
		term.setValue(1.9);
		term.setWordCount(6);
		assertTrue(dao.store(term));
		
		Term storedTerm=dao.findByMenuId(Menu.MENUID_DOCUMENTS).iterator().next();
		assertEquals(term, storedTerm);
		assertEquals("xxx", storedTerm.getOriginWord());
		assertEquals(1.9, storedTerm.getValue());
		assertEquals(6, storedTerm.getWordCount());
		
		//Load an existing term and modify it
		term=dao.findByMenuId(4).iterator().next();
		term.setWordCount(11);
		term.setValue(14.2);
		assertTrue(dao.store(term));
		storedTerm=dao.findByMenuId(4).iterator().next();
		assertEquals(term, storedTerm);
		assertEquals(11, storedTerm.getWordCount());
		assertEquals(14.2, storedTerm.getValue());
	}

}
