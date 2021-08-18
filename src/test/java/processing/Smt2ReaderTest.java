package processing;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class Smt2ReaderTest {

    @Test
    void testGetIdentifiers() {
        {
            String smt = "(= i0 0)";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("All identifiers are identified",
                      () -> assertTrue(result.contains("i0")),
                      () -> assertFalse(result.contains("0")));
        }

        {
            String smt = "(= i0 61)";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("All identifiers are identified",
                      () -> assertTrue(result.contains("i0")),
                      () -> assertFalse(result.contains("61")));
        }

        {
            String smt = "(and (< b2 5) (> b2 0) (>= b2 2))";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("All Identifiers are identified",
                      () -> assertTrue(result.contains("b2")),
                      () -> assertFalse(result.contains("and")));
        }

        {
            String smt = "(and (< b2 5) (> b2 0) (>= b2 i4))";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("All Identifiers are identified",
                      () -> assertTrue(result.contains("b2")),
                      () -> assertTrue(result.contains("i4")),
                      () -> assertFalse(result.contains("and")));
        }

        {
            String smt = "(or (< b2 5) (>= b2 5))";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("All identifiers are identified",
                      () -> assertTrue(result.contains("b2")),
                      () -> assertFalse(result.contains("or")));
        }
    }

    @Test
    void testParseExpression() {

        {
            assertThrows(AssertionError.class, () -> {
                    Smt2Reader.parseExpression("->");
                });
        }

        {
            Optional<SmtExpression> result = Smt2Reader.parseExpression("");
            assertAll("Empty expression is parsed as empty",
                      () -> assertFalse(result.isPresent()));
        }

        {
            String varFormula = "i0->(= i0 0)";
            SmtExpression result = Smt2Reader.parseExpression(varFormula).get();
            assertAll("Expression Parsed Correctly",
                      () -> assertEquals("i0", result.identifier),
                      () -> assertTrue(result.identifiers.contains("i0")),
                      () -> assertEquals("(= i0 0)", result.expression));
        }

        {
            String varFormula = "b2->(and (< b2 5) (> b2 0) (>= b2 i4))";
            SmtExpression result = Smt2Reader.parseExpression(varFormula).get();
            assertAll("Expression Parsed Correctly",
                      () -> assertEquals("b2", result.identifier),
                      () -> assertTrue(result.identifiers.contains("b2")),
                      () -> assertTrue(result.identifiers.contains("i4")),
                      () -> assertFalse(result.identifiers.contains("and")),
                      () -> assertEquals("(and (< b2 5) (> b2 0) (>= b2 i4))",
                                         result.expression));
        }

        {
            String varFormula = "b2f->(and (< b2 5) (> b2 0) (>= b2 i4))";
            SmtExpression result = Smt2Reader.parseExpression(varFormula).get();
            assertAll("Expression Parsed Correctly",
                      () -> assertEquals("b2f", result.identifier),
                      () -> assertFalse(result.identifiers.contains("b2f")),
                      () -> assertTrue(result.identifiers.contains("b2")),
                      () -> assertTrue(result.identifiers.contains("i4")),
                      () -> assertFalse(result.identifiers.contains("and")),
                      () -> assertEquals("(and (< b2 5) (> b2 0) (>= b2 i4))",
                                         result.expression));
        }
    }

    @Test
    void testParseAnalysisOutput() {
        {
            Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory>\n" +
                                         "i1->(= i1 0)\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
            Map<String, List<SmtExpression>> result = Smt2Reader.parse(r1);
            assertAll("analysis output was parsed correctly",
                      () -> assertEquals(2, result.keySet().size()),
                      () -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(1, result.get("6 i1 = 0:<test.BallonFactory>").size()),
                      () -> assertEquals("(= i1 0)", result.get("6 i1 = 0:<test.BallonFactory>").get(0).expression),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertEquals(1, result.get("7 b2 = 2:<test.BallonFactory>").size()),
                      () -> assertEquals("(and (< b2 5) (> b2 0) (>= b2 2))",
                                         result.get("7 b2 = 2:<test.BallonFactory>").get(0).expression));
        }

        {
            Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory>\n" +
                                         "i1->(= i1 0)\n" +
                                         "i1f->(or (<= i1 0) (> i1 0))\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "b2->(and (< b2 5)\n" +
                                         "         (> b2 0)\n" +
                                         "         (>= b2 2))\n");
            Map<String, List<SmtExpression>> result = Smt2Reader.parse(r1);
            assertAll("analysis output was parsed correctly",
                      () -> assertEquals(2, result.keySet().size()),
                      () -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(2, result.get("6 i1 = 0:<test.BallonFactory>").size()),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertEquals(1, result.get("7 b2 = 2:<test.BallonFactory>").size()),
                      () -> assertEquals("(and (< b2 5) (> b2 0) (>= b2 2))",
                                         result.get("7 b2 = 2:<test.BallonFactory>").get(0).expression));
        }

        {
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
            Map<String, List<SmtExpression>> result = Smt2Reader.parse(r1);
            assertAll("analysis output was parsed correctly",
                      () -> assertEquals(2, result.keySet().size()),
                      () -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(4, result.get("6 i1 = 0:<test.BallonFactory>").size()),
                      () -> assertEquals("(= i1 0)", result.get("6 i1 = 0:<test.BallonFactory>").get(0).expression),
                      () -> assertEquals("(or (<= i1 0) (> i1 0))",
                                         result.get("6 i1 = 0:<test.BallonFactory>").get(1).expression),
                      () -> assertEquals("(or (<= b2 0) (> b2 0))",
                                         result.get("6 i1 = 0:<test.BallonFactory>").get(2).expression),
                      () -> assertEquals("(and (<= b6 (+ i1 3)) (<= b6 (+ b2 4)))",
                                         result.get("6 i1 = 0:<test.BallonFactory>").get(3).expression),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").get(3).identifiers.contains("b2")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").get(3).identifiers.contains("b6")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").get(3).identifiers.contains("i1")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertEquals(1, result.get("7 b2 = 2:<test.BallonFactory>").size()),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").get(0).identifiers.contains("b2")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").get(0).identifiers.contains("i4")),
                      () -> assertEquals("(and (< b2 5) (> b2 0) (>= b2 i4))",
                                         result.get("7 b2 = 2:<test.BallonFactory>").get(0).expression));
        }
    }
}
