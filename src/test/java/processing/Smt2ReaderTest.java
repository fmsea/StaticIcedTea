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
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;

import soot.Local;
import common.Locals;
import processing.util.FlowSet;

public class Smt2ReaderTest {

    @ParameterizedTest
    @MethodSource("testGetIdentifiersProvider")
    void testGetIdentifiers(String smt,
                            Set<String> expectedPresent,
                            Set<String> expectedAbsent) {
        Set<String> results = Smt2Reader.getIdentifiers(smt).stream().map(v -> v.toString()).collect(Collectors.toSet());
        Stream<Executable> assertions0 = Stream.of(() -> assertEquals(expectedPresent.size(), results.size()));
        Stream<Executable> assertions1 = Stream.of(() -> assertTrue(results.containsAll(expectedPresent)));
        Stream<Executable> assertions2 = expectedAbsent.stream().map(absent -> () -> assertFalse(results.contains(absent)));
        Stream<Executable> assertions = Stream.of(assertions0,
                                                  assertions1,
                                                  assertions2).flatMap(s -> s.map(v -> v));
        assertAll(assertions);
    }

    private static Stream<Arguments> testGetIdentifiersProvider() {
        return Stream.of(Arguments.arguments("(= i0 0)",
                                             Set.of("i0"),
                                             Set.of("0")),
                         Arguments.arguments("(= i0 61)",
                                             Set.of("i0"),
                                             Set.of("61")),
                         Arguments.arguments("(and (< b2 5) (> b2 0) (>= b2 2))",
                                             Set.of("b2"),
                                             Set.of("and")),
                         Arguments.arguments("(and (< b2 5) (> b2 0) (>= b2 i4))",
                                             Set.of("b2", "i4"),
                                             Set.of("and")),
                         Arguments.arguments("(or (< b2 5) (>= b2 5))",
                                              Set.of("b2"),
                                              Set.of("or")),
                         Arguments.arguments("true",
                                             Set.of(),
                                             Set.of("true")),
                         Arguments.arguments("false",
                                             Set.of(),
                                             Set.of("false")));
    }

    @Test
    void testParseAnalysisOutput() {
        {
            Reader r1 = new StringReader("b2\ti1\n" +
                                         "6 i1 = 0:<test.BallonFactory>\n" +
                                         "fall\t(= i1 0)\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "fall\t(and (< b2 5) (> b2 0) (>= b2 2))\n");
            AnalysisSMTReport result = Smt2Reader.parse(r1);
            assertAll("analysis output was parsed correctly",
                      () -> assertEquals(2, result.statements().size()),
                      () -> assertTrue(result.statements().contains("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of("(= i1 0)"), result.getFallThrough("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.statements().contains("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of("(and (< b2 5) (> b2 0) (>= b2 2))"),
                                         result.getFallThrough("7 b2 = 2:<test.BallonFactory>")));
        }

        {
            Reader r1 = new StringReader("b2\ti1\n" +
                                         "6 i1 = 0:<test.BallonFactory>\n" +
                                         "fall\t(= i1 0)\n" +
                                         "branch\t(or (<= i1 0) (> i1 0))\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "fall\t(and (< b2 5)\n" +
                                         "\t(> b2 0)\n" +
                                         "\t(>= b2 2))\n");
            AnalysisSMTReport result = Smt2Reader.parse(r1);
            assertAll("analysis output was parsed correctly",
                      () -> assertEquals(2, result.statements().size()),
                      () -> assertTrue(result.statements().contains("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of("(= i1 0)"),
                                         result.getFallThrough("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of("(or (<= i1 0) (> i1 0))"),
                                         result.getBranchOut("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.statements().contains("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of("(and (< b2 5)\n\t(> b2 0)\n\t(>= b2 2))"),
                                         result.getFallThrough("7 b2 = 2:<test.BallonFactory>")));
        }

    }

    @Test
    void testGetIdentifiersPerStatement() {
        {
            Reader r1 = new StringReader("b2\ti1\n" +
                                         "6 i1 = 0:<test.BallonFactory>\n" +
                                         "fall\t(= i1 0)\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "fall\t(and (< b2 5) (> b2 0) (>= b2 2))\n");
            Map<String, FlowSet<Local>> result = Smt2Reader.getIdentifiersPerStatement(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains(Locals.get("i1"))),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().isEmpty()),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains(Locals.get("b2"))),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
        }

        {
            Reader r1 = new StringReader("b2\ti1\n" +
                                         "6 i1 = 0:<test.BallonFactory>\n" +
                                         "fall\t(= i1 0)\n" +
                                         "branch\t(or (<= i1 0) (> i1 0))\n" +
                                         "7 b2 = 2:<test.BallonFactory>\n" +
                                         "fall\t(and (< b2 5)\n" +
                                         "           (> b2 0)\n" +
                                         "           (>= b2 2))\n");
            Map<String, FlowSet<Local>> result = Smt2Reader.getIdentifiersPerStatement(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains(Locals.get("i1"))),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains(Locals.get("i1"))),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains(Locals.get("b2"))),
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
            Map<String, FlowSet<Local>> result = Smt2Reader.parseExtraIdentifiers(r1);
            assertAll(() -> assertTrue(result.containsKey("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertTrue(result.containsKey("7 b2 = 2:<test.BallonFactory>")),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains(Locals.get("b2"))),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains(Locals.get("b6"))),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getFallThrough().contains(Locals.get("i1"))),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains(Locals.get("b2"))),
                      () -> assertFalse(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains(Locals.get("b6"))),
                      () -> assertTrue(result.get("6 i1 = 0:<test.BallonFactory>").getBranchOut().contains(Locals.get("i1"))),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains(Locals.get("b2"))),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getFallThrough().contains(Locals.get("i4"))),
                      () -> assertTrue(result.get("7 b2 = 2:<test.BallonFactory>").getBranchOut().isEmpty()));
        }
    }

    @Test
    void testParseFullReport() {
        {
            Reader r1 = new StringReader("");
            AnalysisSMTReport report = Smt2Reader.parse(r1);
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
            AnalysisSMTReport report = Smt2Reader.parse(r1);
            assertAll(() -> assertTrue(report.variables().contains(Locals.get("$z0"))),
                      () -> assertTrue(report.variables().contains(Locals.get("b0"))),
                      () -> assertTrue(report.statements().contains("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("3 $z0 = 1:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("5 $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.statements().contains("6 return $z0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertTrue(report.getFallThrough("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getBranchOut("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals(Set.of(Locals.get("b0")), report.getFallVariables("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>").get()),
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
                      () -> assertEquals(Set.of(Locals.get("$z0")), report.getFallVariables("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertEquals("(= $z0 0)",
                                         report.getFallThrough("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("5 $z0 = 0:<test.Base64: boolean isPad(byte)>").isEmpty()),
                      () -> assertTrue(report.getFallThrough("6 return $z0:<test.Base64: boolean isPad(byte)>").isPresent()),
                      () -> assertEquals("(and (>= $z0 0) (< $z0 0) (>= b0 0) (< b0 0))",
                                         report.getFallThrough("6 return $z0:<test.Base64: boolean isPad(byte)>").get()),
                      () -> assertTrue(report.getBranchOut("6 return $z0:<test.Base64: boolean isPad(byte)>").isEmpty()));
        }
    }

    @Test
    void testParseFullReportWithChangedVariables() {
        {
            String reportSource = Stream.of("b2\ti1",
                                      "6 i1 = 0:<test.BallonFactory>",
                                      "fall\ti1\t(= i1 0)",
                                      "7 b2 = 2:<test.BallonFactory>",
                                      "fall\tb2\t(and (< b2 5) (> b2 0) (>= b2 2))").collect(Collectors.joining("\n"));
            Reader r1 = new StringReader(reportSource);
            AnalysisSMTReport report = Smt2Reader.parse(r1);
            assertAll(() -> assertEquals(reportSource, report.toString().trim()),
                      () -> assertEquals(Optional.of(Set.of(Locals.get("i1"))),
                                         report.getFallChangedVariables("6 i1 = 0:<test.BallonFactory>")),
                      () -> assertEquals(Optional.of(Set.of(Locals.get("b2"))),
                                         report.getFallChangedVariables("7 b2 = 2:<test.BallonFactory>")));
        }

        {
            String inputAnalysisText = Stream.of("$z0	b0",
                                                 "1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>",
                                                 "2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	b0	(= b0 61)",
                                                 "3 $z0 = 1:<test.Base64: boolean isPad(byte)>",
                                                 "fall	$z0	(and (= $z0 1) (= b0 61))",
                                                 "4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>",
                                                 "branch	(and (= $z0 1)\n\t(= b0 61))",
                                                 "5 $z0 = 0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	$z0	(= $z0 0)",
                                                 "6 return $z0:<test.Base64: boolean isPad(byte)>",
                                                 "fall	(and (>= $z0 0) (< $z0 0) (>= b0 0) (< b0 0))",
                                                 "").collect(Collectors.joining("\n"));
            Reader r1 = new StringReader(inputAnalysisText);
            AnalysisSMTReport report = Smt2Reader.parse(r1);
            assertAll(() -> assertEquals(inputAnalysisText, report.toString()),
                      () -> assertEquals(Optional.empty(),
                                         report.getFallChangedVariables("1 b0 := @parameter0: byte:<test.Base64: boolean isPad(byte)>")),
                      () -> assertEquals(Optional.of(Set.of(Locals.get("b0"))),
                                         report.getFallChangedVariables("2 if b0 != 61 goto $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertEquals(Optional.empty(),
                                         report.getBranchChangedVariables("4 goto [?= return $z0]:<test.Base64: boolean isPad(byte)>")),
                      () -> assertEquals(Optional.of(Set.of(Locals.get("$z0"))),
                                         report.getFallChangedVariables("5 $z0 = 0:<test.Base64: boolean isPad(byte)>")),
                      () -> assertEquals(Optional.empty(),
                                         report.getFallChangedVariables("6 return $z0:<test.Base64: boolean isPad(byte)>")));
        }
    }

    @Test
    void testReadFalseWithChangedVariable() {
        String reportSource = Stream.of("l0",
                                        "4 if l0 >= 3 goto l3 = 6",
                                        "fall	l0	false").collect(Collectors.joining("\n"));
        Reader r1 = new StringReader(reportSource);
        AnalysisSMTReport report = Smt2Reader.parse(r1);
        assertAll(() -> assertEquals(reportSource, report.toString().trim()
                                     ),
                  () -> assertEquals(Optional.of(Set.of(Locals.get("l0"))),
                                     report.getFallChangedVariables("4 if l0 >= 3 goto l3 = 6")),
                  () -> assertEquals(Optional.of("false"),
                                     report.getFallThrough("4 if l0 >= 3 goto l3 = 6")));
    }

    @Test
    void testReadWithVariablesContainingTorF() {
        String reportSource = Stream.of("f0\tt0",
                                        "7 test-statement",
                                        "fall	t0	true",
                                        "branch	true").collect(Collectors.joining("\n"));
        Reader r1 = new StringReader(reportSource);
        AnalysisSMTReport report = Smt2Reader.parse(r1);
        assertAll(() -> assertEquals(reportSource, report.toString().trim()),
                  () -> assertEquals(Optional.of(Set.of(Locals.get("t0"))),
                                     report.getFallChangedVariables("7 test-statement")),
                  () -> assertEquals(Optional.of("true"),
                                     report.getFallThrough("7 test-statement")),
                  () -> assertEquals(Optional.empty(),
                                     report.getBranchChangedVariables("7 test-statement")),
                  () -> assertEquals(Optional.of("true"),
                                     report.getBranchOut("7 test-statement")));
    }
}
