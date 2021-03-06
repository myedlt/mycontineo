package org.contineo.web.i18n;

import org.contineo.util.config.FacesConfigurator;

import org.contineo.web.util.Constants;

import java.text.MessageFormat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;


/**
 * A class for retrieval of localized messages. All bundles declared in
 * faces-config.xml are searched but in the opposite order they are declared in
 * the file. The first key match wins.
 *
 * @author Marco Meschieri
 * @version $Id:$
 * @since 3.0
 */
public class Messages extends AbstractMap<String, String> {
    /**
     * The list of bundles in which keys will be searched
     */
    private static List<String> bundles = new ArrayList<String>();

    public Messages() {
        // a static class
    }

    public static String getMessage(String key, Locale locale) {
        if (bundles.isEmpty()) {
            FacesConfigurator config = new FacesConfigurator();
            bundles = config.getBundles();
        }

        // Iterate over bundles in reverse order
        for (int i = bundles.size() - 1; i >= 0; i--) {
            String path = bundles.get(i);
            ResourceBundle bundle = ResourceBundle.getBundle(path, locale);

            try {
                return bundle.getString(key);
            } catch (MissingResourceException e) {
                // Continue
            }
        }

        return key;
    }

    public static String getMessage(String key) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Locale locale = (Locale) facesContext.getExternalContext()
                                             .getSessionMap()
                                             .get(Constants.LOCALE);

        if (locale == null) {
            locale = facesContext.getApplication().getDefaultLocale();
        }

        return getMessage(key, locale);
    }

    public static String getMessage(String key, String val0) {
        String msg = getMessage(key);

        return MessageFormat.format(msg, new Object[] { val0 });
    }

    public static void addLocalizedWarn(String message) {
        addWarn(Messages.getMessage(message));
    }

    public static void addWarn(String message) {
        addMessage(FacesMessage.SEVERITY_WARN, null, message, message);
    }

    public static void addLocalizedError(String message) {
        addError(Messages.getMessage(message));
    }

    public static void addError(String message) {
        addMessage(FacesMessage.SEVERITY_ERROR, null, message, message);
    }

    public static void addLocalizedInfo(String message) {
        addInfo(Messages.getMessage(message));
    }

    public static void addInfo(String message) {
        addMessage(FacesMessage.SEVERITY_INFO, null, message, message);
    }

    public static void addMessage(FacesMessage.Severity severity,
        String summary, String detail) {
        addMessage(severity, null, summary, detail);
    }

    /**
     * Adds a message in the jsf queue
     *
     * @param severity The severity level
     * @param clientId The componentID (null can be accepted)
     * @param summary The summary part(bundle key)
     * @param detail The detail part(bundle key)
     */
    public static void addMessage(FacesMessage.Severity severity,
        String clientId, String summary, String detail) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage(severity, summary, detail);
        facesContext.addMessage(clientId, message);
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return null;
    }

    @Override
    public String get(Object key) {
        return Messages.getMessage(key.toString());
    }
}
