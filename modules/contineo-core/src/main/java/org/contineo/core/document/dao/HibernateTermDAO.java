package org.contineo.core.document.dao;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.document.Term;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of <code>TermDAO</code>
 * 
 * @author Marco Meschieri
 * @version $Id: HibernateTermDAO.java,v 1.1 2007/06/29 06:28:28 marco Exp $
 * @since 3.0
 */
public class HibernateTermDAO extends HibernateDaoSupport implements TermDAO {

	protected static Log log = LogFactory.getLog(HibernateTermDAO.class);

	private HibernateTermDAO() {
	}

	/**
	 * @see org.contineo.core.document.dao.TermDAO#delete(int)
	 */
	@SuppressWarnings("unchecked")
	public boolean delete(int menuId) {
		boolean result = true;

		try {
			Collection<Term> coll = (Collection<Term>) getHibernateTemplate().find(
					"from org.contineo.core.document.Term _term where _term.id.menuId = ?",
					new Object[] { new Integer(menuId) });
			getHibernateTemplate().deleteAll(coll);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				logger.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}

	/**
	 * @see org.contineo.core.document.dao.TermDAO#findByMenuId(int)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Term> findByMenuId(int menuId) {
		Collection<Term> result = new ArrayList<Term>();

		try {
			result = (Collection<Term>) getHibernateTemplate().find(
					"from org.contineo.core.document.Term _term where _term.id.menuId = ?",
					new Object[] { new Integer(menuId) });
		} catch (Exception e) {
			if (log.isErrorEnabled())
				logger.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * @see org.contineo.core.document.dao.TermDAO#findByStem(int)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Term> findByStem(int menuId) {
		Collection<Term> result = new ArrayList<Term>();

		try {

			StringBuffer query = new StringBuffer("from org.contineo.core.document.Term _term where _term.id.menuId != ? ");
			Collection<Term> coll = findByMenuId(menuId);

			if (!coll.isEmpty()) {
				query.append("and _term.id.stem in (");
				boolean first=true;
				for (Term term : coll) {
					if(!first)
						query.append(",");
					query.append("'" + term.getStem() + "'");
					first=false;
				}
				query.append(") ");
			}

			query.append("order by id.menuId, id.stem, value, wordCount, originWord");
			result = (Collection<Term>) getHibernateTemplate().find(query.toString(),
					new Object[] { new Integer(menuId) });
		} catch (Exception e) {
			if (log.isErrorEnabled())
				logger.error(e.getMessage(), e);
		}

		return result;
	}

	/**
	 * @see org.contineo.core.document.dao.TermDAO#store(org.contineo.core.document.Term)
	 */
	public boolean store(Term term) {
		boolean result = true;

		try {
			getHibernateTemplate().saveOrUpdate(term);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				logger.error(e.getMessage(), e);
			result = false;
		}

		return result;
	}
}