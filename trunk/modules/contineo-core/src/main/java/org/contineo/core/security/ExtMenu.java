/*
 * ExtMenu.java
 *
 * Created on 9. Februar 2004, 17:46
 */

package org.contineo.core.security;

import java.util.Date;


/**
 *
 * @author  Michael Scholz
 */
public class ExtMenu
    extends Menu
{
    /**
     * Defines the size of the requested menu; this is either
     * the number of children or the size of the file the
     * menu is pointing to.
     */
    private long size;

    /**
     * Defines the date of the requested menu.
     */
    private Date date;

    /**
     * Defines if the user requesting the menu has write access
     */
    private boolean writable;

    /**
     * Defines if the document is checked out
     */
    private int docStatus;

    /**
     * Defines the user name of the one who has checked out the document
     */
    private String checkoutUser;
    
    /**
     * Defines the document name 
     */    
    private String docName;
    

    /** Creates a new instance of ExtMenu */
    public ExtMenu(Menu m)
    {
        this.setMenuGroups(m.getMenuGroups());
        this.setMenuHier(m.getMenuHier());
        this.setMenuIcon(m.getMenuIcon());
        this.setMenuId(m.getMenuId());
        this.setMenuParent(m.getMenuParent());
        this.setMenuPath(m.getMenuPath());
        this.setMenuRef(m.getMenuRef());
        this.setMenuSort(m.getMenuSort());
        this.setMenuText(m.getMenuText());
        this.setMenuType(m.getMenuType());
        size = 0;
        writable = false;
        docStatus = -1;
        checkoutUser = "";
        date = new Date();
        this.docName = "";
    } 

    public long getSize()
    {
        return size;
    }
    public void setSize(long sz)
    {
        size = sz;
    }
    
    public boolean getWritable()
    {
        return writable;
    }
    
    public void setWritable(boolean wa)
    {
        writable = wa;
    }
    
    public int getDocStatus()
    {
        return docStatus;
    }
    
    public void setDocStatus(int status)
    {
        docStatus = status;
    }
    
    public String getCheckoutUser()
    {
        return checkoutUser;
    }
    
    public void setCheckoutUser(String user)
    {
        checkoutUser = user;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }
} // end class ExtMenu
