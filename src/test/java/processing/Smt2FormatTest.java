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

public class Smt2FormatTest {

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
        Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "i1->(= i1 0)\n" +
                                     "7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
        Reader r2 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "i1->(= i1 0)\n" +
                                     "7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals("(echo \"6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\")\n" +
                                    "(echo \"i1\")\n(push)\n(assert (forall ((i1 Int))\n" +
                                    "(=> (= i1 0) (= i1 0))))\n(check-sat)\n(pop)\n(push)\n" +
                                    "(assert (forall ((i1 Int))\n(=> (= i1 0) (= i1 0))))\n" +
                                    "(check-sat)\n(pop)\n" +
                                    "(echo \"7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\")\n" +
                                    "(echo \"b2\")\n(push)\n(assert (forall ((b2 Int))\n" +
                                    "(=> (and (< b2 5) (> b2 0) (>= b2 2)) (and (< b2 5) (> b2 0) (>= b2 2)))))\n" +
                                    "(check-sat)\n(pop)\n(push)\n(assert (forall ((b2 Int))\n" +
                                    "(=> (and (< b2 5) (> b2 0) (>= b2 2)) (and (< b2 5) (> b2 0) (>= b2 2)))))\n" +
                                    "(check-sat)\n(pop)\n",
                                    w1.toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatUnbalanced() {
        Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "i1->(= i1 0)\n" +
                                     "7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
        Reader r2 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "i1->(or (= i1 0) (>= i2 i1))\n" +
                                     "i2->(and (<= i1 i2) (= i2 3))\n" +
                                     "7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals("(echo \"6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\")\n" +
                                    "(echo \"i1\")\n(push)\n(assert (forall ((i1 Int)(i2 Int))\n" +
                                    "(=> (= i1 0) (or (= i1 0) (>= i2 i1)))))\n(check-sat)\n(pop)\n(push)\n" +
                                    "(assert (forall ((i1 Int)(i2 Int))\n(=> (or (= i1 0) (>= i2 i1)) (= i1 0))))\n" +
                                    "(check-sat)\n(pop)\n" +
                                    "(echo \"i2\")\n" +
                                    "(push)\n(assert (forall ((i1 Int)(i2 Int))\n" +
                                    "(=> (= 0 0) (and (<= i1 i2) (= i2 3)))))\n" +
                                    "(check-sat)\n(pop)\n(push)\n" +
                                    "(assert (forall ((i1 Int)(i2 Int))\n" +
                                    "(=> (and (<= i1 i2) (= i2 3)) (= 0 0))))\n" +
                                    "(check-sat)\n(pop)\n" +
                                    "(echo \"7 b2 = 2:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\")\n" +
                                    "(echo \"b2\")\n(push)\n(assert (forall ((b2 Int))\n" +
                                    "(=> (and (< b2 5) (> b2 0) (>= b2 2)) (and (< b2 5) (> b2 0) (>= b2 2)))))\n" +
                                    "(check-sat)\n(pop)\n(push)\n(assert (forall ((b2 Int))\n" +
                                    "(=> (and (< b2 5) (> b2 0) (>= b2 2)) (and (< b2 5) (> b2 0) (>= b2 2)))))\n" +
                                    "(check-sat)\n(pop)\n",
                                    w1.toString());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatUnchanged() {
        Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n" +
                                     "i1->(= i1 0)\n");
        Reader r2 = new StringReader("6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\n");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2Format(r1, r2, w1);
            Assertions.assertEquals("(echo \"6 i1 = 0:<test.BallonFactory: java.util.ArrayList getLines(java.awt.FontMetrics,java.lang.String,double,int)>\")\n" +
                                    "(echo \"i1\")\n(push)\n(assert (forall ((i1 Int))\n" +
                                    "(=> (= i1 0) (= 0 0))))\n(check-sat)\n(pop)\n(push)\n" +
                                    "(assert (forall ((i1 Int))\n(=> (= 0 0) (= i1 0))))\n" +
                                    "(check-sat)\n(pop)\n",
                                    w1.toString());
        } catch (IOException e) {
            e.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }

    @Test
    void testSMT2FormatIdentifiers() {
        Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory>\n" +
                                     "i1->(= i1 0)\n" +
                                     "i1f->(or (<= i1 0) (> i1 0))\n" +
                                     "b2->(or (<= b2 0) (> b2 0))\n" +
                                     "b6->(and (<= b6 (+ i1 3))\n" +
                                     "         (<= b6 (+ b2 4)))\n" +
                                     "7 b2 = 2:<test.BallonFactory>\n" +
                                     "b2->(and (< b2 5)\n" +
                                     "         (> b2 0)\n" +
                                     "         (>= b2 i4))\n");
        Writer w1 = new StringWriter();
        try {
            Smt2Format.SMT2FormatIdentifiers(r1, w1);
            Assertions.assertEquals(Stream.of("7 b2 = 2:<test.BallonFactory>\tfall\tb2\ti4",
                                              "7 b2 = 2:<test.BallonFactory>\tbranch",
                                              "6 i1 = 0:<test.BallonFactory>\tfall\tb2\tb6\ti1",
                                              "6 i1 = 0:<test.BallonFactory>\tbranch\ti1",
                                              "")
                                    .collect(Collectors.joining("\n")),
                                    w1.toString());
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            Assertions.assertTrue(false);
        }
    }
}
