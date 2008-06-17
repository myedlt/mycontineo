package org.contineo.util.config;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

/**
 * Configurator for the web.xml file
 * 
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class WebConfigurator extends XMLBean {
	public WebConfigurator() {
		super(System.getProperty("contineo.app.rootdir") + "/WEB-INF/web.xml");
	}

	@SuppressWarnings("unchecked")
	public void setDefaultSkin(String skin) {
		// Retrieve the <application> element
		List params = getRootElement().getChildren("context-param", getRootElement().getNamespace());
		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			Element param = (Element) iterator.next();
			Element name = param.getChild("param-name", param.getNamespace());
			if ("xkins.defaultSkin".equals(name.getText())) {
				Element value = param.getChild("param-value", param.getNamespace());
				value.setText(skin);
				writeXMLDoc();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setRelease(String release) {
		// Retrieve the <application> element
		List params = getRootElement().getChildren("context-param", getRootElement().getNamespace());
		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			Element param = (Element) iterator.next();
			Element name = param.getChild("param-name", param.getNamespace());
			if ("release".equals(name.getText())) {
				Element value = param.getChild("param-value", param.getNamespace());
				value.setText(release);
				writeXMLDoc();
			}
		}
	}

	public String getRelease() {
		// Retrieve the <application> element
		List params = getRootElement().getChildren("context-param", getRootElement().getNamespace());
		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			Element param = (Element) iterator.next();
			Element name = param.getChild("param-name", param.getNamespace());
			if ("release".equals(name.getText())) {
				Element value = param.getChild("param-value", param.getNamespace());
				return value.getText();
			}
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public void setStore(String store) {
		// Retrieve the <application> element
		List params = getRootElement().getChildren("context-param", getRootElement().getNamespace());
		for (Iterator iterator = params.iterator(); iterator.hasNext();) {
			Element param = (Element) iterator.next();
			Element name = param.getChild("param-name", param.getNamespace());
			if ("store".equals(name.getText())) {
				Element value = param.getChild("param-value", param.getNamespace());
				value.setText(store);
				writeXMLDoc();
			}
		}
	}

	public void addServletMapping(String servlet, String pattern) {
		// Search for the specified mapping
		List mappings = getRootElement().getChildren("servlet-mapping", getRootElement().getNamespace());
		for (Iterator iterator = mappings.iterator(); iterator.hasNext();) {
			Element elem = (Element) iterator.next();
			Element servletName = elem.getChild("servlet-name", elem.getNamespace());
			if (servletName.getText().equals(servlet)) {
				Element url = elem.getChild("url-pattern", elem.getNamespace());
				if (url.getText().equals(pattern)){
					//The mapping already exists
					return;
				}
			}
		}

		// Retrieve the last <servlet-mapping> element
		Element lastMapping = (Element) mappings.get(mappings.size() - 1);

		List children = getRootElement().getChildren();
		// Find the index of the element to add the new element after.
		int index = children.indexOf(lastMapping);

		// Prepare the new mapping
		Element servletMapping = new Element("servlet-mapping", getRootElement().getNamespace());
		Element servletName = new Element("servlet-name", getRootElement().getNamespace());
		servletName.setText(servlet);
		Element servletPattern = new Element("url-pattern", getRootElement().getNamespace());
		servletPattern.setText(pattern);
		servletMapping.addContent("\n ");
		servletMapping.addContent(servletName);
		servletMapping.addContent("\n ");
		servletMapping.addContent(servletPattern);
		servletMapping.addContent("\n ");
		
		// Add the new element to the next index along.
		// This does cover the case where indexOf returned -1.
		children.add(index + 1, servletMapping);
		writeXMLDoc();
	}

	public void setDisplayName(String displayName) {
		// Retrieve the <display-name> element
		Element element = getRootElement().getChild("display-name", getRootElement().getNamespace());
		element.setText(displayName);
		writeXMLDoc();
	}

	public String getDisplayName() {
		// Retrieve the <display-name> element
		Element element = getRootElement().getChild("display-name", getRootElement().getNamespace());
		return element.getText();
	}

	public void setDescription(String description) {
		// Retrieve the <display-name> element
		Element element = getRootElement().getChild("description", getRootElement().getNamespace());
		element.setText(description);
		writeXMLDoc();
	}
}