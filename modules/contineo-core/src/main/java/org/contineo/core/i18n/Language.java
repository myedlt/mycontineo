package org.contineo.core.i18n;

import java.util.Locale;

public class Language {

	private Locale locale;
	private String index;
	
	public Language(Locale locale) {
		this.locale = locale;
		this.index = locale.getDisplayLanguage(Locale.SIMPLIFIED_CHINESE).toLowerCase();
	}

	public Locale getLocale() {
		return locale;
	}
	
	public String getLanguage() {
		return locale.getLanguage();
	}
	
	public String getDisplayLanguage() {
		return locale.getDisplayLanguage();
	}

	public String getIndex() {
		return index;
	}
	
}
