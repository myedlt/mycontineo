package org.contineo.core.text.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.JarBean;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;

/**
 * @author Michael Scholz
 */
public class SXParser implements Parser {
	
	private String content = new String();

	protected static Log logger = LogFactory.getLog(SXParser.class);

	public void parse(File file) {
		StringBuffer buffer=new StringBuffer();
		try {
			SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
			logger.info("conf = " + conf);
			
			logger.info("sxcontent = " + conf.getValue("sxcontent"));
			JarBean.unjar(file.getAbsolutePath(), conf.getValue("userdir") + "unjar/", conf.getValue("sxcontent"));

			File xmlfile = new File(conf.getValue("userdir") + "unjar/" + conf.getValue("sxcontent"));
			InputStream in = new FileInputStream(xmlfile);
			BufferedInputStream reader = new BufferedInputStream(in);
			int ichar = 0;
			boolean istag = false;
			boolean isspec = false;

			while ((ichar = reader.read()) != -1) {
				if (ichar == 60) {
					buffer.append((char) 32);
					istag = true;
				}

				if (!istag) {
					if (ichar == 195) {
						isspec = true;
					} else {
						if (isspec) {
							switch (ichar) {
							case 132: {
								buffer.append('Ä');
								break;
							}

							case 164: {
								buffer.append('ä');
								break;
							}

							case 150: {
								buffer.append('Ö');
								break;
							}

							case 182: {
								buffer.append('ö');
								break;
							}

							case 156: {
								buffer.append('Ü');
								break;
							}

							case 188: {
								buffer.append('ü');
								break;
							}

							case 159: {
								buffer.append('ß');
								break;
							}
							}

							isspec = false;
						} else {
							buffer.append((char) ichar);
						}
					}
				}

				if (ichar == 62) {
					istag = false;
				}
			}

			in.close();
			reader.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		content=buffer.toString();
	}

	public String getContent() {
		return content;
	}

	public String getVersion() {
		return "";
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