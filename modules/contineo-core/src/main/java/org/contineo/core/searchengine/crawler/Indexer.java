package org.contineo.core.searchengine.crawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.contineo.core.i18n.Language;
import org.contineo.core.i18n.LanguageManager;
import org.contineo.core.searchengine.util.SquareSimilarity;
import org.contineo.core.text.AnalyzeText;
import org.contineo.core.text.parser.Parser;
import org.contineo.core.text.parser.ParserFactory;
import org.contineo.util.config.SettingsConfig;

/**
 * Class for indexing files and maintaining indexes.
 * 
 * @author Michael Scholz, Marco Meschieri, Alessandro Gasparini
 */
public class Indexer {

	protected static Log log = LogFactory.getLog(Indexer.class);

	private SettingsConfig settingsConfig;

	private Indexer() {
	}

	public void setSettingsConfig(SettingsConfig settingsConfig) {
		this.settingsConfig = settingsConfig;
	}

	public synchronized int addFile(File file, org.contineo.core.document.Document document, String content,
			String language) throws Exception {

		String name = file.getName();
		int testversion = -1;
		int result = -1;
		name = name.substring(name.lastIndexOf(".") + 1);

		try {
			testversion = Integer.parseInt(name);
		} catch (Exception e) {
		}

		if (testversion == -1) {
			LuceneDocument lDoc = new LuceneDocument(document);

			try {
				log.info("addFile: " + file.toString());

				Document doc = lDoc.getDocument(file, content);
				result = addDocument(doc, language);
			} catch (Exception e) {
				log.error("Exception addFile: " + e.getLocalizedMessage(), e);
			}

			try {
				AnalyzeText aText = new AnalyzeText();
				aText.storeTerms(document.getMenuId(), content.toString(), language);
			} catch (Exception e) {
				log.error("Exception analyzing File: " + e.getLocalizedMessage(), e);
			}
		}

		return result;
	}

	/**
	 * Adds a LuceneDocument to the index.
	 */
	private int addDocument(Document doc, String iso639_2) {
		
		String indexdir = settingsConfig.getValue("indexdir");
		
		Language language = LanguageManager.getInstance().getLanguage(iso639_2);
		Analyzer analyzer = LuceneAnalyzerFactory.getAnalyzer(language.getLanguage());

		IndexWriter writer = null;
		try {			
			File indexPath = new File(indexdir, language.getIndex());
			writer = new IndexWriter(indexPath, analyzer, false);
			writer.setSimilarity(new SquareSimilarity());
			writer.addDocument(doc);
			writer.optimize();
			return writer.docCount() - 1;
		} catch (Exception e) {
			log.error("Exception adding Document to Lucene index: " + indexdir + ", " + e.getMessage(), e);
		} finally {
			if (writer != null)
				try {
					writer.close();
				} catch (Exception e) {
					log.error("Error closing index: " + language.getIndex() + ", " + e.getMessage(), e);
				}
		}
		
		return -1;
	}

	/**
	 * Adds all documents of a given directory to the index of the search
	 * engine.
	 * 
	 * @param file
	 *            Path of the directory.
	 * @param doc
	 *            The document that we want to add
	 * @throws Exception
	 */
	public synchronized void addDirectory(File file, org.contineo.core.document.Document doc) throws Exception {

		if (file.isDirectory()) {
			String[] subitems = file.list();

			for (int i = 0; i < subitems.length; i++) {
				addDirectory(new File(file, subitems[i]), doc);
			}
		} else {
			try {
				Parser parser = ParserFactory.getParser(file);
				if (parser == null) {
					return;
				}

				String content = parser.getContent();

				String language = doc.getLanguage();
				if (StringUtils.isEmpty(language)) {
					language = "en";
				}

				if (log.isInfoEnabled()) {
					log.info("addDirectory " + doc.getDocId() + " " + doc.getDocName() + " " + doc.getDocVersion()
							+ " " + doc.getDocDate() + " " + doc.getDocPublisher() + " " + doc.getDocStatus() + " "
							+ doc.getSource() + " " + doc.getSourceAuthor());
				}
				addFile(file, doc, content, language);
			} catch (Exception e) {
				log.error("addDirectory " + e.getMessage(), e);
			}
		}
	}

	/**
	 * Launch optimization on a single Lucene Index identified by the language
	 */
	protected synchronized void optimize(Language language) {
		
		String indexdir = settingsConfig.getValue("indexdir");

		try {
			Analyzer analyzer = LuceneAnalyzerFactory.getAnalyzer(language.getLanguage());
			File indexPath = new File(indexdir, language.getIndex());
			IndexWriter writer = new IndexWriter(indexPath, analyzer, false);
			writer.optimize();
			writer.close();
		} catch (Exception e) {
			log.error("optimize " + e.getMessage(), e);
		}
	}

	/**
	 * Launch optimization on all the Lucene Indexes
	 */
	public synchronized void optimize() {
		log.warn("Started optimization for all indexes");

		String indexdir = settingsConfig.getValue("indexdir");

		try {			
			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				Analyzer analyzer = LuceneAnalyzerFactory.getAnalyzer(language.getLanguage());
				File indexPath = new File(indexdir, language.getIndex());
				IndexWriter writer = new IndexWriter(indexPath, analyzer, false);
				writer.optimize();
				writer.close();
			}
		} catch (Exception e) {
			log.error("optimize " + e.getMessage(), e);
		}

		log.warn("Finished optimization for all indexes");
	}

	/**
	 * Deletes the entries of a document in the index of the search engine then
	 * launch optimization on the language specific index
	 * 
	 * @param menuId -
	 *            MenuID of the document.
	 * @param language -
	 *            Language of the document.
	 */
	public synchronized void deleteFile(String menuId, String iso639_2) {

		String indexdir = settingsConfig.getValue("indexdir");

		Language language = LanguageManager.getInstance().getLanguage(iso639_2);
		File indexPath = new File(indexdir, language.getIndex());

		try {
			IndexReader reader = IndexReader.open(indexPath);
			reader.deleteDocuments(new Term("menuId", menuId));
			reader.close();
			optimize(language);
		} catch (IOException ioe) {
			log.error("deleteFile " + ioe.getMessage(), ioe);
		}
	}

	public Document getDocument(int luceneid) {

		String indexdir = settingsConfig.getValue("indexdir");

		try {
			List<IndexReader> readerList = new ArrayList<IndexReader>();

			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				File indexPath = new File(indexdir, language.getIndex());
				IndexReader ir = IndexReader.open(indexPath);
				readerList.add(ir);
			}

			IndexReader[] readers = (IndexReader[]) readerList.toArray(new IndexReader[0]);

			MultiReader reader = new MultiReader(readers);
			Document doc = reader.document(luceneid);
			reader.close();

			return doc;
		} catch (Exception e) {
			log.error("getDocument " + e.getMessage(), e);

			return null;
		}
	}

	/**
	 * This method can unlock a locked index.
	 */
	public synchronized void unlock() {

		String indexdir = settingsConfig.getValue("indexdir");

		try {
			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				File indexPath = new File(indexdir, language.getIndex());

				FSDirectory fsindexdir = FSDirectory.getDirectory(indexPath);
				IndexReader ir = IndexReader.open(fsindexdir);
				IndexReader.unlock(fsindexdir);
				ir.close();
			}
		} catch (Exception e) {
			log.error("unlock " + e.getMessage(), e);
		}
	}

	public boolean isLocked() {

		boolean result = false;
		String indexdir = settingsConfig.getValue("indexdir");

		try {
			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				File indexPath = new File(indexdir, language.getIndex());
				FSDirectory fsindexdir = FSDirectory.getDirectory(indexPath);
				IndexReader ir = null;
				try {
					ir = IndexReader.open(fsindexdir);
					if (IndexReader.isLocked(fsindexdir)) {
						result = true;
						break;
					}
				} finally {
					if (ir != null)
						ir.close();
				}
			}
		} catch (Exception e) {
			log.error("isLocked " + e.getMessage(), e);
		}

		return result;
	}

	/**
	 * Returns the number of indexed documents in all indexes. Used for
	 * statistical output.
	 */
	public int getCount() {

		int count = 0;
		String indexdir = settingsConfig.getValue("indexdir");

		try {
			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				File indexPath = new File(indexdir, language.getIndex());
				IndexReader ir = IndexReader.open(indexPath);
				count += ir.numDocs();
				ir.close();
			}
		} catch (Exception e) {
			log.error("getCount " + e.getMessage(), e);
		}

		return count;
	}

	/**
	 * Create all indexes (one per language)
	 */
	public void createIndexes() {
		
		String indexdir = settingsConfig.getValue("indexdir");
		
		try {
			// Get languages from LanguageManager
			Collection<Language> languages = LanguageManager.getInstance().getLanguages();
			for (Language language : languages) {
				File indexPath = new File(indexdir, language.getIndex());
				createIndex(indexPath, language.getLanguage());
			}
		} catch (Exception e) {
			log.error("createIndexes " + e.getMessage(), e);
		}
	}

	public static void createIndex(File indexPath, String iso639_2) throws CorruptIndexException, LockObtainFailedException, IOException {
		new IndexWriter(indexPath, LuceneAnalyzerFactory.getAnalyzer(iso639_2), true);
	}

}