package dev.fmsea.processing;

import static dev.fmsea.util.ResourceFileUtility.readResourcesFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.absint.scalar.state.DefaultOctagonState;
import dev.fmsea.absint.scalar.state.SimpleOctagonState;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.ZonotopalInferenceParser;
import dev.fmsea.inference.rewrite.DefaultRewriter;
import dev.fmsea.inference.rewrite.Rewriter;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.TADRReader;
import dev.fmsea.tadr.visitors.VariableVisitor;
import dev.fmsea.util.Sets;
import soot.Local;

public class InferDomainsTests {

    @ParameterizedTest
    @MethodSource("constructOctagons")
    public void testConstructionDifferential(Set<String> expressions, Set<String> oracle) {
        Rewriter rewriter = new DefaultRewriter();
        Set<InvariantExpression> exprs = expressions.stream()
            .map(expr -> ZonotopalInferenceParser.parseExpr(expr))
            .flatMap(expr -> rewriter.rewrite(expr))
            .collect(Collectors.toSet());
        Set<TADR> tadrs = oracle.stream().map(tadr -> TADRReader.parse(tadr)).collect(Collectors.toSet());
        Set<Local> locals = tadrs.stream()
            .map(tadr -> tadr.accept(new VariableVisitor()))
            .reduce(Sets::union)
            .orElse(Set.of());
        DefaultOctagonState expectedOctag = new DefaultOctagonState(locals, true);
        tadrs.stream()
            .forEach(tadr -> expectedOctag.refine(tadr, expectedOctag));
        SimpleOctagonState state = InferDomains.from(exprs);
        assertAll(
            () -> assertTrue(expectedOctag.reduce()),
            () -> assertEquals(expectedOctag.toSmt(), state.toSmt()));
    }

    private static Stream<Arguments> constructOctagons() {
        return Stream.of(
            Arguments.of(Set.of("v0 <= 0", "v0 >= 0"), Set.of("(<= v0 0)", "(>= v0 0)")),
            Arguments.of(Set.of("v0 <= v1"), Set.of("(<= v0 (+ v1 0))")),
            Arguments.of(Set.of("v0 + v1 <= 0"), Set.of("(<= v1 (- 0 v0))")),
            Arguments.of(Set.of("v0 + v1 >= 0"), Set.of("(>= v1 (- 0 v0))")),
            Arguments.of(Set.of("v0 + v1 >= 0", "v0 - v1 <= 0"), Set.of("(<= v0 (+ 0 v1))", "(>= v1 (- 0 v0))", "(>= v1 0)")),
            Arguments.of(Set.of("v0 - v1 <= 4", "v1 - v0 <= -4"), Set.of("(<= v0 (+ v1 4))", "(<= v1 (- v0 4))")),
            Arguments.of(Set.of("v0 - v1 <= 4", "v1 - v0 <= -2"), Set.of("(<= v0 (+ v1 4))", "(<= v1 (- v0 2))")),
            Arguments.of(
                Set.of("v4 >= 0", "v1 - v2 <= 1", "v2 - v1 <= 0", "v4 - v3 <= -1"),
                Set.of(
                    "(>= v3 1)",
                    "(>= v4 0)",
                    "(<= v1 (+ v2 1))",
                    "(<= v2 (+ v1 0))",
                    "(<= v4 (+ v3 (- 1)))",
                    "(>= v4 (- (- 1) v3))")),
            Arguments.of(Set.of("v0 <= -1", "v0 >= 1"), Set.of("(<= v0 (- 1))", "(>= v0 1)")),
            Arguments.of(Set.of(
                "v0 == v2",
                "v0 <= 0",
                "v0 >= 0",
                "v1 <= 1",
                "v1 >= 1",
                "v0 - v1 <= -1",
                "v1 - v0 <= 1"
            ), Set.of(
                "(<= v0 (+ 0 v2))",
                "(<= v2 (+ 0 v0))",
                "(<= v0 0)",
                "(>= v0 0)",
                "(<= v1 1)",
                "(>= v1 1)",
                "(<= v0 (+ 1 v1))",
                "(<= v1 (- 1 v0))"
            ))

        );
    }

    @Test
    public void testInvariantParsingWorksOverSpaces() {
        String expressions = Stream.of(
            "v0 == v2",
            "v0 <= 0",
            "v0 >= 0",
            "v1 <= 1",
            "v1 >= 1",
            "v0 - v1 <= -1",
            "v1 - v0 <= 1").sorted().collect(Collectors.joining("    "));
        assertEquals(
            expressions,
            InferDomains.parseInvariants(expressions)
                .stream()
                .map(expr -> expr.toString())
                .sorted()
                .collect(Collectors.joining("    ")));
    }

    @ParameterizedTest
    @MethodSource("parseInvariants")
    public void testInvaraintParsing(String expressions) {
        String result = InferDomains.parseInvariants(expressions)
            .stream()
            .map(expr -> expr.toString())
            .sorted()
            .collect(Collectors.joining("\t"));
        assertEquals(expressions, result);
    }

    private static Stream<Arguments> parseInvariants() {
        return Stream.of(
            Arguments.of(""),
            Arguments.of("v1 >= 0"),
            Arguments.of(Stream.of("v0 - v1 <= 2", "v1 - v0 <= -2").sorted().collect(Collectors.joining("\t"))),
            Arguments.of("v1 <= -1"),
            Arguments.of(Stream.of("v0 - v1 <= 4", "v1 - v0 <= -4").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v0 - v1 <= 4", "v1 - v0 <= -2").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v0 == v2", "v0 <= 0", "v0 >= 0", "v1 <= 1", "v1 >= 1", "v0 - v1 <= -1", "v1 - v0 <= 1").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v4 >= 0", "v1 - v2 <= 1", "v2 - v1 <= 0", "v4 - v3 <= -1").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v4 >= 0", "v4 - v3 <= -1").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v4 >= 0", "v0 == v1", "v4 - v3 <= -1").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v1 == v2", "v4 >= 0", "v4 - v3 <= -1").sorted().collect(Collectors.joining("\t"))),
            Arguments.of(Stream.of("v1 - v2 <= 1", "v2 - v1 <= 0").sorted().collect(Collectors.joining("\t")))
        );
    }

    @ParameterizedTest
    @MethodSource("inferDomainsExamples")
    public void testDomainInference(String input, String oracle) {
        try (StringWriter writer = new StringWriter();
             StringReader reader = new StringReader(input)) {
            InferDomains.inferDomains(new BufferedReader(reader), new BufferedWriter(writer));
            assertEquals(oracle, writer.toString());
        } catch (IOException ex) {
            assertTrue(false);
        }
    }

    private static Stream<Arguments> inferDomainsExamples() {
        return Stream.of(
            Arguments.of(
                readResourcesFile("dev/fmsea/inference/motivation.in"),
                readResourcesFile("dev/fmsea/inference/motivation.out")),
            Arguments.of(
                readResourcesFile("dev/fmsea/inference/fibonacci.in"),
                readResourcesFile("dev/fmsea/inference/fibonacci.out"))
        );
    }
}
