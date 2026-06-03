package dev.fmsea.inference.rewrite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.ZonotopalInferenceParser;

public class DefaultRewriterTests {

    private Rewriter rewriter;

    @BeforeEach
    public void setup() {
        this.rewriter = new DefaultRewriter();
    }

    @ParameterizedTest
    @MethodSource("simpleRewriteTests")
    public void testRewriter(String expression, String expected) {
        assertEquals(
            expected,
            this.rewriter.rewrite(ZonotopalInferenceParser.parseExpr(expression))
                .map(InvariantExpression::toString)
                .sorted()
                .collect(Collectors.joining(" && "))
        );
    }

    private static Stream<Arguments> simpleRewriteTests() {
        return Stream.of(
            Arguments.of("v0 < 0", "v0 <= -1"),
            Arguments.of("v0 < v1", "v0 - v1 <= -1"),
            Arguments.of("v0 + v1 < 0", "v0 + v1 <= -1"),
            Arguments.of("v0 > 0", "v0 >= 1"),
            Arguments.of("v0 > v1", "v1 - v0 <= -1"),
            Arguments.of("v0 >= v1", "v1 - v0 <= 0"),
            Arguments.of("v0 - v1 >= 0", "v1 - v0 <= 0"),
            Arguments.of("v0 + v1 >= 3", "v0 + v1 >= 3"),
            Arguments.of("-v0 - v1 <= 3", "v0 + v1 >= -3"),
            Arguments.of("v0 == v1", "v0 - v1 <= 0 && v1 - v0 <= 0")
        );
    }
}
