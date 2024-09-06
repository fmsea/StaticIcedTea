package dev.fmsea.absint.scalar.state;

import soot.IntType;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverWrapperZ3;

public class Interval32BoxZ3Test {
    private SolverWrapper solver;
    @BeforeEach
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Test
    void testToGrimpExprEqualsZ3SMT() {
        Local l = Jimple.v().newLocal("l0", IntType.v());
        Grimp g = Grimp.v();
        IntConstant zero = IntConstant.v(0);
        IntConstant max = IntConstant.v(Integer.MAX_VALUE);
        IntConstant min = IntConstant.v(Integer.MIN_VALUE);
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max_box = Interval32Box.MAX();
        assertAll(() -> assertTrue(solver.equals(g.newAndExpr(g.newGeExpr(l, zero),
                                                              g.newLtExpr(l, zero)),
                                                 bot.toGrimpExpr(l))),
                  () -> assertTrue(solver.equals(g.newOrExpr(g.newGeExpr(l, zero),
                                                             g.newLtExpr(l, zero)),
                                                 top.toGrimpExpr(l))),
                  () -> assertTrue(solver.equals(g.newAndExpr(g.newGeExpr(l, min),
                                                              g.newLeExpr(l, max)),
                                                 max_box.toGrimpExpr(l))));
    }
}
