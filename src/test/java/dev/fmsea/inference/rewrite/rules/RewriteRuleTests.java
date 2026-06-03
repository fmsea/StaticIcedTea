package dev.fmsea.inference.rewrite.rules;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.ZonotopalInferenceParser;

public abstract class RewriteRuleTests {

    protected RewriteRule rule;

    public InvariantExpression parse(String expression) {
        return ZonotopalInferenceParser.parseExpr(expression);
    }

    protected static Stream<Arguments> eqExpressions() {
        return Stream.of(
            Arguments.of("v0 == 1", "v0 <= 1 && v0 >= 1"),
            Arguments.of("v0 == v1", "v0 <= v1 && v0 >= v1")
        );
    }

    protected static Stream<Arguments> lt2LeqExpressions() {
        return Stream.of(
            Arguments.of("v0 < 0", "v0 <= 0 - 1"),
            Arguments.of("v0 < v1", "v0 <= v1 - 1"),
            Arguments.of("v0 + v1 < 0", "v0 + v1 <= 0 - 1")
        );
    }

    protected static Stream<Arguments> gt2GeqExpressions() {
        return Stream.of(
            Arguments.of("v0 > 0", "v0 >= 0 + 1"),
            Arguments.of("v0 > v1", "v0 >= v1 + 1"),
            Arguments.of("v0 + v1 > 0", "v0 + v1 >= 0 + 1")
        );
    }

    protected static Stream<Arguments> geq2LeqExpressions() {
        return Stream.of(
            Arguments.of("v0 >= v1", "v1 <= v0"),
            Arguments.of("v0 >= 0", "v0 >= 0"),
            Arguments.of("v0 - v1 >= 0", "v1 - v0 <= 0"),
            Arguments.of("v0 - v1 >= 3", "v1 - v0 <= -3"),
            Arguments.of("v0 + v1 >= 0", "v0 + v1 >= 0"),
            Arguments.of("v0 + v1 >= 3", "v0 + v1 >= 3")
        );
    }

    protected static Stream<Arguments> moveVariablesLeftExpressions() {
        return Stream.of(
            Arguments.of("v0 <= v1", "v0 - v1 <= 0"),
            Arguments.of("v0 >= v1", "v0 - v1 >= 0")
        );
    }

    protected static Stream<Arguments> moveSumsLeftExpressions() {
        return Stream.of(
            Arguments.of("v0 <= 0 - 1", "v0 <= 0 - 1"),
            Arguments.of("v0 <= v1 - 1", "v0 - v1 <= -1"),
            Arguments.of("v0 <= 1 - v1", "v0 + v1 <= 1"),
            Arguments.of("v0 >= v1 + 1", "v0 - v1 >= 1"),
            Arguments.of("v0 <= v1 - 1", "v0 - v1 <= -1"),
            Arguments.of("v0 >= v1 + 1", "v0 - v1 >= 1"),
            Arguments.of("v0 + v1 <= 0 - 1", "v0 + v1 <= 0 - 1")
        );
    }

    protected static Stream<Arguments> simplifyExpressions() {
        return Stream.of(
            Arguments.of("v0 + v1 <= 0 - 1", "v0 + v1 <= -1"),
            Arguments.of("v0 <= 0 - 1", "v0 <= -1"),
            Arguments.of("v0 >= 0 + 1", "v0 >= 1")
        );
    }

    protected static Stream<Arguments> octagonalExpressions() {
        return Stream.of(
            Arguments.of("-v0 - v1 <= 3", "v0 + v1 >= -3"),
            Arguments.of("-v0 + v1 <= 2", "-v0 + v1 <= 2"),
            Arguments.of("-v1 - v0 <= -2", "v1 + v0 >= 2")
        );
    }
}
