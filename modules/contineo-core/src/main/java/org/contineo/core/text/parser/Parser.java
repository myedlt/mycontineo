package org.contineo.core.text.parser;

import java.io.File;

/**
 * @author Michael Scholz
 */
public interface Parser {
	
	public String getVersion();

	public String getContent();

	public String getAuthor();

	public String getSourceDate();

	public String getKeywords();

	public String getTitle();
	
	public void parse(File file);
}