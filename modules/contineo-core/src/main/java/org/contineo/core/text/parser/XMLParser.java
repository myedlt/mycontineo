package org.contineo.core.text.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author Michael Scholz
 */
public class XMLParser implements Parser {
	private String content = "";

	protected static Log logger = LogFactory.getLog(XMLParser.class);
	
	public void parse(File file) {
		StringBuffer buffer=new StringBuffer();
		try {
			InputStream in = new FileInputStream(file);
			BufferedInputStream reader = new BufferedInputStream(in);
			int ichar = 0;
			boolean istag = false;

			while ((ichar = reader.read()) != -1) {
				if (ichar == 60) {
					buffer.append((char) 32);
					istag = true;
				}

				if (!istag) {
					buffer.append((char) ichar);
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