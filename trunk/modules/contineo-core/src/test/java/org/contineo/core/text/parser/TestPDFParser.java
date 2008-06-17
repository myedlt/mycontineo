package org.contineo.core.text.parser;

import java.io.File;

/**
 * Created on 02.01.2005
 * 
 * @author Michael Scholz
 */
public class TestPDFParser {

    public TestPDFParser() {
    }

    public void test() {
        try {
            File file = new File("E:/test2.pdf");
            PDFParser parser = new PDFParser();
            parser.parse(file);
            System.out.println(parser.getContent().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TestPDFParser tester = new TestPDFParser();
        tester.test();
    }
}
