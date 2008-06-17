package org.contineo.core.searchengine.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.searchengine.SearchDocument;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Alessandro Gasparini
 * @since 2006-12-18
 */
public class HibernateSearchDocumentDAO extends HibernateDaoSupport implements
		SearchDocumentDAO {

	protected static Log log = LogFactory
			.getLog(HibernateSearchDocumentDAO.class);

	private HibernateSearchDocumentDAO() {
	}

	/*
	 * @see org.contineo.core.searchengine.dao.SearchDocumentDAO#store(org.contineo.core.searchengine.SearchDocument)
	 */
	public boolean store(SearchDocument doc) {
		boolean result = true;

		try {
			getHibernateTemplate().saveOrUpdate(doc);
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage());
			result = false;
			e.printStackTrace();
		}

		return result;
	}

	/*
	 * @see org.contineo.core.searchengine.dao.SearchDocumentDAO#delete(int)
	 */
	public boolean deleteByMenuId(int menuId) {
		boolean result = true;

		try {
			DetachedCriteria dt = DetachedCriteria.forClass(
					SearchDocument.class);
			dt.add(Property.forName("menuId").eq(new Integer(menuId)));

			if (log.isDebugEnabled())
				log.debug("deleteByMenuId DetachedCriteria = " + dt);

			List searchList = getHibernateTemplate().findByCriteria(dt);
			getHibernateTemplate().deleteAll(searchList);
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error(e.getMessage());
			}
			result = false;
		}

		return result;
	}

	/*
	 * @see org.contineo.core.searchengine.dao.SearchDocumentDAO#findByMenuId(int)
	 */
	public SearchDocument findByMenuId(int menuId) {
		SearchDocument searchDoc = null;

		try {
			DetachedCriteria dt = DetachedCriteria.forClass(
					SearchDocument.class);
			dt.add(Property.forName("menuId").eq(new Integer(menuId)));

			if (log.isDebugEnabled())
				log.debug("findByMenuId DetachedCriteria = " + dt);

			/**
			 * TODO Check this behavior this query return a list of
			 * SearchDocument but we get only the first ? Taken from OJBVersion -
			 * A.Gasparini 17/12/2006
			 */

			List searchList = getHibernateTemplate().findByCriteria(dt);
			if (searchList != null && !searchList.isEmpty()) {
				searchDoc = (SearchDocument) searchList.get(0);
			}
		} catch (Exception e) {
			if (log.isErrorEnabled())
				log.error(e.getMessage());
		}

		return searchDoc;
	}

}
