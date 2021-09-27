package abstractinterp.scalar.state;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.jqwik.api.ForAll;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotEmpty;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeProperty;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

import abstractinterp.scalar.state.providers.LocalProvider;

public class DifferenceBoundedStateProperties {

    private Local[] xs;
    private Set<Local> locals;

    @BeforeProperty
    void setupLocals() {
        this.xs = new Local[] {
            Jimple.v().newLocal("x0", IntType.v()),
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        this.locals = new HashSet<>();
        for (Local x : this.xs) {
            this.locals.add(x);
        }
    }

    @Property
    void copyToDoesNotLoseVerticies(@ForAll Set<Local> locals) {
        DifferenceBoundedState source = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState target = new DifferenceBoundedState(new HashSet<>(), false);
        source.copyTo(target);
        int expected = locals.size() + 1;
        assertEquals(expected, target.getLocals().size());
    }

    @Property
    void copyToBringsEdges(@ForAll @NotEmpty @UniqueElements(by = LocalProvider.Unique.class) Set<Local> locals,
                           @ForAll Constraint c) {
        DifferenceBoundedState source = new DifferenceBoundedState(locals, true);
        Local[] xs = locals.toArray(new Local[locals.size()]);
        int N = locals.size();
        int N2 = N / 2;
        for (int i = 0; i < N2; i++) {
            source.add(xs[i], xs[(i + 1)], c);
        }

        Consumer<DifferenceBoundedState> checkAssertions = (target) -> {
            assertAll(Stream.concat(IntStream.range(0, N2)
                                    .mapToObj(i -> () ->
                                              assertEquals(c, target.getConstraint(xs[i], xs[(i + 1)]))),
                                    locals.stream().map(l -> () ->
                                                        assertEquals(Constraint.TOP(),
                                                                     target.getConstraint(l)))));
        };
        {
            DifferenceBoundedState target = new DifferenceBoundedState(source);
            checkAssertions.accept(target);
        }

        {
            DifferenceBoundedState target = new DifferenceBoundedState(locals, false);
            source.copyTo(target);
            checkAssertions.accept(target);
        }
    }

    @Property
    void negativeCyclesAreInfeasible(@ForAll @IntRange(min = -524288, max = 524288) int a,
                                     @ForAll @IntRange(min = -524288, max = 524288) int b,
                                     @ForAll @IntRange(min = -524288, max = 524288) int c) {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], new Constraint(a, PredicateType.Le));
        state.add(DifferenceBoundedState.ZERO, xs[1], new Constraint(b, PredicateType.Le));
        state.add(xs[1], xs[0], new Constraint(c, PredicateType.Le));
        assertEquals(a + b + c >= 0, state.isFeasible());
    }

    @Property
    void forgetConstraintsDoesNotLoseVertices(@ForAll @NotEmpty Set<Local> locals) {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        locals.forEach(l -> {
                locals.forEach(k -> {
                        if (l.equals(k)) {
                            state.add(l, k, Constraint.ZERO());
                        } else {
                            state.add(l, k, new Constraint(6, PredicateType.Le));
                        }
                    });
            });
        Local[] xs = locals.toArray(new Local[locals.size()]);
        state.forgetConstraints(xs[0]);
        assertEquals(locals.size() + 1, state.getLocals().size());
    }

    @Property
    void updateStateConstantAssignment(@ForAll int x, @ForAll int y) {
        Set<Local> locals = new HashSet<>();
        Local x0 = Jimple.v().newLocal("x0", IntType.v());
        locals.add(x0);
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(x0, new Constraint(x, PredicateType.Eq));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(x0, inState, IntConstant.v(y));
        assertEquals(new Constraint(y, PredicateType.Eq), state.getConstraint(x0));
    }

    @Property
    void updateStateAliasAssignment(@ForAll Local a, @ForAll Local b) {
        Set<Local> locals = new HashSet<>();
        locals.add(a);
        locals.add(b);
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.updateState(a, inState, b);
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        assertEquals(new Constraint(0, PredicateType.Eq), state.getConstraint(a, b));
    }

    @Property
    void updateStateUpdateRelationsAddition(@ForAll @IntRange(min = -536870911, max = 536870911) int w,
                                            @ForAll @IntRange(min = -536870911, max = 536870911) int x,
                                            @ForAll @IntRange(min = -536870911, max = 536870911) int y,
                                            @ForAll @IntRange(min = -536870911, max = 536870911) int z) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(x, PredicateType.Le));
        inState.add(xs[2], xs[3], new Constraint(y, PredicateType.Eq));
        inState.add(xs[3], xs[0], new Constraint(z, PredicateType.Le));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[0], inState, xs[0], IntConstant.v(w), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(new Constraint(x + w, PredicateType.Le),
                                     state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(new Constraint(y, PredicateType.Eq),
                                     state.getConstraint(xs[2], xs[3])),
                  () -> assertEquals(new Constraint(z - w, PredicateType.Le),
                                     state.getConstraint(xs[3], xs[0])));
    }

    @Property
    void testIncrementalClosure(@ForAll @IntRange(min = -536870911, max = 536870911) int c,
                                @ForAll @IntRange(min = -536870911, max = 536870911) int k,
                                @ForAll @IntRange(min = -536870911, max = 536870911) int w,
                                @ForAll @IntRange(min = -536870911, max = 536870911) int v) {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(k, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(w, PredicateType.Le));
        state.add(xs[3], new Constraint(v, PredicateType.Eq));
        assertAll(locals.stream()
                  .map(x -> () -> assertTrue(state.incrementalClosure(x, state))));
        assertAll(() -> assertEquals(new Constraint(v + w, PredicateType.Le),
                                     state.getConstraint(xs[2])),
                  () -> assertEquals(new Constraint(v + w + k, PredicateType.Le),
                                     state.getConstraint(xs[1])),
                  () -> assertEquals(new Constraint(c + k + w + v, PredicateType.Le),
                                     state.getConstraint(xs[0])));
    }

    @Property
    void updateVariableAddition(@ForAll @IntRange(min = -536870911, max = 536870911) int c,
                                @ForAll @IntRange(min = -536870911, max = 536870911) int k) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(k), BinaryOperatorType.ADDITION);
            assertEquals(new Constraint(c + k, PredicateType.Le),
                         state.getConstraint(xs[0], xs[1]));

        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(k), xs[0], BinaryOperatorType.ADDITION);
            assertEquals(new Constraint(c + k, PredicateType.Le),
                         state.getConstraint(xs[0], xs[1]));
        }
    }

    @Property
    void updateVariableSubtraction(@ForAll @IntRange(min = -536870911, max = 536870911) int c,
                                   @ForAll @IntRange(min = -536870911, max = 536870911) int k) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(k), BinaryOperatorType.SUBTRACTION);
            assertEquals(new Constraint(c - k, PredicateType.Le),
                         state.getConstraint(xs[0], xs[1]));

        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(k), xs[0], BinaryOperatorType.SUBTRACTION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1]));
        }
    }

    @Property
    void updateVariableMultiplication(@ForAll int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(c), BinaryOperatorType.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(c), xs[0], BinaryOperatorType.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }
    }

    @Property
    void updateVariableDivision(@ForAll int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(c), BinaryOperatorType.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(c), xs[0], BinaryOperatorType.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }
    }

    @Property
    void transferTwoVariablesAddition(@ForAll int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperatorType.ADDITION);
            assertEquals(new Constraint(c, PredicateType.Le),
                         state.getConstraint(xs[2], DifferenceBoundedState.ZERO));
        }
    }

    @Property
    void transferTwoVariablesSubtraction(@ForAll @IntRange(min = -536870911, max = 536870911) int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperatorType.SUBTRACTION);
            assertEquals(new Constraint(c * -1, PredicateType.Le),
                         state.getConstraint(xs[2]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperatorType.SUBTRACTION);
            assertEquals(new Constraint(c, PredicateType.Le), state.getConstraint(xs[2]));
        }
    }

    @Property
    void transferTwoVariablesMultiplication(@ForAll int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperatorType.MULTIPLICATION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[2]));
        }
    }

    @Property
    void transferTwoVariablesDivision(@ForAll int c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(c, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperatorType.DIVISION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[2]));
        }
    }

    @Property
    void assignmentKillsExistingRelationships(@ForAll Constraint a, @ForAll Constraint b,
                                              @ForAll Constraint c) {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], a);
        inState.add(xs[2], xs[0], b);
        inState.add(xs[0], xs[3], c);
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(3));
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[3])));
        }
    }
}
