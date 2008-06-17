package org.contineo.util.config;

import org.contineo.util.config.XMLBean;
import org.jdom.Element;

/**
 * @author Michael Scholz
 */
public class DBMSConfigurator {

	private XMLBean xml;

	/** Creates a new instance of DBMSConfigurator */
	public DBMSConfigurator() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		xml = new XMLBean(loader.getResource("org/contineo/core/dbms.xml"));
	}

	public String getAttribute(String name, String attr) {
		Element element = xml.getChild("db", "name", name);
		return xml.getAttributeValue(element, attr);
	}
}
