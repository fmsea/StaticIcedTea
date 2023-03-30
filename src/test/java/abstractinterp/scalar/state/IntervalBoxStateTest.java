package abstractinterp.scalar.state;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import soot.Local;
import soot.IntType;
import soot.jimple.BinopExpr;
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
        {
            Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.INVALID);
            assertEquals(Interval32Box.TOP(), z);
        }
        {
            Interval32Box z = IntervalBoxState.transferBinary(y, x, BinaryOperatorType.INVALID);
            assertEquals(Interval32Box.TOP(), z);
        }
    }

    @Test
    void testUpdateConditionReturnsBottomWhenBottomValue() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Set<Local> states = new HashSet<>();
        states.add(l0);
        IntervalBoxState inState = new IntervalBoxState(states, false);
        states.forEach(l -> inState.update(l, Interval32Box.BOT()));
        {
            IntervalBoxState out = new IntervalBoxState(states, true);
            out.updateCond(inState, l0, IntConstant.v(4), PredicateType.Invalid);
            assertAll(states.stream().map(l -> () -> assertEquals(Interval32Box.BOT(), out.getValue(l))));
        }
        {
            IntervalBoxState out = new IntervalBoxState(states, true);
            out.updateCond(inState, IntConstant.v(4), l0, PredicateType.Invalid);
            assertAll(states.stream().map(l -> () -> assertEquals(Interval32Box.BOT(), out.getValue(l))));
        }
    }

    @Test
    void transferBinaryReturnBottomWhenBottomValue() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = Interval32Box.BOT();
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.INVALID);
        assertEquals(Interval32Box.BOT(), z);
        z = IntervalBoxState.transferBinary(y, x, BinaryOperatorType.INVALID);
        assertEquals(Interval32Box.BOT(), z);
    }

    @Test
    void transferBinaryDivisionDivideByZero() {
        Interval32Box x = new Interval32Box(-4, 3);
        Interval32Box y = new Interval32Box(0, 0);
        Interval32Box z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.DIVISION);
        assertTrue(z.isTop());
        x = new Interval32Box(y);
        z = IntervalBoxState.transferBinary(x, y, BinaryOperatorType.DIVISION);
        assertTrue(z.isTop());
    }

    @Test
    void testToSMT() {
        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, false);
            locals.forEach(l -> box.update(l, new Interval32Box(-5, 5)));
            assertEquals("(and (>= l0 (- 5)) (<= l0 5))\n", box.toSMT(this.solver));
        }

        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, false);
            assertEquals("false\n", box.toSMT(this.solver));
        }

        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, true);
            assertEquals("true\n", box.toSMT(this.solver));
        }
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
        state.forEach((l, b) -> assertEquals(b, box.getValue(l)));
        state.forEach((l, b) -> {
                state.put(l, Interval32Box.TOP());
                merge.update(l, Interval32Box.TOP());
            });
        box.mergeWith(merge);
        assertAll(state.entrySet()
                  .stream()
                  .map((s) -> () -> assertEquals(s.getValue(),
                                                 box.getValue(s.getKey()))));
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
        assertAll(() -> assertEquals(new Interval32Box(1, 3),
                                     path1.getValue(l0)),
                  () -> assertEquals(new Interval32Box(3, 5),
                                     path1.getValue(l1)));
    }

    @Test
    void testMergeWithOneInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        {
            IntervalBoxState path1 = new IntervalBoxState(locals, true);
            IntervalBoxState path2 = new IntervalBoxState(locals, true);
            path1.update(l0, new Interval32Box(1, 3));
            path1.update(l1, new Interval32Box(3, 5));
            locals.forEach(l -> path2.update(l, Interval32Box.BOT()));
            path1.mergeWith(path2);
            assertAll(() -> assertEquals(new Interval32Box(1, 3),
                                         path1.getValue(l0)),
                      () -> assertEquals(new Interval32Box(3, 5),
                                         path1.getValue(l1)));
        }

        {
            IntervalBoxState path1 = new IntervalBoxState(locals, true);
            IntervalBoxState path2 = new IntervalBoxState(locals, true);
            path1.update(l0, new Interval32Box(1, 3));
            path1.update(l1, new Interval32Box(3, 5));
            locals.forEach(l -> path2.update(l, Interval32Box.BOT()));
            path2.mergeWith(path1);
            assertAll(() -> assertEquals(new Interval32Box(1, 3),
                                         path2.getValue(l0)),
                      () -> assertEquals(new Interval32Box(3, 5),
                                         path2.getValue(l1)));
        }
    }

    @Test
    void testMergeTopOnInfeasiblePath() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Set<Local> locals = new HashSet<>();
        locals.add(l0);
        locals.add(l1);
        {
            IntervalBoxState path1 = new IntervalBoxState(locals, true);
            path1.update(l0, new Interval32Box(3));
            path1.update(l1, new Interval32Box(5));
            IntervalBoxState path2 = new IntervalBoxState(locals, true);
            path2.update(l0, Interval32Box.BOT());
            path2.update(l1, Interval32Box.TOP());
            path1.mergeWith(path2);
            assertAll(() -> assertEquals(new Interval32Box(3),
                                         path1.getValue(l0)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         path1.getValue(l1)));
        }
        {
            IntervalBoxState path1 = new IntervalBoxState(locals, true);
            path1.update(l0, new Interval32Box(3));
            path1.update(l1, new Interval32Box(5));
            IntervalBoxState path2 = new IntervalBoxState(locals, true);
            path2.update(l0, Interval32Box.BOT());
            path2.update(l1, Interval32Box.TOP());
            path2.mergeWith(path1);
            assertAll(() -> assertEquals(new Interval32Box(3),
                                         path2.getValue(l0)),
                      () -> assertEquals(Interval32Box.TOP(),
                                         path2.getValue(l1)));
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
        path1.mergeWith(path2);
        assertAll(locals.stream().map(l -> () -> assertEquals(Interval32Box.BOT(),
                                                              path1.getValue(l))));
        path2.mergeWith(path1);

        assertAll(locals.stream().map(l -> () -> assertEquals(Interval32Box.BOT(),
                                                              path2.getValue(l))));
    }

    @Test
    void testToBinop() {
        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, false);
            locals.forEach(l -> box.update(l, new Interval32Box(-5, 5)));
            assertEquals("(and (>= l0 (- 5)) (<= l0 5))", this.solver.smt2(box.toBinop().get()));
        }

        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, false);
            assertEquals("(= 0 1)", this.solver.smt2(box.toBinop().get()));
        }

        {
            Set<Local> locals = new HashSet<>();
            locals.add(Jimple.v().newLocal("l0", IntType.v()));
            IntervalBoxState box = new IntervalBoxState(locals, true);
            assertTrue(box.toBinop().isEmpty());
        }
    }

    // @ParameterizedTest
    // @MethodSource("provideChangedVariablesSubgraphs")
    // void testChangedVariablesSubgraph(IntervalBoxState state,
    //                                   Set<Local> changedVariables,
    //                                   String smtFormula) {
    //     assertAll(() -> assertEquals(changedVariables, state.getChangedVariables()),
    //               () -> assertEquals(changedVariables, state.getChangedVariablesSubgraph()),
    //               () -> assertEquals(smtFormula, state.toChangedVariablesSMT(this.solver)));
    // }

    // static Stream<Arguments> provideChangedVariablesSubgraphs() {
    //     Local[] xs = new Local[] {
    //         Jimple.v().newLocal("x0", IntType.v()),
    //         Jimple.v().newLocal("x1", IntType.v()),
    //         Jimple.v().newLocal("x2", IntType.v()),
    //     };
    //     Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
    //     IntervalBoxState[] states = new IntervalBoxState[] {
    //         new IntervalBoxState(locals, false),
    //         new IntervalBoxState(locals, true),
    //         new IntervalBoxState(locals, true),
    //         new IntervalBoxState(locals, true),
    //     };
    //     states[2].update(xs[0], Interval32Box.of(3));
    //     states[3].updateCond(states[3], xs[1], xs[2], PredicateType.Le);
    //     return Stream.of(Arguments.arguments(states[0], Set.of(), "false"),
    //                      Arguments.arguments(states[1], Set.of(), "true"),
    //                      Arguments.arguments(states[2], Set.of(xs[0]), "(= x0 3)"),
    //                      Arguments.arguments(states[3], Set.of(xs[1], xs[2]), "true"));
    // }

    @Test
    void testWidening() {
        Local[] xs = new Local[] {
            Jimple.v().newLocal("x0", IntType.v()),
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        {
            IntervalBoxState m = new IntervalBoxState(locals, true);
            m.update(xs[0], Interval32Box.of(0));
            m.update(xs[1], Interval32Box.of(-1, 1));
            m.update(xs[2], Interval32Box.of(0, 2));
            m.update(xs[3], Interval32Box.of(-2, -1));
            IntervalBoxState n = new IntervalBoxState(locals, true);
            n.update(xs[0], Interval32Box.of(0));
            n.update(xs[1], Interval32Box.of(-2, 2));
            n.update(xs[2], Interval32Box.of(0, 3));
            n.update(xs[3], Interval32Box.of(-3, -1));
            m.widenWith(n, Set.of(10));
            assertAll(() -> assertEquals(Interval32Box.of(0),
                                         m.getValue(xs[0])),
                      () -> assertEquals(Interval32Box.of(-10, 10),
                                         m.getValue(xs[1])),
                      () -> assertEquals(Interval32Box.of(0, 10),
                                         m.getValue(xs[2])),
                      () -> assertEquals(Interval32Box.of(-10, -1),
                                         m.getValue(xs[3])));
        }
    }
}
