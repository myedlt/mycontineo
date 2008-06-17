package org.contineo.core.text.parser;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.RecordFormatException;

/**
 * Parser for Office 2003 worksheets
 * 
 * @author Michael Scholz
 */
public class XLSParser implements Parser {
	protected static Log log = LogFactory.getLog(XLSParser.class);

	private String content = "";

	public String getContent() {
		return content;
	}

	public String getVersion() {
		return "";
	}

	public void parse(File file) {
		XLSRecordListener listener = new XLSRecordListener();
		try {
			content = listener.parse(file).toString();
		} catch (RecordFormatException re) {
			log.error("Encrypted document, unable to decrypt");
		}
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