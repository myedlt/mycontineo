package org.contineo.core.searchengine.comparision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.contineo.core.document.Term;
import org.contineo.core.document.dao.TermDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;

/**
 * Class for finding similar documents. Created on 21.03.2004
 * 
 * @author Michael Scholz
 */
public class Searcher {

	public Searcher() {
	}

	/**
	 * This method finds documents, which are similar to a reference document.
	 * All documents are valued by dice-coefficient. dice-coefficient = 2*scalar
	 * product (doc1,doc2) / (absolute value(doc1) + absoulute value(doc2))
	 * 
	 * @param docId - ID of the reference document.
	 * @param minScore - Minimum score value (between 0 and 1)
	 * @return Collection of similar documents sorted by score value.
	 */
	public Collection findSimilarDocuments(int menuId, double minScore, String username) {
		TermDAO termsDao = (TermDAO) Context.getInstance().getBean(TermDAO.class);
		Collection basicTerms = termsDao.findByMenuId(menuId);

		// select all documents having a keyword a the basic document
		Collection<Term> terms = termsDao.findByStem(menuId);
		Collection<SearchResult> result = new ArrayList<SearchResult>();
		Iterator iter = terms.iterator();
		MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Collection<Integer> coll2 = mdao.findMenuIdByUserName(username);

		while (iter.hasNext()) {
			// calculate the score for ranking
			Term term = (Term) iter.next();

			if (coll2.contains(new Integer(term.getMenuId()))) {
				Collection docTerms = termsDao.findByMenuId(term.getMenuId());
				double score = calculateScore(basicTerms, docTerms);

				if (score >= minScore) {
					SearchResult sres = new SearchResult();
					Menu menu = mdao.findByPrimaryKey(term.getMenuId());
					sres.setScore(score);
					sres.setIcon(menu.getMenuIcon());
					sres.setMenuId(menu.getMenuId());
					sres.setName(menu.getMenuText());
					sres.setPath(menu.getMenuPath());
					if (!result.contains(sres))
						result.add(sres);
				}
			}
		}

		Collections.sort((List<SearchResult>) result, new SearchResultComparator());
		return result;
	}

	private double calculateScore(Collection refTerms, Collection terms) {
		double score = 0.0d;
		double abs1 = 0.0d;
		double abs2 = 0.0d;
		Hashtable table = convert(terms);
		Iterator iter = refTerms.iterator();

		while (iter.hasNext()) {
			Term term = (Term) iter.next();
			abs1 += term.getValue() * term.getValue();

			if (table.containsKey(term.getStem())) {
				Double value = (Double) table.get(term.getStem());
				abs2 += value.doubleValue() * value.doubleValue();
				score += value.doubleValue() * term.getValue();
			}
		}

		return (2 * score) / (abs1 + abs2);
	}

	private Hashtable convert(Collection coll) {
		Hashtable<String, Double> table = new Hashtable<String, Double>(coll.size());
		Iterator iter = coll.iterator();

		while (iter.hasNext()) {
			Term term = (Term) iter.next();
			table.put(term.getStem(), new Double(term.getValue()));
		}

		return table;
	}
}