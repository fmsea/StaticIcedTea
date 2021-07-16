package abstractinterp.scalar;

import java.util.Set;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.Interval32Box;
import abstractinterp.scalar.state.factory.IntervalBoxStateFactory;

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
                                                       new IntervalBoxStateFactory());
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
        path1.update(l0, new Interval32Box(1, 3));
        path1.update(l1, new Interval32Box(3, 5));
        IntervalBoxState path2 = new IntervalBoxState(locals, true);
        path2.update(l0, new Interval32Box(3));
        path2.update(l1, new Interval32Box(5));
        IntervalBoxState out = new IntervalBoxState(locals, true);
        this.flow.merge(path1, path2, out);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                out.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                out.getValue(l1));
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
        IntervalBoxState out = new IntervalBoxState(locals, true);
        this.flow.merge(path1, path2, out);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                out.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                out.getValue(l1));
        out = new IntervalBoxState(locals, true);
        this.flow.merge(path2, path1, out);
        Assertions.assertEquals(new Interval32Box(1, 3),
                                out.getValue(l0));
        Assertions.assertEquals(new Interval32Box(3, 5),
                                out.getValue(l1));
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
        IntervalBoxState out = new IntervalBoxState(locals, true);
        this.flow.merge(path1, path2, out);
        Assertions.assertEquals(new Interval32Box(3),
                                out.getValue(l0));
        Assertions.assertEquals(new Interval32Box(5),
                                out.getValue(l1));
        out = new IntervalBoxState(locals, true);
        this.flow.merge(path2, path1, out);
        Assertions.assertEquals(new Interval32Box(3),
                                out.getValue(l0));
        Assertions.assertEquals(new Interval32Box(5),
                                out.getValue(l1));
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
        locals.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(),
                                        out1.getValue(l));
            });
        IntervalBoxState out2 = new IntervalBoxState(locals, true);
        this.flow.merge(path2, path1, out2);
        locals.forEach(l -> {
                Assertions.assertEquals(Interval32Box.BOT(),
                                        out2.getValue(l));
            });
    }
}
