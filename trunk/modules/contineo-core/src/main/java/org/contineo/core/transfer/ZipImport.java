package org.contineo.core.transfer;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.ZipBean;
import org.contineo.core.document.CheckinDocUtil;
import org.contineo.core.document.Document;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.security.Menu;
import org.contineo.core.text.AnalyzeText;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;

/**
 * Created on 16.12.2004
 * 
 * @author micha
 * @author Sebastian Stein
 */
public class ZipImport {

	private String username;

	private String language;

	protected static Log logger = LogFactory.getLog(ZipImport.class);

	private boolean extractKeywords = true;

	public ZipImport() {
		username = "";
		language = "";
		extractKeywords = true;
	}

	public boolean isExtractKeywords() {
		return extractKeywords;
	}

	public void setExtractKeywords(boolean extractKeywords) {
		this.extractKeywords = extractKeywords;
	}

	public void process(File zipsource, String language, Menu parent, String user) {

		this.username = user;
		this.language = language;

		SettingsConfig settings = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		String userpath = settings.getValue("userdir");

		if (!userpath.endsWith(File.pathSeparator)) {
			userpath += File.pathSeparator;
		}

		userpath += username + File.pathSeparator + "unzip";

		if (FileBean.exists(userpath)) {
			FileBean.deleteDir(userpath);
		}

		FileBean.createDir(userpath);
		ZipBean.unzip(zipsource.getPath(), userpath);

		File file = new File(userpath);
		File[] files = file.listFiles();

		for (int i = 0; i < files.length; i++) {
			addEntry(files[i], parent);
		}

		FileBean.deleteDir(userpath);
	}

	public void process(String zipsource, String language, Menu parent, String user) {
		File srcfile = new File(zipsource);
		process(srcfile, language, parent, user);
	}

	/**
	 * Stores a file in the repository of contineo and inserts some information
	 * in the database of contineo (menu, document, version, history,
	 * searchdocument).
	 * 
	 * @param file
	 * @param parent
	 * @param language Two characters language of the file to add
	 */
	protected void addEntry(File file, Menu parent) {
		try {
			String menuName = file.getName();
			if (file.isDirectory()) { // creates a contineo folder
				Menu menu = CheckinDocUtil.createFolder(parent, menuName);

				File[] files = file.listFiles();

				for (int i = 0; i < files.length; i++) {
					addEntry(files[i], menu);
				}
			} else { 
				// creates a contineo document
				Menu menu = CheckinDocUtil.createDocument(file, parent, username, language);

				if (extractKeywords) {
					// also extract keywords and save on document
					DocumentDAO ddao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
					Document document = ddao.findByMenuId(menu.getMenuId());
					Parser parser = ParserFactory.getParser(file);
					parser.parse(file);
					String words = parser.getKeywords();
					if (StringUtils.isEmpty(words)) {
						AnalyzeText analyzer = new AnalyzeText();
						words = analyzer.getTerms(5, parser.getContent(), document.getLanguage());
					}
					Set<String> keywords = ddao.toKeywords(words);
					document.setKeywords(keywords);
					ddao.store(document);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}