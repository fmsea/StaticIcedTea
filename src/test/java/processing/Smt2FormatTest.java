package processing;

import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
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
}
