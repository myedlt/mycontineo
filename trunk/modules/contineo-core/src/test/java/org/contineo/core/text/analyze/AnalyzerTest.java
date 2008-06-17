package org.contineo.core.text.analyze;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.TestCase;

public class AnalyzerTest extends TestCase {

	public void testAnalyzerStringInt() {
		Analyzer analyzer = new Analyzer("en", 4);
		assertNotNull(analyzer);
		assertEquals(4, analyzer.minlen);
		assertEquals("en", analyzer.getLanguage());

		analyzer = new Analyzer("de", 5);
		assertEquals(5, analyzer.minlen);
		assertEquals("de", analyzer.getLanguage());
	}

	public void testAnalizeItalian() throws Exception {
		Analyzer analyzer = new Analyzer("it", 4);
		// Text with a lot of words
		String textToAnalize = "Festival di Torino, Moretti rinuncia \"Vi lascio ai vostri rancori personali\""
				+ " Il regista era stato nominato direttore artistico. Ma gli ideatori hanno polemizzato perché "
				+ "l'organizzazione era stata affidata al Museo del Cinema. Una lettera per l'addio. Chiamparino: "
				+ "\"Spero che ci ripensi\"."
				+ " Colpo di scena nella querelle sulla direzione del Torino Film Festival. Il regista Nanni Moretti, che due "
				+ "giorni fa aveva accettato l'incarico offerto dagli enti locali e dal Museo Nazionale del Cinema, l'ente "
				+ "organizzatore dell'edizione 2007 in programma nel prossimo novembre, sbatte la porta e se ne va: "
				+ "\"Con molto dolore rinuncio all'incarico e vi lascio ai vostri problemi di metodo, ai contrasti "
				+ "procedurali, ai rancori personali\", dice in una nota affidata in serata all' Ansa."
				+ "Il sistema di archiviazione Contineo è sviluppato in Java";

		analyzer.analyze(textToAnalize);

		long wordCount = analyzer.getWordCount();
		System.out.println("wordCount = " + wordCount);
		assertTrue(wordCount > 0);

		// Check the content of wordtable
		Hashtable<String, WordEntry> wordtable = analyzer.wordtable;
		assertNotNull(wordtable);
		assertFalse(wordtable.isEmpty());

		System.out.println("size = " + wordtable.size());
		for (String key : wordtable.keySet()) {
			System.out.println(key);
		}

		Collection coll = analyzer.getTopWords(15);
		assertNotNull(coll);
		assertTrue(coll.size() == 15);

		int countWord01 = 0;
		int countWord02 = 0;
		// The top words must contains the stem "morett", the word appears 2
		// times in the text
		for (Iterator iter = coll.iterator(); iter.hasNext();) {
			Entry e = (Entry) iter.next();
			System.out.println(e.getNumber());
			System.out.println(e.getWord());
			System.out.println(e.getOriginWord());
			if (e.getWord().equals("morett"))
				countWord01 = e.getNumber();
			if (e.getWord().equals("rancor"))
				countWord02 = e.getNumber();
		}
		assertTrue(2 == countWord01);
		assertTrue(2 == countWord02);
	}

	public void testAnalizeEnglish() throws Exception {
		Analyzer analyzer = new Analyzer("en", 4);

		File file = new File(URLDecoder.decode(getClass().getClassLoader().getResource("homelidays_vision.txt")
				.getPath(), "UTF-8"));
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		StringBuffer content = new StringBuffer();
		int ichar = 0;
		while ((ichar = bis.read()) > 0) {
			content.append((char) ichar);
		}

		// Text with a lot of words
		String textToAnalize = content.toString();

		analyzer.analyze(textToAnalize);

		long wordCount = analyzer.getWordCount();
		System.out.println("wordCount = " + wordCount);
		assertTrue(wordCount > 0);

		// Check the content of wordtable
		Hashtable<String, WordEntry> wordtable = analyzer.wordtable;
		assertNotNull(wordtable);
		assertFalse(wordtable.isEmpty());

		System.out.println("size = " + wordtable.size());
		for (String key : wordtable.keySet()) {
			System.out.println(key);
		}

		Collection coll = analyzer.getTopWords(10);
		assertNotNull(coll);
		assertTrue(coll.size() == 10);

		int countWord01 = 0;
		int countWord02 = 0;
		// The top words must contains the stem "holiday", the word appears 5
		// times in the text
		for (Iterator iter = coll.iterator(); iter.hasNext();) {
			Entry e = (Entry) iter.next();
			System.out.println(e.getNumber());
			System.out.println(e.getWord());
			System.out.println(e.getOriginWord());
			if (e.getWord().equals("holiday"))
				countWord01 = e.getNumber();
			if (e.getWord().equals("accommod"))
				countWord02 = e.getNumber();
		}
		assertTrue(5 == countWord01);
		assertTrue(5 == countWord02);
	}

}
