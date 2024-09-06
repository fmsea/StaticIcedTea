package dev.fmsea.solver;

import java.util.stream.Stream;
import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;
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

import dev.fmsea.common.Locals;

public class SolverWrapperTest {

    private final SolverWrapper solver = SolverFactory.getSolver();
    private static final Grimp g = Grimp.v();

    @ParameterizedTest
    @MethodSource("grimpToString")
    void testSmt2ToString(String expected, Value expr) {
        assertEquals(expected, solver.smt2(expr));
    }

    private static Stream<Arguments> grimpToString() {
        return Stream.of(Arguments.arguments("(- 1)", g.newNegExpr(IntConstant.v(1))),
                         Arguments.arguments("(- i0)", g.newNegExpr(Locals.get("i0"))),
                         Arguments.arguments("(<= i1 (+ i0 3))",
                                             g.newLeExpr(Locals.get("i1"),
                                                                 g.newAddExpr(Locals.get("i0"),
                                                                                      IntConstant.v(3)))));
    }

    @ParameterizedTest
    @MethodSource("equalsProvider")
    void testSolverWrapperEquals(BinopExpr left, BinopExpr right, boolean expected) {
        assertEquals(expected, solver.equals(left, right));
    }

    private static Stream<Arguments> equalsProvider() {
        return Stream.of(Arguments.arguments(Stream.of(g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(Locals.get("y"), IntConstant.v(1))),
                                                       g.newLeExpr(Locals.get("y"),
                                                                   g.newAddExpr(Locals.get("z"), IntConstant.v(2))))
                                             .map(e -> (Value)e)
                                             .reduce((a, b) -> g.newAndExpr(a, b))
                                             .get(),
                                             Stream.of(g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(Locals.get("y"), IntConstant.v(1))),
                                                       g.newLeExpr(Locals.get("y"),
                                                                   g.newAddExpr(Locals.get("z"), IntConstant.v(2))),
                                                       g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(Locals.get("z"), IntConstant.v(3))))
                                             .map(e -> (Value)e)
                                             .reduce((a, b) -> g.newAndExpr(a, b))
                                             .get(),
                                             true),
                         Arguments.arguments(Stream.of(g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(g.newNegExpr(Locals.get("y")),
                                                                                IntConstant.v(1))),
                                                       g.newLeExpr(Locals.get("y"),
                                                                   g.newAddExpr(g.newNegExpr(Locals.get("z")),
                                                                                IntConstant.v(2))))
                                             .map(e -> (Value)e)
                                             .reduce((a, b) -> g.newAndExpr(a, b))
                                             .get(),
                                             Stream.of(g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(g.newNegExpr(Locals.get("y")),
                                                                                IntConstant.v(1))),
                                                       g.newLeExpr(Locals.get("y"),
                                                                   g.newAddExpr(g.newNegExpr(Locals.get("z")),
                                                                                IntConstant.v(2))),
                                                       g.newLeExpr(Locals.get("x"),
                                                                   g.newAddExpr(g.newNegExpr(Locals.get("z")),
                                                                                IntConstant.v(3))))
                                             .map(e -> (Value)e)
                                             .reduce((a, b) -> g.newAndExpr(a, b))
                                             .get(),
                                             false));
    }

    @ParameterizedTest
    @MethodSource("evaluationProvider")
    void testSolverEvaluate(BinopExpr expr, boolean expected) {
        assertEquals(expected, solver.evaluate(expr));
    }

    private static Stream<Arguments> evaluationProvider() {
        return Stream.of(Arguments.arguments(g.newEqExpr(IntConstant.v(0),
                                                                 IntConstant.v(0)),
                                             true),
                         Arguments.arguments(g.newEqExpr(IntConstant.v(0),
                                                                 IntConstant.v(1)),
                                             false),
                         Arguments.arguments(g.newLeExpr(Locals.get("i1"),
                                                                 g.newAddExpr(Locals.get("i0"),
                                                                                      IntConstant.v(3))),
                                             true));
    }

    @ParameterizedTest
    @MethodSource("notEvaluationProvider")
    void testSolverEvaluateNot(BinopExpr expr, boolean expected) {
        assertEquals(expected, solver.evaluateNot(expr));
    }

    private static Stream<Arguments> notEvaluationProvider() {
        return Stream.of(Arguments.arguments(g.newEqExpr(IntConstant.v(0),
                                                                 IntConstant.v(0)),
                                             false),
                         Arguments.arguments(g.newEqExpr(IntConstant.v(0),
                                                                 IntConstant.v(1)),
                                             true),
                         Arguments.arguments(g.newLeExpr(Locals.get("i1"),
                                                                 g.newAddExpr(Locals.get("i0"),
                                                                                      IntConstant.v(3))),
                                             true));
    }
}
