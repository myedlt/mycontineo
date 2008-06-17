package org.contineo.core.text.analyze;

import junit.framework.TestCase;

public class StemmerTest extends TestCase {

    public void testItalianStemmer() throws Exception {
        Stemmer stemmer = new Stemmer("it");

        String source = "conoscenza";
        String dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("conoscent", dest);

        source = "proposti";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("propost", dest);

        source = "possibile";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("possibil", dest);

        source = "collettività";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("collett", dest);
        
        source = "java";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("jav", dest);
    }
    
    public void testEnglishStemmer() throws Exception {
        Stemmer stemmer = new Stemmer("en");

        String source = "designing";
        String dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("design", dest);

        source = "tables";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("tabl", dest);

        source = "bulleted";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("bullet", dest);

        source = "surprising";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("surpris", dest);
    }    

    public void testSpanishStemmer() throws Exception {
        Stemmer stemmer = new Stemmer("es");
        // Nuestra misión: crear una relación de confianza entre propietarios e
        // inquilinos que comparten las mismas ganas de tener el éxito en sus
        // alquileres.

        String source = "Nuestra";
        String dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("nuestr", dest);

        source = "misión";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("mision", dest);

        source = "relación";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("relacion", dest);

        source = "confianza";
        dest = stemmer.stem(source);
        System.out.println(dest);
        assertEquals("confianz", dest);
    }

}
