package org.contineo.core.text.parser;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.ZipBean;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;

/**
 * Created on 10.12.2004
 */
public class ZABWParser implements Parser {
	private String content = "";

	protected static Log logger = LogFactory.getLog(ZABWParser.class);

	public void parse(File file) {
		try {
			String filename = file.getName();
			SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
			ZipBean.unzip(file.getAbsolutePath(), conf.getValue("userdir") + "unjar/", filename);

			File xmlfile = new File(conf.getValue("userdir") + "unjar/" + filename);
			XMLParser parser = new XMLParser();
			parser.parse(xmlfile);
			content = parser.getContent().toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getVersion()
	 */
	public String getVersion() {
		return "";
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getContent()
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getAuthor()
	 */
	public String getAuthor() {
		return "";
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getSourceDate()
	 */
	public String getSourceDate() {
		return "";
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getKeywords()
	 */
	public String getKeywords() {
		return "";
	}

	/**
	 * @see org.contineo.core.text.parser.Parser#getTitle()
	 */
	public String getTitle() {
		return "";
	}
}