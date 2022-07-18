package processing;

import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import util.ResourceFileUtility;

public class Smt2FormatTest extends FormatTest {

    @Test
    void testSMT2FormatWhenEmpty() {
        Reader r1 = new StringReader("");
        Reader r2 = new StringReader("");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals("", w1.toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatSimple() {
        Reader r1 = getReader("processing/format.simple.1.in");
        Reader r2 = getReader("processing/format.simple.2.in");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.simple.out"),
                                    w1.toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatUnbalanced() {
        Reader r1 = getReader("processing/format.unbalanced.1.in");
        Reader r2 = getReader("processing/format.unbalanced.2.in");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.unbalanced.out"),
                                    w1.toString());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatUnchanged() {
        Reader r1 = getReader("processing/format.unchanged.1.in");
        Reader r2 = getReader("processing/format.unchanged.2.in");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.unchanged.out"),
                                    w1.toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatIdentifiers() {
        Reader r1 = getReader("processing/format.identifiers.in");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2FormatIdentifiers(r1, w1);
            Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.identifiers.out"),
                                    w1.toString());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatQuotes() {
        Reader r1 = getReader("processing/format.quotes.1.in");
        Reader r2 = getReader("processing/format.quotes.2.in");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2FormatFull(r1, r2, w1);
            Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.quotes.out"),
                                    w1.toString());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatFull() {
        {
            Reader r1 = getReader("processing/format.full.simple.1.in");
            Reader r2 = getReader("processing/format.full.simple.2.in");
            Writer w1 = new StringWriter();
            try {
                Smt2Format.SMT2FormatFull(r1, r2, w1);
                Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.full.simple.out"),
                                        w1.toString());
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                Assertions.assertTrue(false);
            }
        }
    }

    @Test
    void testSMT2FormatStatementsSorted() {
        {
            Reader r1 = getReader("processing/format.full.sorted.1.in");
            Reader r2 = getReader("processing/format.full.sorted.2.in");
            Writer w1 = new StringWriter();
            try {
                Smt2Format.SMT2FormatFull(r1, r2, w1);
                Assertions.assertEquals(ResourceFileUtility.readResourcesFile("processing/format.full.sorted.out"),
                                        w1.toString());
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
                Assertions.assertTrue(false);
            }
        }
    }
}
