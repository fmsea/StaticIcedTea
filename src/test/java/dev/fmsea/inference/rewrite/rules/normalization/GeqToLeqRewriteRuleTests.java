package dev.fmsea.inference.rewrite.rules.normalization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.inference.rewrite.rules.RewriteRuleTests;

public class GeqToLeqRewriteRuleTests extends RewriteRuleTests {

    @BeforeEach
    public void setup() {
        this.rule = new GeqToLeqRewriteRule();
    }

    @ParameterizedTest
    @MethodSource("geq2LeqExpressions")
    public void testGeq2Leq(String expression, String expected) {
        assertEquals(expected, parse(expression).accept(rule).toString());
    }
}
