package dev.fmsea.absint.scalar.state;

import soot.IntType;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverWrapperZ3;

public class Interval32BoxZ3Properties {
    private SolverWrapper solver;

    @BeforeProperty
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Property
    boolean testToGrimpExprEqualsZ3SMT(@ForAll Interval32Box b, @ForAll Local l) {
        Grimp g = Grimp.v();
        IntConstant lower = IntConstant.v(b.lowerBoundOrElse());
        IntConstant upper = IntConstant.v(b.upperBoundOrElse());
        return this.solver.equals(g.newAndExpr(g.newGeExpr(l, lower),
                                               g.newLeExpr(l, upper)),
                                  b.toGrimpExpr(l));
    }
}
