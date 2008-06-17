package org.contineo.core.searchengine.crawler;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;


/**
 * Created on 5. November 2003, 16:53
 * 
 * @author Michael Scholz
 */
public class LuceneDocument
{
    private File file = null;

    private Document doc;

    private String content = "";


    private org.contineo.core.document.Document document =
        new org.contineo.core.document.Document();

  public LuceneDocument(org.contineo.core.document.Document d)
    {
        document = d;
    }

    /**
	 * Builds a lucene compatible document of a file. The document contains 7
	 * Fields: name - name of the document size - size of the document in bytes
	 * path - path of the document for calling it on the web browser (e.g
	 * DocFrame.do?menuId=1) type - file format (e.g pdf, sxw) date - date of
	 * creation content - full text of the document summary - first 500 letters
	 * of the content
	 * 
	 * @param f - File of which the document should be built.
	 * @return
	 */
    public Document getDocument(
        File         f,
        String content)
    {
        file = f;
        doc = new Document();
        setDocId();
        setName();
        setSize();
        setPath();
        setDocData();
        setType();
        setDate();
        setContent(content);
        setSummary();
        setKeywords();
        return doc;
    }

    public void setDocId()
    {
        doc.add(new Field("docid", String.valueOf(document.getDocId()),
                Field.Store.YES, Field.Index.NO));
    }

    /**
	 * Returns the content of the indexed document.
	 * 
	 * @return
	 */
    public String getContent()
    {
        return content;
    }
    
    protected void setName()
    {
        doc.add(new Field("name", document.getDocName(), Field.Store.YES,
                Field.Index.TOKENIZED));
    }
    
    protected void setSize()
    {
        String size = String.valueOf(file.length() / 1024);
        doc.add(new Field("size", size, Field.Store.YES,
                Field.Index.UN_TOKENIZED));
    }
    
    protected void setPath()
    {
        String menuId = String.valueOf(document.getMenuId());
        String path = "download?menuId=" + menuId;
        doc.add(new Field("menuId", menuId, Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        doc.add(new Field("path", path, Field.Store.YES, Field.Index.NO));
    }

    protected void setDocData()
    {
        doc.add(new Field("source", document.getSource(), Field.Store.NO,
                Field.Index.TOKENIZED));
        doc.add(new Field("sourceauthor", document.getSourceAuthor(),
                Field.Store.NO, Field.Index.TOKENIZED));
        doc.add(new Field("sourcetype", document.getSourceType(),
                Field.Store.NO, Field.Index.TOKENIZED));
        doc.add(new Field("coverage", document.getCoverage(), Field.Store.NO,
                Field.Index.TOKENIZED));
        doc.add(new Field("sourceDate", document.getSourceDate(), Field.Store.YES,
                Field.Index.UN_TOKENIZED));
    }

    protected void setType()
    {
        int point = file.getName()
                .lastIndexOf(".");
        String type = file.getName()
                .substring(point + 1);
        type = type.toUpperCase();
        doc.add(new Field("type", type, Field.Store.YES,
                Field.Index.UN_TOKENIZED));
    }

    protected void setDate()
    {
        long date = file.lastModified();
        doc.add(new Field("date", String.valueOf(date), Field.Store.YES,
                Field.Index.UN_TOKENIZED));
    }
    
    protected void setContent(String content)
    {
        doc.add(new Field("content", content, Field.Store.YES,
                Field.Index.TOKENIZED));
        doc.add(new Field("length", String.valueOf(content.length()),
                Field.Store.YES, Field.Index.NO));
    }

    protected void setSummary()
    {
        int summarysize = Math.min(content.length(), 500);
        String summary = content.substring(0, summarysize);
        doc.add(new Field("summary", summary, Field.Store.YES,
                Field.Index.TOKENIZED));
    }

    protected void setKeywords()
    {
        doc.add(new Field("keywords", document.getKeywordsString(),
                Field.Store.YES, Field.Index.TOKENIZED));
    } 
}