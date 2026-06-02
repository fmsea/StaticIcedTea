package dev.fmsea.inference;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ZonotopalInferenceParserTests {

    @ParameterizedTest
    @MethodSource("simpleExpressionsProvider")
    public void canParseSimpleExpressions(String expression) {
        var expr = ZonotopalInferenceParser.parseExpr(expression);
        assertEquals(expression, expr.toString());
    }

    private static Stream<Arguments> simpleExpressionsProvider() {
        return Stream.of(
            Arguments.of("v0 == 0"),
            Arguments.of("v0 >= 0"),
            Arguments.of("v0 <= 0"),
            Arguments.of("v0 > 0"),
            Arguments.of("v0 < 0"),
            Arguments.of("0 == v0"),
            Arguments.of("0 >= v0"),
            Arguments.of("0 <= v0"),
            Arguments.of("0 > v0"),
            Arguments.of("0 < v0"),
            Arguments.of("v0 == v1"),
            Arguments.of("v0 >= v1"),
            Arguments.of("v0 <= v1"),
            Arguments.of("v0 > v1"),
            Arguments.of("v0 < v1"),
            Arguments.of("v0 - v1 <= 0"),
            Arguments.of("v0 + v1 <= 0"),
            Arguments.of("v0 - v1 >= 0"),
            Arguments.of("v0 + v1 >= 0"),
            Arguments.of("v0 - v1 < 0"),
            Arguments.of("v0 + v1 < 0"),
            Arguments.of("v0 - v1 > 0"),
            Arguments.of("v0 + v1 > 0"),
            Arguments.of("v0 >= -1345"),
            Arguments.of("v0 <= -13234")
        );
    }
}
