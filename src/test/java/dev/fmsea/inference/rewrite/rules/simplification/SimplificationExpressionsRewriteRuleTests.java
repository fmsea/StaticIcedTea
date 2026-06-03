package dev.fmsea.inference.rewrite.rules.simplification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.inference.rewrite.rules.RewriteRuleTests;

public class SimplificationExpressionsRewriteRuleTests extends RewriteRuleTests {

    @BeforeEach
    public void setup() {
        this.rule = new SimplificationExpressionsRewriteRule();
    }

    @ParameterizedTest
    @MethodSource("simplifyExpressions")
    public void testRewriteRule(String expression, String expected) {
        assertEquals(expected, parse(expression).accept(rule).toString());
    }
}
