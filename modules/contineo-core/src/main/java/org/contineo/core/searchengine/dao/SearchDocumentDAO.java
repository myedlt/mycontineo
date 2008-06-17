package org.contineo.core.searchengine.dao;

import org.contineo.core.searchengine.SearchDocument;

public interface SearchDocumentDAO {

	/**
	 * This method persists a SearchDocument object.
	 * 
	 * @param doc SearchDocument to be stored.
	 * @return True if successfully stored in a database.
	 */
	public boolean store(SearchDocument doc);

	/**
	 * This method deletes an SearchDocument using its menuId.
	 * 
	 * @param menuId MenuID of the SearchDocument which should be delete.
	 */
	public boolean deleteByMenuId(int menuId);

	/**
	 * This method selects all articles of for a given document.
	 * 
	 * @param menuId - MenuID of the SearchDocument.
	 */
	public SearchDocument findByMenuId(int menuId);
}