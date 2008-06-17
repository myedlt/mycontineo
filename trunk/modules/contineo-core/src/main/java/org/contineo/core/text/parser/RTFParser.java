package org.contineo.core.text.parser;

import java.io.File;
import java.io.FileInputStream;

import javax.swing.JEditorPane;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Michael Scholz
 */
public class RTFParser implements Parser {
	private String content = "";

	protected static Log logger = LogFactory.getLog(RTFParser.class);

	public void parse(File file) {
		try {
			RTFEditorKit rtf = new RTFEditorKit();
			JEditorPane editor = new JEditorPane();
			editor.setEditorKit(rtf);

			FileInputStream fis = new FileInputStream(file);
			rtf.read(fis, editor.getDocument(), 0);

			content = editor.getDocument().getText(0, editor.getDocument().getLength());
			fis.close();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
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