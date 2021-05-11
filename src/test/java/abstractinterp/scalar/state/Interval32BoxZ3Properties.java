package abstractinterp.scalar.state;

import soot.IntType;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class Interval32BoxZ3Properties {
    private SolverWrapper solver;

    @BeforeProperty
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Property
    boolean testToGrimpExprEqualsZ3SMT(@ForAll Interval32Box b, @ForAll Local l) {
        Grimp g = Grimp.v();
        IntConstant lower = IntConstant.v(b.lowerBound());
        IntConstant upper = IntConstant.v(b.upperBound());
        return this.solver.equals(g.newAndExpr(g.newGeExpr(l, lower),
                                               g.newLeExpr(l, upper)),
                                  b.toGrimpExpr(l));
    }
}
