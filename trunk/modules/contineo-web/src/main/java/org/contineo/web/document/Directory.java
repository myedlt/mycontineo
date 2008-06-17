package org.contineo.web.document;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.contineo.web.SessionManagement;
import org.contineo.web.navigation.PageContentBean;

/**
 * Instances of this bean represents document directories to be displayed in the
 * navigation tree
 * 
 * @author Marco Meschieri
 * @version $Id: Directory.java,v 1.5 2006/08/27 10:25:36 marco Exp $
 * @since 3.0
 */
public class Directory extends PageContentBean {
	protected static Log log = LogFactory.getLog(Directory.class);

	private boolean selected = false;

	private int count = 0;

	private Boolean writeable = null;

	// True if all childs were loaded from db
	private boolean loaded = false;

	public Directory(Menu menu) {
		super(menu);
	}

	/**
	 * The number of contained documents
	 */
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public void onSelect(ActionEvent event) {

		// Documents record manager binding
		Application application = FacesContext.getCurrentInstance().getApplication();
		DocumentNavigation navigation = ((DocumentNavigation) application.createValueBinding("#{documentNavigation}")
				.getValue(FacesContext.getCurrentInstance()));
		navigation.selectDirectory(this);
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean isWriteable() {
		if (writeable == null) {
			MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
			writeable = new Boolean(mdao.isWriteEnable(getMenuId(), SessionManagement.getUsername()));
		}
		return writeable.booleanValue();
	}
}
