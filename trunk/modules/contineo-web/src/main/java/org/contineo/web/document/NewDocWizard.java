package org.contineo.web.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.doxter.Storer;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.core.text.AnalyzeText;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;
import org.contineo.web.navigation.PageContentBean;
import org.contineo.web.upload.InputFileBean;

import com.icesoft.faces.context.effects.JavascriptContext;

/**
 * Wizard that handled the creation of a new document.
 * 
 * @author Marco Meschieri
 * @version $Id: NewDocWizard.java,v 1.9 2006/09/04 15:48:35 marco Exp $
 * @since 3.0
 */
public class NewDocWizard {
	protected static Log log = LogFactory.getLog(NewDocWizard.class);

	private boolean showUpload = true;

	private DocumentNavigation documentNavigation;

	public boolean isShowUpload() {
		return showUpload;
	}

	/**
	 * Starts the upload process
	 */
	public String start() {
		documentNavigation.setSelectedPanel(new PageContentBean("uploadDocument"));

		// Remove the uploaded file, if one was uploaded
		Application application = FacesContext.getCurrentInstance().getApplication();
		InputFileBean inputFile = ((InputFileBean) application.createValueBinding("#{inputFile}").getValue(
				FacesContext.getCurrentInstance()));
		inputFile.deleteUploadDir();
		inputFile.reset();

		showUpload = true;

		return null;
	}

	/**
	 * Acquires the uploaded file and shows the edit form. Gets the file
	 * uploaded through the HTML form and extracts all necessary data like
	 * language, keywords, autor, etc. to fill the document form so that the
	 * user can still edit this data before finally storing the document in
	 * contineo.
	 */
	public String next() {
		showUpload = false;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		Application application = FacesContext.getCurrentInstance().getApplication();
		InputFileBean inputFile = ((InputFileBean) application.createValueBinding("#{inputFile}").getValue(
				FacesContext.getCurrentInstance()));
		DocumentEditForm docForm = ((DocumentEditForm) application.createValueBinding("#{documentForm}").getValue(
				FacesContext.getCurrentInstance()));
		docForm.reset();

		String[] groups = SessionManagement.getUser().getGroupNames();

		// Add the admin group if not specified
		boolean found = false;
		for (int i = 0; i < groups.length; i++) {
			if (groups[i].equals("admin"))
				found = true;
		}
		if (!found) {
			String[] tmp = new String[groups.length + 1];
			for (int i = 0; i < groups.length; i++)
				tmp[i] = groups[i];
			tmp[groups.length] = "admin";
			groups = tmp;
		}

		docForm.setMenuGroup(groups);

		if (SessionManagement.isValid()) {
			try {
				File file = inputFile.getFile();
				String documentLanguage = inputFile.getLanguage();

				// Get menuParent that called AddDocAction
				MenuDAO mdao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
				Menu parent = documentNavigation.getSelectedDir().getMenu();

				// Makes menuPath
				String menupath = new StringBuilder(parent.getMenuPath()).append("/").append(parent.getMenuId())
						.toString();
				int menuhier = parent.getMenuHier();

				// Makes new Menu
				Menu menu = new Menu();
				menu.setMenuParent(parent.getMenuId());
				menu.setMenuPath(menupath);
				menu.setMenuHier(menuhier++);

				// and stores it
				mdao.store(menu);

				// Gets file to upload name
				String filename = file.getName();

				// Stores the document in the repository
				SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
				String path = new StringBuilder(settings.getValue("docdir")).append("/").append(menupath).append("/")
						.append(String.valueOf(menu.getMenuId())).append("/").toString();
				String mpath = new StringBuilder(menupath).append("/").append(String.valueOf(menu.getMenuId()))
						.toString();

				// Get file to upload inputStream
				InputStream stream = new FileInputStream(file);
				Storer storer = (Storer) Context.getInstance().getBean(Storer.class);

				// stores it in folder
				try {
					storer.store(stream, mpath, filename, "1.0");
				} finally {
					if (stream != null) {
						stream.close();
					}
				}

				File f = new File(new StringBuilder(path).append(filename).toString());

				// Parses the file where it is already stored
				Parser parser = ParserFactory.getParser(f);
				String content = null;
				String name = "";
				String author = "";
				String keywords = "";

				// and gets some fields
				if (parser != null) {
					content = parser.getContent();
					name = parser.getTitle();
					author = parser.getAuthor();
					if (inputFile.isExtractKeywords())
						keywords = parser.getKeywords();
				}

				if (content == null) {
					content = "";
				}

				// source field got from web.xml.
				// This field is required for Lucene to work properly
				String source = (String) facesContext.getExternalContext().getApplicationMap().get("store");

				if (source == null) {
					source = settings.getValue("defaultSource");
				}

				// fills needed fields
				if ((name != null) && (name.length() > 0)) {
					docForm.setDocName(name);
				} else {
					int tmpInt = filename.lastIndexOf(".");

					if (tmpInt != -1) {
						docForm.setDocName(filename.substring(0, tmpInt));
					} else {
						docForm.setDocName(filename);
					}
				}

				docForm.setMenuParent(parent.getMenuId());
				docForm.setSource(source);

				if (author != null) {
					docForm.setSourceAuthor(author);
				}

				docForm.setSourceDate(new Date());
				docForm.setLanguage(documentLanguage);

				if (inputFile.isExtractKeywords()) {
					if ((keywords != null) && !keywords.trim().equals("")) {
						docForm.setKeywords(keywords);
					} else {
						AnalyzeText analyzer = new AnalyzeText();
						docForm.setKeywords(analyzer.getTerms(5, content.toString(), documentLanguage));
					}
				}

				docForm.setFilename(filename);
				docForm.setMenu(menu);

				// update the file size
				mdao.store(menu);
			} catch (Exception e) {
				String message = Messages.getMessage("errors.action.savedoc");
				log.error(message, e);
				Messages.addMessage(FacesMessage.SEVERITY_ERROR, message, message);
				showUpload = true;
			} finally {

			}

			// sometimes IE7 freezes the page and in these cases a refresh
			// solves the problem
			JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(), "window.location.reload(false);");

			return null;
		} else {
			return "login";
		}
	}

	public String abort() {
		documentNavigation.setSelectedPanel(new PageContentBean("documents"));

		// Remove the uploaded file, if one was uploaded
		Application application = FacesContext.getCurrentInstance().getApplication();
		InputFileBean inputFile = ((InputFileBean) application.createValueBinding("#{inputFile}").getValue(
				FacesContext.getCurrentInstance()));
		inputFile.deleteUploadDir();

		return null;
	}

	public String save() {
		Application application = FacesContext.getCurrentInstance().getApplication();
		DocumentEditForm documentForm = ((DocumentEditForm) application.createValueBinding("#{documentForm}").getValue(
				FacesContext.getCurrentInstance()));

		documentForm.save();
		documentNavigation.selectDirectory(documentNavigation.getSelectedDir());

		return abort();
	}

	public void setDocumentNavigation(DocumentNavigation documentNavigation) {
		this.documentNavigation = documentNavigation;
	}
}
