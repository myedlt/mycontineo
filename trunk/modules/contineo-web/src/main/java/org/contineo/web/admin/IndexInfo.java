package org.contineo.web.admin;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.contineo.core.FileBean;
import org.contineo.core.document.Document;
import org.contineo.core.document.dao.DocumentDAO;
import org.contineo.core.searchengine.crawler.Indexer;
import org.contineo.core.security.Menu;
import org.contineo.core.security.dao.MenuDAO;
import org.contineo.util.Context;
import org.contineo.util.config.SettingsConfig;
import org.contineo.web.SessionManagement;
import org.contineo.web.i18n.Messages;

/**
 * 
 * @author Michael Scholz
 */
public class IndexInfo {
	protected static Log log = LogFactory.getLog(IndexInfo.class);

	private Indexer indexer;

	public IndexInfo() {
		indexer = (Indexer) Context.getInstance().getBean(Indexer.class);
	}

	public String getIndexDir() {
		SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
		return conf.getValue("indexdir");
	}

	public String unlock() {
		if (SessionManagement.isValid()) {
			try {
				Indexer indexer = (Indexer) Context.getInstance().getBean(Indexer.class);
				indexer.unlock();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addError(e.getMessage());
			}
		} else {
			return "login";
		}

		return null;
	}

	/**
	 * Recreated all indexes in a new Thread
	 */
	public String recreate() {
		if (SessionManagement.isValid()) {
			final SettingsConfig conf = (SettingsConfig) Context.getInstance().getBean(SettingsConfig.class);
			try {
				String path = conf.getValue("indexdir");

				if (!path.endsWith(File.pathSeparator)) {
					path += "/";
				}

				// Delete all index directories(one per language)
				FileBean.deleteDir(path + "english/");
				FileBean.deleteDir(path + "french/");
				FileBean.deleteDir(path + "german/");
				FileBean.deleteDir(path + "spanish/");
				FileBean.deleteDir(path + "italian/");

				// Create all index directories(one per language)
				FileBean.createDir(path + "english/");
				FileBean.createDir(path + "french/");
				FileBean.createDir(path + "german/");
				FileBean.createDir(path + "spanish/");
				FileBean.createDir(path + "italian/");

				Indexer indexer = (Indexer) Context.getInstance().getBean(Indexer.class);
				indexer.createIndexes();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				Messages.addError(e.getMessage());
			}

			Runnable task = new Runnable() {
				public void run() {
					try {
						MenuDAO menuDao = (MenuDAO) Context.getInstance().getBean(MenuDAO.class);
						DocumentDAO documentDao = (DocumentDAO) Context.getInstance().getBean(DocumentDAO.class);
						Collection documents = documentDao.findAll();
						Iterator iter = documents.iterator();
						Indexer indexer = (Indexer) Context.getInstance().getBean(Indexer.class);

						while (iter.hasNext()) {
							Document document = (Document) iter.next();
							Menu menu = menuDao.findByPrimaryKey(document.getMenuId());
							String dir = conf.getValue("docdir") + "/";
							dir += (menu.getMenuPath() + "/" + menu.getMenuId());
							indexer.addDirectory(new File(dir), document);
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
						Messages.addError(e.getMessage());
					}
				}
			};

			Thread recreateThread = new Thread(task);
			recreateThread.start();
		} else {
			return "login";
		}

		return null;
	}

	public int getDocCount() {
		return indexer.getCount();
	}

	public boolean getLocked() {
		return indexer.isLocked();
	}
}
