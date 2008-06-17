package org.contineo.web.search;

import java.util.Date;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.searchengine.search.Result;
import org.contineo.core.searchengine.search.ResultInterface;
import org.contineo.core.searchengine.search.SearchOptions;
import org.contineo.core.security.ExtMenu;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.contineo.web.StyleBean;
import org.contineo.web.document.DocumentRecord;
import org.contineo.web.i18n.Messages;
import org.contineo.web.navigation.NavigationBean;
import org.contineo.web.navigation.PageContentBean;
import org.contineo.web.util.SnippetStripper;

public class DocumentResult extends DocumentRecord implements ResultInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 24242424L;

	protected static Log log = LogFactory.getLog(DocumentResult.class);

	private Result result;

	private boolean showPath;

	public DocumentResult(Result result) {
		super();
		this.result = result;
		initMenu(getMenuId());

		DocumentDAO docDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
		this.document = docDao.findByMenuId(getMenu().getMenuId());
	}

	protected void initMenu(int menuId) {
		try {
			MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);

			Menu m = mdao.findByPrimaryKey(menuId);
			ExtMenu myMenu = new ExtMenu(m);
			this.menu = myMenu;
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
		}
	}

	public Date getDate() {
		return result.getDate();
	}

	public int getDateCategory() {
		return result.getDateCategory();
	}

	public Integer getDocId() {
		return result.getDocId();
	}

	public int getDocType() {
		return result.getDocType();
	}

	public Integer getGreen() {
		return result.getGreen();
	}

	public String getIcon() {
		return result.getIcon();
	}

	public int getLengthCategory() {
		return result.getLengthCategory();
	}

	public int getMenuId() {
		return result.getMenuId();
	}

	public String getName() {
		return result.getName();
	}

	public String getPath() {
		return result.getPath();
	}

	public Integer getRed() {
		return result.getRed();
	}

	public int getSize() {
		return result.getSize();
	}

	public Date getSourceDate() {
		return result.getSourceDate();
	}

	/**
	 * Teturns the the summary properly escaped for the results page. The Lucene
	 * Hilights are preserved.
	 * 
	 * @see org.contineo.core.searchengine.search.ResultInterface#getSummary()
	 */
	public String getSummary() {
		String summary = result.getSummary();
		return SnippetStripper.strip(summary);
	}

	public String getType() {
		return result.getType();
	}

	public boolean isRelevant(SearchOptions arg0, String arg1) {
		return result.isRelevant(arg0, arg1);
	}

	/**
	 * Creates the context menu associated with this record
	 * 
	 * @see org.contineo.web.navigation.MenuBarBean#createMenuItems()
	 */
	protected void createMenuItems() {

		model.clear();

		ExtMenu menu = getMenu();

		contextMenu = createMenuItem(" ", "context-" + menu.getMenuId(), null, "#{entry.noaction}", null, StyleBean
				.getImagePath("options_small.png"), true, null, null);
		model.add(contextMenu);

		if (menu.getMenuType() == Menu.MENUTYPE_FILE) {
			contextMenu.getChildren().add(
					createMenuItem(Messages.getMessage("msg.jsp.versions"), "versions-" + menu.getMenuId(), null,
							"#{entry.versions}", null, StyleBean.getImagePath("versions.png"), true, "_blank", null));
			contextMenu.getChildren().add(
					createMenuItem(Messages.getMessage("msg.jsp.discuss"), "articles-" + menu.getMenuId(), null,
							"#{entry.articles}", null, StyleBean.getImagePath("comments.png"), true, "_blank", null));
			contextMenu.getChildren()
					.add(
							createMenuItem(Messages.getMessage("msg.jsp.sendasemail"),
									"sendasmail-" + menu.getMenuId(), null, "#{entry.sendAsEmail}", null, StyleBean
											.getImagePath("editmail.png"), true, "_blank", null));
			contextMenu.getChildren().add(
					createMenuItem(Messages.getMessage("msg.jsp.sendticket"), "sendticket-" + menu.getMenuId(), null,
							"#{entry.sendAsTicket}", null, StyleBean.getImagePath("ticket.png"), true, "_blank", null));
			contextMenu.getChildren().add(
					createMenuItem(Messages.getMessage("msg.jsp.foldercontent.info"), "info-" + menu.getMenuId(), null,
							"#{entry.info}", null, StyleBean.getImagePath("info.png"), true, "_blank", null));
			contextMenu.getChildren().add(
					createMenuItem(Messages.getMessage("msg.jsp.history"), "history-" + menu.getMenuId(), null,
							"#{entry.history}", null, StyleBean.getImagePath("history.png"), true, "_blank", null));
		}
	}

	public void showDocumentPath() {
		this.showPath = true;
	}

	public boolean isShowPath() {
		return showPath;
	}

	private void openDocumentsPage() {
		Application application = FacesContext.getCurrentInstance().getApplication();
		NavigationBean navigation = ((NavigationBean) application.createValueBinding("#{navigation}").getValue(
				FacesContext.getCurrentInstance()));
		MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
		Menu docMenu = menuDao.findByPrimaryKey(Menu.MENUID_DOCUMENTS);
		PageContentBean panel = new PageContentBean("m-" + docMenu.getMenuId(), "document/browse");
		panel.setContentTitle(Messages.getMessage(docMenu.getMenuText()));
		navigation.setSelectedPanel(panel);
	}

	public String info() {
		openDocumentsPage();
		return super.info();
	}

	public String history() {
		openDocumentsPage();
		return super.history();
	}

	public String sendAsEmail() {
		openDocumentsPage();
		return super.sendAsEmail();
	}

	public String sendAsTicket() {
		openDocumentsPage();
		return super.sendAsTicket();
	}

	public String articles() {
		openDocumentsPage();
		return super.articles();
	}

	public String versions() {
		openDocumentsPage();
		return super.versions();
	}
}