package org.contineo.core.document.dao;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.document.Document;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of <code>DocumentDAO</code>
 * 
 * @author Marco Meschieri
 * @version $Id: HibernateDocumentDAO.java,v 1.1 2007/06/29 06:28:28 marco Exp $
 * @since 3.0
 */
public class HibernateDocumentDAO extends HibernateDaoSupport implements DocumentDAO {

	protected static Log log = LogFactory.getLog(HibernateDocumentDAO.class);

	private ArticleDAO articleDAO;

	private HistoryDAO historyDAO;

	private MenuDAO menuDAO;

	private HibernateDocumentDAO() {
	}

	public MenuDAO getMenuDAO() {
		return menuDAO;
	}

	public void setMenuDAO(MenuDAO menuDAO) {
		this.menuDAO = menuDAO;
	}

	public ArticleDAO getArticleDAO() {
		return articleDAO;
	}

	public void setArticleDAO(ArticleDAO articleDAO) {
		this.articleDAO = articleDAO;
	}

	public HistoryDAO getHistoryDAO() {
		return historyDAO;
	}

	public void setHistoryDAO(HistoryDAO historyDAO) {
		this.historyDAO = historyDAO;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#delete(int)
	 */
	public boolean delete(int docId) {
		boolean result = true;

		try {
			Document doc = (Document) getHibernateTemplate().get(Document.class, docId);
			if (doc != null) {
				getHibernateTemplate().deleteAll(articleDAO.findByDocId(docId));
				getHibernateTemplate().deleteAll(historyDAO.findByDocId(docId));
				Menu menu = doc.getMenu();
				doc.getVersions().clear();
				doc.getKeywords().clear();
				doc.setMenu(null);
				getHibernateTemplate().delete(doc);
				menuDAO.delete(menu.getMenuId());
			}
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#deleteByMenuId(int)
	 */
	public boolean deleteByMenuId(int menuId) {
		Document doc = findByMenuId(menuId);
		if (doc != null)
			return delete(doc.getDocId());
		else
			return true;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	public Collection<Document> findAll() {
		Collection<Document> coll = new ArrayList<Document>();

		try {
			coll = (Collection<Document>) getHibernateTemplate().find("from org.contineo.core.document.Document");
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return coll;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#findByMenuId(int)
	 */
	@SuppressWarnings("unchecked")
	public Document findByMenuId(int menuId) {
		Document doc = null;

		try {
			Collection<Document> coll = (Collection<Document>) getHibernateTemplate().find(
					"from org.contineo.core.document.Document _doc where _doc.menu.menuId = ?",
					new Object[] { new Integer(menuId) });
			if (!coll.isEmpty())
				doc = coll.iterator().next();
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return doc;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#findByPrimaryKey(int)
	 */
	public Document findByPrimaryKey(int docId) {
		Document doc = null;

		try {
			doc = (Document) getHibernateTemplate().get(Document.class, docId);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return doc;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#findByUserName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Integer> findByUserName(String username) {
		Collection<Integer> coll = new ArrayList<Integer>();

		try {
			Collection<Menu> menus = menuDAO.findByUserName(username);
			if (menus.isEmpty())
				return coll;

			StringBuffer query = new StringBuffer("select docId from org.contineo.core.document.Document _doc where ");
			query.append("_doc.menu.menuId in (");
			boolean first = true;
			for (Menu menu : menus) {
				if (!first)
					query.append(",");
				query.append("'" + menu.getMenuId() + "'");
				first = false;
			}
			query.append(")");

			coll = (Collection<Integer>) getHibernateTemplate().find(query.toString());

		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return coll;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#findMenuIdByKeyword(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Integer> findMenuIdByKeyword(String keyword) {
		Collection<Integer> coll = new ArrayList<Integer>();

		try {
			StringBuilder query = new StringBuilder(
					"select menu.menuId from org.contineo.core.document.Document _doc where ");
			query.append("'" + keyword + "'");
			query.append(" in elements(_doc.keywords) ");
			coll = (Collection<Integer>) getHibernateTemplate().find(query.toString());
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return coll;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#store(org.contineo.core.document.Document)
	 */
	@SuppressWarnings("unchecked")
	public boolean store(final Document doc) {
		boolean result = true;
		try {
			Set<String> src = doc.getKeywords();
			if (src != null && src.size() > 0) {
				// Trim too long keywords
				Set<String> dst = new HashSet<String>();
				for (String str : src) {
					String s = str;
					if (str.length() > 20) {
						s = str.substring(0, 20);
					}
					if (!dst.contains(s))
						dst.add(s);
				}
				doc.setKeywords(dst);
			}
			getHibernateTemplate().saveOrUpdate(doc);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}

	/**
	 * @see org.contineo.core.document.dao.DocumentDAO#toKeywords(java.lang.String)
	 */
	public Set<String> toKeywords(String words) {
		Set<String> coll = new HashSet<String>();
		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(words);

		int start = boundary.first();

		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			String word = words.substring(start, end).toLowerCase().trim();

			if (word.length() > 2) {
				if (word.length() > 20)
					coll.add(word.substring(0, 20));
				else
					coll.add(word);
			}
		}

		return coll;
	}

	@SuppressWarnings("unchecked")
	public Collection<String> findKeywords(String firstLetter, String username) {
		Collection<String> coll = new ArrayList<String>();

		try {
			DocumentDAO documentDAO = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
			Collection<Integer> precoll = documentDAO.findByUserName(username);

			if ((precoll == null) || (precoll.size() == 0)) {
				return null;
			}

			StringBuilder query = new StringBuilder(
					"select item from org.contineo.core.document.Document _doc join _doc.keywords item where lower(item) like '")
					.append(firstLetter.toLowerCase()).append("%' ");
			query.append("and _doc.docId in (");
			boolean first = true;
			for (Integer docId : precoll) {
				if (!first)
					query.append(",");
				query.append("'" + docId + "'");
				first = false;
			}
			query.append(")");

			coll = (Collection<String>) getHibernateTemplate().find(query.toString());
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage(), e);
		}

		return coll;
	}
}