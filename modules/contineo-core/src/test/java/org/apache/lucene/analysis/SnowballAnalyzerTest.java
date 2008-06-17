package org.apache.lucene.analysis;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;

import org.contineo.core.text.analyze.Stopwords;

public class SnowballAnalyzerTest extends TestCase {

    private SnowballAnalyzer sbesAnal;

    public SnowballAnalyzerTest() {
        sbesAnal = new SnowballAnalyzer("Spanish", Stopwords.getStopwords("es"));
    }

    /**
     * A helper method that analizes a string
     * 
     * @param a the Analyzer to use
     * @param input an input String to analyze
     * @throws Exception in case an error occurs
     */
    private String[] getAnalysisResult(Analyzer a, String input) throws Exception {
        TokenStream ts = a.tokenStream("dummy", new StringReader(input));
        List<String> resultList = new ArrayList<String>();
        while (true) {
            Token token = ts.next();
            if (token == null)
                break;
            resultList.add(token.termText());
        }
        return resultList.toArray(new String[0]);
    }


    /**
     * @throws Exception
     */
    public void testSpanishAnalizers() throws Exception {

        System.out.println("##############################");

        String testLong = "Nuestra misión: crear una relación de confianza entre propietarios e inquilinos que comparten las mismas ganas de tener el éxito en sus alquileres.";
        System.out.println(testLong);

        String[] result2 = getAnalysisResult(sbesAnal, testLong);
        for (String token : result2) {
            System.out.println(token);
        }

        assertTrue(result2.length > 0);
    }

    /**
     * @throws Exception
     */
    public void testSpanishAnalizers2() throws Exception {

        System.out.println("##############################");

        String testLong = "La progresión de Internet, el desarrollo de las relaciones en la red que favorece, su capacidad a hacer encontrar sencillamente a personas que comparten \"algo\", hace evolucionar la idea de \"conocerse\" Ayer, \"se concocía\" a personas, por encuentros poco numerosos, en su ámbito y en su ciudad.";
        
        System.out.println(testLong);

        String[] result2 = getAnalysisResult(sbesAnal, testLong);
        for (String token : result2) {
            System.out.println(token);
        }

        // The 2 Arrays SHOULD BE EQUALS
        assertTrue(result2.length > 0);
    }
}
