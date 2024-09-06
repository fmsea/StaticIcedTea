package dev.fmsea.processing.smt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import soot.grimp.Grimp;

public class SmtExpressionReaderTest {

    @ParameterizedTest
    @MethodSource("smtExpressionProvider")
    void testParseExpression(String smtExpression,
                             String message) {
        SmtExpressionReader reader = new SmtExpressionReader(smtExpression);
        SmtExpression expr = reader.getSmtExpression();
        assertEquals(smtExpression, expr.toSmt2(), message);
    }

    private static Stream<Arguments> smtExpressionProvider() {
        Grimp g = Grimp.v();

        return Stream.of(Arguments.arguments("(= i0 0)",
                                             "i0 == 0 != i0 == 0"),
                         Arguments.arguments("true",
                                             "true != 0 == 0"),
                         Arguments.arguments("false",
                                             "false is not false"),
                         Arguments.arguments("i0",
                                             "i0 != i0"),
                         Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                             "(i0 == 1 | i1 >= 2) != (i0 == 1 | i1 >= 2)"),
                         Arguments.arguments("(- 1)",
                                             "-1 != -1"),
                         Arguments.arguments("(- i0)",
                                             "-i0 != -i0"),
                         Arguments.arguments("(and (<= i3 (+ i4 0)) (<= i4 (+ i0 (- 1))) (= $z0 0) (>= i3 0))",
                                             "holy nested ampersands batman"));
    }
}
