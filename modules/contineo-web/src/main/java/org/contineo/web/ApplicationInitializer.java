package org.contineo.web;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import org.springframework.util.Log4jConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Listener that initialises relevant system stuffs during application startup
 *
 * @author Alessandro Gasparini
 * @version $Id:$
 * @since 3.0
 */
public class ApplicationInitializer implements ServletContextListener {
    private static final String CONTINEO_APP_PLUGINSDIR = "contineo.app.pluginsdir";
    private static final String CONTINEO_APP_ROOTDIR = "contineo.app.rootdir";
    private static final String CONTINEO_HOME = "CONTINEO_HOME";
    private static final String CONTINEO_APP_PLUGINREGISTRY = "contineo.app.pluginregistry";
    private static final String WEB_INF_BOOT_PROPERTIES = "WEB-INF/boot.properties";

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        Log4jConfigurer.shutdownLogging();
    }

    public static Properties loadBootProperties(ServletContext context) {
        Properties boot = new Properties();

        try {
            boot.load(new FileInputStream(context.getRealPath(
                        WEB_INF_BOOT_PROPERTIES)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return boot;
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Properties boot = loadBootProperties(context);
        String contineoHome = boot.getProperty(CONTINEO_HOME);

        // replace system properties
        if (contineoHome.indexOf("$") != -1) {
            contineoHome = StrSubstitutor.replaceSystemProperties(contineoHome);
        }

        boot.setProperty(CONTINEO_HOME, initContineoHomePath(contineoHome));
        boot.setProperty(CONTINEO_APP_ROOTDIR, initRootPath(context));
        boot.setProperty(CONTINEO_APP_PLUGINSDIR, initPluginsPath(context));
        boot.setProperty(CONTINEO_APP_PLUGINREGISTRY, initPluginRegistry());

        try {
            String log4jPath = context.getRealPath("/WEB-INF/classes/log4j.xml");
            System.out.println("log4jPath = " + log4jPath);
            Log4jConfigurer.initLogging(log4jPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        saveBootProperties(boot, context);

        // Initialize plugins
        org.contineo.util.PluginRegistry.getInstance().init();
    }

    private String initPluginRegistry() {
        System.setProperty(CONTINEO_APP_PLUGINREGISTRY,
            "org.contineo.web.PluginRegistry");

        return "org.contineo.web.PluginRegistry";
    }

    public static void saveBootProperties(Properties boot,
        ServletContext context) {
        // Save properties for the next bootstrap
        try {
            boot.store(new FileOutputStream(context.getRealPath(
                        WEB_INF_BOOT_PROPERTIES)), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String initContineoHomePath(String contineoHome) {
        String homePath = StringUtils.replace(contineoHome, "\\", "/");
        homePath = StringUtils.removeEnd(homePath, "/");
        System.err.println("CONTINEO_HOME = " + homePath);
        System.setProperty(CONTINEO_HOME, homePath);

        return homePath;
    }

    protected String initRootPath(final ServletContext context) {
        String rootPath = StringUtils.replace(context.getRealPath(
                    StringUtils.EMPTY), "\\", "/");
        rootPath = StringUtils.removeEnd(rootPath, "/");
        System.setProperty(CONTINEO_APP_ROOTDIR, rootPath);

        return rootPath;
    }

    protected String initPluginsPath(final ServletContext context) {
        String pluginsPath = StringUtils.replace(context.getRealPath(
                    StringUtils.EMPTY), "\\", "/");
        pluginsPath = StringUtils.removeEnd(pluginsPath, "/");
        pluginsPath += "/WEB-INF/plugins";

        File dir = new File(pluginsPath);
        dir.mkdirs();
        dir.mkdir();
        System.setProperty(CONTINEO_APP_PLUGINSDIR, pluginsPath);

        return pluginsPath;
    }
}
