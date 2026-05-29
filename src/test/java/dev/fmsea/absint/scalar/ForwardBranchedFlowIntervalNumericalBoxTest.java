package dev.fmsea.absint.scalar;

import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import dev.fmsea.absint.scalar.state.IntervalBoxState;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.factory.IntervalBoxStateFactory;

public class ForwardBranchedFlowIntervalNumericalBoxTest {

    private ForwardBranchedFlowNumerical<IntervalBoxState> flow;

    @BeforeEach
    void setup() {
        this.flow = new ForwardBranchedFlowNumerical<>(null,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       new HashSet<>(),
                                                       2,
                                                       null,
                                                       new IntervalBoxStateFactory(),
                                                       null);
    }

    @AfterEach
    void teardown() {
        this.flow = null;
    }

    @Test
    void testMergeWithFeasiblePaths() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, Interval32Box.of(1, 3));
        path1.update(l1, Interval32Box.of(3, 5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        path2.update(l0, Interval32Box.of(3));
        path2.update(l1, Interval32Box.of(5));
        IntervalBoxState out = new IntervalBoxState(locals, true);
        this.flow.merge(path1, path2, out);
        assertAll(() -> assertEquals(Interval32Box.of(1, 3), out.getValue(l0)),
                  () -> assertEquals(Interval32Box.of(3, 5), out.getValue(l1)));
    }

    @Test
    void testMergeWithOneInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, Interval32Box.of(1, 3));
        path1.update(l1, Interval32Box.of(3, 5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        locals.forEach(l -> path2.update(l, Interval32Box.BOT()));
        {
            IntervalBoxState out = new IntervalBoxState(locals, true);
            this.flow.merge(path1, path2, out);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), out.getValue(l0)),
                      () -> assertEquals(Interval32Box.of(3, 5), out.getValue(l1)));
        }

        {
            IntervalBoxState out = new IntervalBoxState(locals, true);
            this.flow.merge(path2, path1, out);
            assertAll(() -> assertEquals(Interval32Box.of(1, 3), out.getValue(l0)),
                      () -> assertEquals(Interval32Box.of(3, 5), out.getValue(l1)));
        }
    }

    @Test
    void testMergeTopOnInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        IntervalBoxState path1 = new IntervalBoxState(locals, true);
        path1.update(l0, Interval32Box.of(3));
        path1.update(l1, Interval32Box.of(5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        path2.update(l0, Interval32Box.BOT());
        path2.update(l1, Interval32Box.TOP());
        {
            IntervalBoxState out = new IntervalBoxState(locals, true);
            this.flow.merge(path1, path2, out);
            assertAll(() -> assertEquals(Interval32Box.of(3), out.getValue(l0)),
                      () -> assertEquals(Interval32Box.of(5), out.getValue(l1)));
        }

        {
            IntervalBoxState out = new IntervalBoxState(locals, true);
            this.flow.merge(path2, path1, out);
            assertAll(() -> assertEquals(Interval32Box.of(3), out.getValue(l0)),
                      () -> assertEquals(Interval32Box.of(5), out.getValue(l1)));
        }
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
        IntervalBoxState out1 = new IntervalBoxState(locals, true);
        this.flow.merge(path1, path2, out1);
        assertAll(locals.stream().map(l -> () -> assertEquals(Interval32Box.BOT(),
                                                              out1.getValue(l))));
        IntervalBoxState out2 = new IntervalBoxState(locals, true);
        this.flow.merge(path2, path1, out2);
        assertAll(locals.stream().map(l -> () -> assertEquals(Interval32Box.BOT(),
                                                              out2.getValue(l))));
    }
}
