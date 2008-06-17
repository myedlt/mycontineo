package org.contineo.core.searchengine;

/**
 * Created on 08.12.2004
 * @author  Michael Scholz
 */
public class SearchDocument
{
    private int luceneId;
    private int menuId;
    private String index;


    public SearchDocument()
    {
        luceneId = 0;
        menuId = 0;
        index = "english";
    } 

    /**
     * @return  Returns the luceneId.
     */
    public int getLuceneId()
    {
        return luceneId;
    } 

    /**
     * @param luceneId  The luceneId to set.
     */
    public void setLuceneId(int luceneId)
    {
        this.luceneId = luceneId;
    } 

    /**
     * @return  Returns the menuId.
     */
    public int getMenuId()
    {
        return menuId;
    } 

    /**
     * @param menuId  The menuId to set.
     */
    public void setMenuId(int menuId)
    {
        this.menuId = menuId;
    } 

    /**
     * @return  Returns the index.
     */
    public String getIndex()
    {
        return index;
    } 

    /**
     * @param index  The index to set.
     */
    public void setIndex(String index)
    {
        this.index = index;
    } 

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SearchDocument))
			return false;
		SearchDocument other=(SearchDocument)obj;
		return other.getLuceneId()==this.getLuceneId();
	}

	@Override
	public int hashCode() {
		return new Integer(luceneId).hashCode();
	}
    
    
} 
