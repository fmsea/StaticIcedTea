package solver;

import java.util.stream.Stream;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.grimp.Grimp;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import processing.Locals;

public class SolverWrapperTest {

    private SolverWrapper solver = SolverFactory.getSolver();

    @ParameterizedTest
    @MethodSource("grimpToString")
    void testSmt2ToString(String expected, Value expr) {
        assertEquals(expected, solver.smt2(expr));
    }

    private static Stream<Arguments> grimpToString() {
        return Stream.of(Arguments.arguments("(- 1)", Grimp.v().newNegExpr(IntConstant.v(1))),
                         Arguments.arguments("(- i0)", Grimp.v().newNegExpr(Locals.get("i0"))),
                         Arguments.arguments("(<= i1 (+ i0 3))",
                                             Grimp.v().newLeExpr(Locals.get("i1"),
                                                                 Grimp.v().newAddExpr(Locals.get("i0"),
                                                                                      IntConstant.v(3)))));
    }
}
