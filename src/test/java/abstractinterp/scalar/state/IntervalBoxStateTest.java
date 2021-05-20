package abstractinterp.scalar.state;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.IntConstant;
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
    void testUpdateConditionReturnsTopWhenUnbounded() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Set<Local> states = new HashSet<>();
        states.add(l0);
        IntervalBoxState inState = new IntervalBoxState(states, true);
        IntervalBoxState out1 = new IntervalBoxState(states, true);
        states.forEach(l -> inState.update(l, new Interval32Box(null, 1)));
        out1.updateCond(inState, l0, IntConstant.v(3), PredicateType.Invalid);
        states.forEach(l -> {
                Assertions.assertEquals(Interval32Box.TOP(), out1.getValue(l));
            });
        IntervalBoxState out2 = new IntervalBoxState(states, true);
        out2.updateCond(inState, IntConstant.v(3), l0, PredicateType.Invalid);
        states.forEach(l -> {
                Assertions.assertEquals(Interval32Box.TOP(), out2.getValue(l));
            });
    }

    @Test
    void testUpdateConditionReturnsBottomWhenBottomValue() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Set<Local> states = new HashSet<>();
        states.add(l0);
        IntervalBoxState inState = new IntervalBoxState(states, false);
        states.forEach(l -> inState.update(l, Interval32Box.BOT()));
        IntervalBoxState out1 = new IntervalBoxState(states, true);
        out1.updateCond(inState, l0, IntConstant.v(4), PredicateType.Invalid);
        states.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(), out1.getValue(l));
            });
        IntervalBoxState out2 = new IntervalBoxState(states, true);
        out2.updateCond(inState, IntConstant.v(4), l0, PredicateType.Invalid);
        states.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(), out2.getValue(l));
            });
    }

    @Test
    void transferBinaryReturnBottomWhenBottomValue() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = Interval32Box.BOT();
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 5);
        Assertions.assertEquals(Interval32Box.BOT(), z);
        z = IntervalBoxState.transferBinary(y, x, (byte) 5);
        Assertions.assertEquals(Interval32Box.BOT(), z);
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

    @Test
    void testMergeWith() {
        Map<Local, Interval32Box> state = new HashMap<>();
        state.put(Jimple.v().newLocal("l0", IntType.v()), new Interval32Box(-5, 5));
        Set<Local> locals = state.keySet();
        IntervalBoxState box = new IntervalBoxState(locals, false);
        state.forEach((l, b) -> box.update(l, b));
        IntervalBoxState merge = new IntervalBoxState(locals, false);
        //locals.forEach(l -> merge.update(l, Interval32Box.
        box.mergeWith(merge);
        state.forEach((l, b) -> Assertions.assertEquals(b, box.getValue(l)));
        state.forEach((l, b) -> {
                state.put(l, Interval32Box.TOP());
                merge.update(l, Interval32Box.TOP());
            });
        box.mergeWith(merge);
        state.forEach((l, b) -> Assertions.assertEquals(b, box.getValue(l)));
    }

    @Test
    void testMergeWithFeasiblePaths() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, new Interval32Box(1, 3));
        path1.update(l1, new Interval32Box(3, 5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        path2.update(l0, new Interval32Box(3));
        path2.update(l1, new Interval32Box(5));
        path1.mergeWith(path2);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                path1.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                path1.getValue(l1));
    }

    @Test
    void testMergeWithOneInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, new Interval32Box(1, 3));
        path1.update(l1, new Interval32Box(3, 5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        locals.forEach(l -> path2.update(l, Interval32Box.BOT()));
        path1.mergeWith(path2);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                path1.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                path1.getValue(l1));
        path2.mergeWith(path1);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                path2.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                path2.getValue(l1));
    }

    @Test
    void testMergeTopOnInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, new Interval32Box(3));
        path1.update(l1, new Interval32Box(5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        path2.update(l0, Interval32Box.BOT());
        path2.update(l1, Interval32Box.TOP());
        path1.mergeWith(path2);
        Assertions.assertEquals(new Interval32Box(3),
                                path1.getValue(l0));
        Assertions.assertEquals(Interval32Box.TOP(),
                                path1.getValue(l1));
        path1 = new IntervalBoxState(locals, true);
        path1.update(l0, new Interval32Box(3));
        path1.update(l1, new Interval32Box(5));
        path2.mergeWith(path1);
        Assertions.assertEquals(new Interval32Box(3),
                                path2.getValue(l0));
        Assertions.assertEquals(Interval32Box.TOP(),
                                path2.getValue(l1));
    }

    @Test
    void testMergeWithTwoInfeasiblePaths() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        locals.forEach(l -> path1.update(l, Interval32Box.BOT()));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        locals.forEach(l -> path2.update(l, Interval32Box.BOT()));
        path1.mergeWith(path2);
        locals.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(),
                                        path1.getValue(l));
            });
        path2.mergeWith(path1);
        locals.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(),
                                        path2.getValue(l));
            });
    }
}
