package processing.smt;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import soot.Local;
import soot.IntType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.grimp.Grimp;
import processing.providers.SmtExpressionProvider;
import processing.Locals;

public class SmtExpressionReaderTest {

    @ParameterizedTest
    @MethodSource("smtExpressionProvider")
    void testParseExpression(String smtExpression,
                             Value expected,
                             String message) {
        SmtExpressionReader reader = new SmtExpressionReader(smtExpression);
        SmtExpression expr = reader.getSmtExpression();
        assertEquals(expected.toString(), expr.getValue().toString(), message);
    }

    private static Stream<Arguments> smtExpressionProvider() {
        Grimp g = Grimp.v();

        return Stream.of(Arguments.arguments("(= i0 0)",
                                             g.newEqExpr(Locals.get("i0"),
                                                         IntConstant.v(0)),
                                             "i0 == 0 != i0 == 0"),
                         Arguments.arguments("true",
                                             g.newEqExpr(IntConstant.v(0),
                                                         IntConstant.v(0)),
                                             "true != 0 == 0"),
                         Arguments.arguments("false",
                                             g.newEqExpr(IntConstant.v(0),
                                                         IntConstant.v(1)),
                                             "false != 0 == 1"),
                         Arguments.arguments("i0",
                                             Locals.get("i0"),
                                             "i0 != i0"),
                         Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                             g.newOrExpr(g.newEqExpr(Locals.get("i0"),
                                                                     IntConstant.v(1)),
                                                         g.newGeExpr(Locals.get("i1"),
                                                                     IntConstant.v(2))),
                                             "(i0 == 1 | i1 >= 2) != (i0 == 1 | i1 >= 2)"),
                         Arguments.arguments("(- 1)",
                                             g.newNegExpr(IntConstant.v(1)),
                                             "-1 != -1"),
                         Arguments.arguments("(- i0)",
                                             g.newNegExpr(Locals.get("i0")),
                                             "-i0 != -i0"),
                         Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                             g.newAndExpr(g.newEqExpr(Locals.get("$z0"),
                                                                      IntConstant.v(0)),
                                                          g.newAndExpr(g.newGeExpr(Locals.get("i3"),
                                                                                   IntConstant.v(0)),
                                                                       g.newAndExpr(g.newLeExpr(Locals.get("i4"),
                                                                                                g.newAddExpr(Locals.get("i0"),
                                                                                                             g.newNegExpr(IntConstant.v(1)))),
                                                                                    g.newLeExpr(Locals.get("i3"),
                                                                                                g.newAddExpr(Locals.get("i4"),
                                                                                                             IntConstant.v(0)))))),
                                             "holy nested ampersands batman"));
    }
}
