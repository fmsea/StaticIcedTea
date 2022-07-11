package processing;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import processing.util.FlowSet;

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

        {
            String smt = "true";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("No identifier is found",
                      () -> assertEquals(0, result.size()));
        }

        {
            String smt = "false";
            Set<String> result = Smt2Reader.getIdentifiers(smt);
            assertAll("No identifier is found",
                      () -> assertEquals(0, result.size()));
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
            Optional<SmtIdentifierExpression> result = Smt2Reader.parseExpression("");
            assertAll("Empty expression is parsed as empty",
                      () -> assertFalse(result.isPresent()));
        }

        {
            String varFormula = "i0->(= i0 0)";
            SmtIdentifierExpression result = Smt2Reader.parseExpression(varFormula).get();
            assertAll("Expression Parsed Correctly",
                      () -> assertEquals("i0", result.identifier),
                      () -> assertTrue(result.identifiers.contains("i0")),
                      () -> assertEquals("(= i0 0)", result.expression));
        }

        {
            String varFormula = "b2->(and (< b2 5) (> b2 0) (>= b2 i4))";
            SmtIdentifierExpression result = Smt2Reader.parseExpression(varFormula).get();
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
            SmtIdentifierExpression result = Smt2Reader.parseExpression(varFormula).get();
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
            Map<String, List<SmtIdentifierExpression>> result = Smt2Reader.parse(r1);
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
            Map<String, List<SmtIdentifierExpression>> result = Smt2Reader.parse(r1);
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
            Map<String, List<SmtIdentifierExpression>> result = Smt2Reader.parse(r1);
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

    @Test
    void testGetIdentifiersPerStatement() {
        {
            Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory>\n" +
                                         "i1->(= i1 0)\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "b2->(and (< b2 5) (> b2 0) (>= b2 2))\n");
            Map<String, FlowSet<String>> result = Smt2Reader.getIdentifiersPerStatement(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("i1")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().isEmpty()),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
        }

        {
            Reader r1 = new StringReader("6 i1 = 0:<test.BallonFactory>\n" +
                                         "i1->(= i1 0)\n" +
                                         "i1f->(or (<= i1 0) (> i1 0))\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "b2->(and (< b2 5)\n" +
                                         "         (> b2 0)\n" +
                                         "         (>= b2 2))\n");
            Map<String, FlowSet<String>> result = Smt2Reader.getIdentifiersPerStatement(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("i1")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("i1")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
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
            Map<String, FlowSet<String>> result = Smt2Reader.getIdentifiersPerStatement(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("i1")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("b6")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("i1")),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("b2")),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("b6")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("i4")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
        }
    }

    @Test
    void testParseExtraIdentifiersTSV() {
        {
            Reader r1 = new StringReader(Stream.of("7 b2 = 2:<test.BallonFactory>\tfall\tb2\ti4",
                                                   "7 b2 = 2:<test.BallonFactory>\tbranch",
                                                   "6 i1 = 0:<test.BallonFactory>\tfall\tb2\tb6\ti1",
                                                   "6 i1 = 0:<test.BallonFactory>\tbranch\ti1")
                                         .collect(Collectors.joining("\n")));
            Map<String, FlowSet<String>> result = Smt2Reader.parseExtraIdentifiers(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("b6")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains("i1")),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("b2")),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("b6")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains("i1")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("b2")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains("i4")),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
        }
    }

    @Test
    void testParseFullReport() {
        {
            Reader r1 = new StringReader("");
            AnalysisFullSMTReport report = Smt2Reader.parseFullReport(r1);
            assertEquals("", report.toString());
        }

        {
            String inputAnalysisText = Stream.of("$z0	b0",
                                                 "1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>",
                                                 "2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	(= b0 61)",
                                                 "3 $z0 = 1:<test.Base64: boolean isPad(byte)>",
                                                 "fall	(and (= $z0 1) (= b0 61))",
                                                 "4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>",
                                                 "branch	(and (= $z0 1)\n\t(= b0 61))",
                                                 "5 $z0 = 0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	(= $z0 0)",
                                                 "6 return $z0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	(and (>= $z0 0) (< $z0 0) (>= b0 0) (< b0 0))",
                                                 "").collect(Collectors.joining("\n"));
            Reader r1 = new StringReader(inputAnalysisText);
            AnalysisFullSMTReport report = Smt2Reader.parseFullReport(r1);
            assertAll(() -> assertTrue(report.variables().contains("$z0")),
                      () -> assertTrue(report.variables().contains("b0")),
                      () -> assertTrue(report.statements().contains("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("3 $z0 = 1:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("5 $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("6 return $z0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.getFallThrough("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getBranchOut("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(= b0 61)",
                                         report.getFallThrough("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("3 $z0 = 1:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(and (= $z0 1) (= b0 61))",
                                         report.getFallThrough("3 $z0 = 1:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("3 $z0 = 1:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getBranchOut("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(and (= $z0 1)\n\t(= b0 61))",
                                         report.getBranchOut("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getFallThrough("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(= $z0 0)",
                                         report.getFallThrough("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("6 return $z0:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(and (>= $z0 0) (< $z0 0) (>= b0 0) (< b0 0))",
                                         report.getFallThrough("6 return $z0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("6 return $z0:<test.Base64: boolean isPad(byte)>").isEmpty()));
        }
    }
}
