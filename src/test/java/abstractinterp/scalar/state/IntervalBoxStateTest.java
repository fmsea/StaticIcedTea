package abstractinterp.scalar.state;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class IntervalBoxStateTest {

    private SolverWrapper solver;

    @BeforeEach
    void setup() {
        this.solver = new SolverWrapperZ3();
    }

    @Test
    void transferBinaryReturnsTopWhenUnbounded() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = new Interval32Box(1, null);
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 6);
        Assertions.assertEquals(Interval32Box.TOP(), z);
        z = IntervalBoxState.transferBinary(y, x, (byte)5);
        Assertions.assertEquals(Interval32Box.TOP(), z);
    }

    @Test
    void transferConditionReturnsTopWhenUnbounded() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = new Interval32Box(1, null);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 6);
        for (Interval32Box b : zs) {
            Assertions.assertEquals(Interval32Box.TOP(), b);
        }
        zs = IntervalBoxState.transferCond(y, x, (byte) 5);
        for (Interval32Box b : zs) {
            Assertions.assertEquals(Interval32Box.TOP(), b);
        }
    }

    @Test
    void transferBinaryDivisionDivideByZero() {
        Interval32Box x = new Interval32Box(-4, 3);
        Interval32Box y = new Interval32Box(0, 0);
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 3);
        Assertions.assertTrue(z.isMax());
        x = new Interval32Box(y);
        z = IntervalBoxState.transferBinary(x, y, (byte) 3);
        Assertions.assertTrue(z.isMax());
    }

    @Test
    void testToSMTFormula() {
        Set<Local> locals = new HashSet<>();
        locals.add(Jimple.v().newLocal("l0", IntType.v()));
        IntervalBoxState box = new IntervalBoxState(locals, false);
        locals.forEach(l -> box.update(l, new Interval32Box(-5, 5)));
        Assertions.assertEquals("l0->(and (>= l0 (- 5)) (<= l0 5))\n", box.toSMTFormula(this.solver));
    }
}
