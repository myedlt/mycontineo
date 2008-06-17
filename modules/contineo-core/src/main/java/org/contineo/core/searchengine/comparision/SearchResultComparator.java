package org.contineo.core.searchengine.comparision;

import java.util.Comparator;


/**
 * @author Michael Scholz
 * @author Marco Meschieri
 */
public class SearchResultComparator
    implements Comparator<SearchResult>
{

    public SearchResultComparator()
    {
    } 

    public int compare(
    		SearchResult sr1,
    		SearchResult sr2)
    {

        Double d1 = sr1.getScore();
        Double d2 = sr2.getScore();
        return -1 * d1.compareTo(d2);
    } 
}
