package abstractinterp.scalar.state;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class DifferenceBoundedStateTest {

    private Set<Local> locals;
    private Local[] xs;
    private Local ZERO = DifferenceBoundedState.ZERO;
    private SolverWrapper solver;

    @BeforeEach
    void setupSolver() {
        this.solver = new SolverWrapperZ3();
    }

    @BeforeEach
    void setupLocals() {
        this.xs = new Local[] {
            Jimple.v().newLocal("x0", IntType.v()),
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        this.locals = new HashSet<>();
        for (Local x : this.xs) { this.locals.add(x); }
    }

    @Test
    void testCopyTo() {
        DifferenceBoundedState source = new DifferenceBoundedState(this.locals, true);
        Constraint c = new Constraint(0, PredicateType.Eq);
        int N = locals.size();
        int N2 = N / 2;
        for (int i = 0; i < N2; i++) {
            source.add(xs[i], xs[i + 1], c.copy());
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

    @Test
    void testCopyTop() {
        DifferenceBoundedState source = new DifferenceBoundedState(this.locals, true);
        source.add(xs[0], xs[1], Constraint.TOP());
        source.add(xs[1], xs[2], Constraint.TOP());
        source.add(xs[2], xs[3], new Constraint(3));

        Consumer<DifferenceBoundedState> check = (state) -> {
            assertAll("copyTo copies TOP edges",
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1], xs[2])),
                      () -> assertTrue(state.getValue(xs[2], xs[3]).isPresent()),
                      () -> assertEquals(new Constraint(3), state.getConstraint(xs[2], xs[3])));
        };

        {
            DifferenceBoundedState target = new DifferenceBoundedState(source);
            check.accept(target);
        }

        {
            DifferenceBoundedState target = new DifferenceBoundedState(this.locals, false);
            source.copyTo(target);
            check.accept(target);
        }
    }

    @Test
    void testIsFeasible() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        assertTrue(state.isFeasible());
        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(xs[0], new Constraint(0));
            assertTrue(test.isFeasible());
        }
        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(xs[0], new Constraint(0));
            test.add(ZERO, xs[0], new Constraint(-1, PredicateType.Le));
            assertFalse(test.isFeasible());
        }

        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(xs[0], xs[1], new Constraint(0));
            assertTrue(test.isFeasible());
            test.add(xs[1], xs[0], new Constraint(-1, PredicateType.Le));
            assertFalse(test.isFeasible());
        }

        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(xs[0], new Constraint(6, PredicateType.Le));
            test.add(ZERO, xs[1], new Constraint(-11, PredicateType.Le));
            test.add(xs[1], xs[0], new Constraint(4, PredicateType.Le));
            assertFalse(test.isFeasible());
        }

        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(ZERO, xs[0], new Constraint(1, PredicateType.Le));
            test.add(xs[0], xs[1], new Constraint(1, PredicateType.Le));
            test.add(xs[1], xs[2], new Constraint(2, PredicateType.Le));
            test.add(xs[2], new Constraint(-5, PredicateType.Le));
            assertFalse(test.isFeasible());
        }

        {
            DifferenceBoundedState test = new DifferenceBoundedState(state);
            test.add(xs[0], xs[1], new Constraint(1, PredicateType.Le));
            test.add(xs[1], xs[2], new Constraint(2, PredicateType.Le));
            test.add(xs[2], xs[3], new Constraint(1, PredicateType.Le));
            test.add(xs[3], xs[1], new Constraint(-4, PredicateType.Le));
            assertFalse(test.isFeasible());
        }
    }

    @Test
    void testConstantTransfer() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        assertEquals(Constraint.TOP(), state.getConstraint(xs[0]));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, IntConstant.v(1));
        assertEquals(new Constraint(1, PredicateType.Eq),
                     state.getConstraint(xs[0]));
        state.updateState(xs[0], inState, IntConstant.v(-1));
        assertEquals(new Constraint(-1, PredicateType.Eq),
                     state.getConstraint(xs[0]));
    }

    @Test
    void testAliasAssignment() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, xs[1]);
        assertEquals(new Constraint(0, PredicateType.Eq),
                     state.getConstraint(xs[0], xs[1]));
    }

    @Test
    void testTransferBinaryAddition() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(3), BinaryOperator.ADDITION);
            assertEquals(new Constraint(6, PredicateType.Le),
                         state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(3), xs[0], BinaryOperator.ADDITION);
            assertEquals(new Constraint(6, PredicateType.Le),
                         state.getConstraint(xs[0], xs[1]));
        }
    }

    @Test
    void testTransferBinarySubtraction() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(3), BinaryOperator.SUBTRACTION);
            assertEquals(new Constraint(0, PredicateType.Le),state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state = new DifferenceBoundedState(locals, true);
            state.updateState(xs[0], inState, IntConstant.v(3), xs[0], BinaryOperator.SUBTRACTION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1]));
        }
    }

    @Test
    void testTransferBinaryMultiplication() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(2), xs[0], BinaryOperator.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }
    }

    @Test
    void testTransferBinaryDivision() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[0], inState, IntConstant.v(2), xs[0], BinaryOperator.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
        }
    }

    @Test
    void testSingleVariableAddition() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[1], inState, xs[0], IntConstant.v(1), BinaryOperator.ADDITION);
        assertAll("single variable subtraction without interval",
                  () -> assertEquals(new Constraint(1, PredicateType.Eq),
                                     state.getConstraint(xs[1], xs[0])));
    }

    @Test
    void testSingleVariableSubtraction() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[1], inState, xs[0], IntConstant.v(1), BinaryOperator.SUBTRACTION);
            assertAll("subtraction with interval",
                      () -> assertEquals(new Constraint(-1, PredicateType.Eq),
                                         state.getConstraint(xs[1], xs[0])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, IntConstant.v(2), xs[0], BinaryOperator.SUBTRACTION);
            assertAll("subtraction with interval",
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(new Constraint(-5, PredicateType.Le),
                                         state.getConstraint(xs[2])));
        }
    }

    @Test
    void testSingleVariableMultiplication() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[1], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
        assertAll("single variable multiplication without interval",
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1])));
    }

    @Test
    void testSingleVariableMultiplicationIncrementalClosure() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
            assertAll("single variable multiplication with interval",
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(new Constraint(14, PredicateType.Le),
                                         state.getConstraint(xs[2])));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, IntConstant.v(2), xs[0], BinaryOperator.MULTIPLICATION);
            assertAll("single variable multiplication with interval",
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(new Constraint(14, PredicateType.Le),
                                         state.getConstraint(xs[2])));
        }
    }

    @Test
    void testSingleVariableDivision() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[1], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
        assertAll("single variable division without interval",
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1])));
    }

    @Test
    void testSingleVariableDivisionIncrementalClosure() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
            assertAll("single variable division with interval",
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(new Constraint(3, PredicateType.Le),
                                         state.getConstraint(xs[2])));

        }
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, IntConstant.v(14), xs[0], BinaryOperator.DIVISION);
            assertAll("single variable division with interval",
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(new Constraint(2, PredicateType.Le), state.getConstraint(xs[2])));
        }
    }

    @Test
    void testTransferInfersInterval() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], new Constraint(5, PredicateType.Eq));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.ADDITION);
        assertAll("transfer infers interval",
                  () -> assertEquals(new Constraint(5, PredicateType.Le),
                                     state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(new Constraint(5, PredicateType.Eq),
                                     state.getConstraint(xs[1])),
                  () -> assertEquals(new Constraint(10, PredicateType.Le),
                                     state.getConstraint(xs[0])));
    }

    @Test
    void testIncrementalClosure() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(1, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(2, PredicateType.Le));
        state.add(xs[3], new Constraint(4, PredicateType.Eq));
        for (Local x : xs) {
            state.incrementalClosure(x, state);
        }
        assertAll("Incremental Closure",
                  () -> assertEquals(new Constraint(6, PredicateType.Le),
                                     state.getConstraint(xs[2])),
                  () -> assertEquals(new Constraint(7, PredicateType.Le),
                                     state.getConstraint(xs[1])),
                  () -> assertEquals(new Constraint(10, PredicateType.Le),
                                     state.getConstraint(xs[0])));
    }

    @Test
    void testIncrementalClosureOverflow() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(0, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(Integer.MAX_VALUE, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(0, PredicateType.Le));
        state.add(xs[3], new Constraint(1, PredicateType.Eq));
        for (Local x : xs) {
            state.incrementalClosure(x, state);
        }
        assertAll(() -> assertEquals(new Constraint(1, PredicateType.Le),
                                     state.getConstraint(xs[2])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[1])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0])));
    }

    @Test
    void testIncrementalClosureWhenNoEdgesToZero() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(1, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(2, PredicateType.Le));
        for (Local x : xs) {
            state.incrementalClosure(x, state);
        }
        assertAll(locals.stream().map(l -> () -> assertEquals(Constraint.TOP(), state.getConstraint(l))));
    }

    @Test
    void testTransferTwoVariablesAddition() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperator.ADDITION);
            assertEquals(new Constraint(3, PredicateType.Le), state.getConstraint(xs[2]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.ADDITION);
            assertEquals(new Constraint(3, PredicateType.Le), state.getConstraint(xs[2]));
        }
    }

    @Test
    void testTransferTwoVariablesSubtraction() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperator.SUBTRACTION);
            assertEquals(new Constraint(-3, PredicateType.Le), state.getConstraint(xs[2]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.SUBTRACTION);
            assertEquals(new Constraint(3, PredicateType.Le),
                         state.getConstraint(xs[2], ZERO));
        }
    }

    @Test
    void testTransferTwoVariablesMultiplication() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.MULTIPLICATION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[2]));
        }
    }

    @Test
    void testTransferTwoVariablesDivision() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        {
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.DIVISION);
            assertEquals(Constraint.TOP(), state.getConstraint(xs[2]));
        }
    }

    @Test
    void testTransferTwoVariableAdditionInIntervals() {
        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
            inState.add(xs[1], new Constraint(4, PredicateType.Eq));
            inState.add(xs[2], new Constraint(3, PredicateType.Eq));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.ADDITION);
            assertEquals(new Constraint(7, PredicateType.Eq),
                         state.getConstraint(xs[3], ZERO));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], xs[0], new Constraint(-3, PredicateType.Le));
            inState.add(ZERO, xs[1], new Constraint(-4, PredicateType.Eq));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.ADDITION);
            assertEquals(new Constraint(-7, PredicateType.Le),
                         state.getConstraint(ZERO, xs[3]));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], ZERO, new Constraint(4));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.ADDITION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(ZERO, xs[1], new Constraint(-4));
            inState.add(xs[2], ZERO, new Constraint(3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.ADDITION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }
    }

    @Test
    void testTransferTwoVariableSubtractionInIntervals() {
        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
            inState.add(xs[1], new Constraint(4, PredicateType.Eq));
            inState.add(xs[2], new Constraint(3, PredicateType.Eq));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.SUBTRACTION);
            assertEquals(new Constraint(1, PredicateType.Eq),
                         state.getConstraint(xs[3], ZERO));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], xs[0], new Constraint(-3, PredicateType.Le));
            inState.add(ZERO, xs[1], new Constraint(-4, PredicateType.Eq));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.SUBTRACTION);
            assertEquals(new Constraint(-1, PredicateType.Le),
                         state.getConstraint(ZERO, xs[3]));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], ZERO, new Constraint(4));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.SUBTRACTION);
            assertAll(() -> assertEquals(new Constraint(7, PredicateType.Le),
                                         state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(ZERO, xs[1], new Constraint(-4));
            inState.add(xs[2], ZERO, new Constraint(3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.SUBTRACTION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(new Constraint(-7, PredicateType.Le),
                                         state.getConstraint(ZERO, xs[3])));
        }
    }

    @Test
    void testTransferTwoVariableMultiplicationInIntervals() {
        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
            inState.add(xs[1], new Constraint(4, PredicateType.Eq));
            inState.add(xs[2], new Constraint(3, PredicateType.Eq));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.MULTIPLICATION);
            assertEquals(new Constraint(12, PredicateType.Eq),
                         state.getConstraint(xs[3]));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], xs[0], new Constraint(-3, PredicateType.Le));
            inState.add(ZERO, xs[1], new Constraint(-4, PredicateType.Eq));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.MULTIPLICATION);
            assertEquals(new Constraint(-12, PredicateType.Le),
                         state.getConstraint(ZERO, xs[3]));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], ZERO, new Constraint(4));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(ZERO, xs[1], new Constraint(-4));
            inState.add(xs[2], ZERO, new Constraint(3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.MULTIPLICATION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }
    }

    @Test
    void testTransferTwoVariableDivisionInIntervals() {
        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
            inState.add(xs[1], new Constraint(4, PredicateType.Eq));
            inState.add(xs[2], new Constraint(3, PredicateType.Eq));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.DIVISION);
            assertEquals(new Constraint(1, PredicateType.Eq),
                         state.getConstraint(xs[3]));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], xs[0], new Constraint(3, PredicateType.Le));
            inState.add(ZERO, xs[1], new Constraint(-4));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.DIVISION);
            assertEquals(new Constraint(-1, PredicateType.Le),
                         state.getConstraint(ZERO, xs[3]));
        }

                {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(xs[1], ZERO, new Constraint(4));
            inState.add(ZERO, xs[2], new Constraint(-3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }

        {
            DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
            inState.add(ZERO, xs[1], new Constraint(-4));
            inState.add(xs[2], ZERO, new Constraint(3, PredicateType.Le));
            DifferenceBoundedState state = new DifferenceBoundedState(inState);
            state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.DIVISION);
            assertAll(() -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], ZERO)),
                      () -> assertEquals(Constraint.TOP(), state.getConstraint(ZERO, xs[3])));
        }
    }

    @Test
    @DisplayName("updateCondition <=")
    void testUpdateCondLE() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[0], new Constraint(3));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[1], IntConstant.v(2), PredicateType.Le);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(2, PredicateType.Le),
                                         fall.getConstraint(xs[1], ZERO)));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(4), xs[1], PredicateType.Le);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-4, PredicateType.Le),
                                         fall.getConstraint(ZERO, xs[1])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Le);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(0, PredicateType.Le),
                                         fall.getConstraint(xs[2], xs[3])));
        }
    }

    @Test
    @DisplayName("updateCondition <")
    void testUpdateCondLT() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[0], new Constraint(3));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[1], IntConstant.v(2), PredicateType.Lt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(1, PredicateType.Le),
                                         fall.getConstraint(xs[1], ZERO)));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(2), xs[2], PredicateType.Lt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-3, PredicateType.Le),
                                         fall.getConstraint(ZERO, xs[2])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Lt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-1, PredicateType.Le),
                                         fall.getConstraint(xs[2], xs[3])));
        }
    }

    @Test
    @DisplayName("updateCondition ==")
    void testUpdateCondEQ() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Eq);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(2), fall.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(0), xs[2], PredicateType.Eq);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(0), fall.getConstraint(xs[2])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Eq);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(0), fall.getConstraint(xs[2], xs[3])));
        }
    }

    @Test
    @DisplayName("updateCondition !=")
    void testUpdateCondNE() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Ne);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(Constraint.TOP(), fall.getConstraint(xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         fall.getConstraint(ZERO, xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(3), xs[1], PredicateType.Ne);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(Constraint.TOP(), fall.getConstraint(xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         fall.getConstraint(ZERO, xs[1])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Ne);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(Constraint.TOP(), fall.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.TOP(), fall.getConstraint(xs[3], xs[2])));
        }
    }

    @Test
    @DisplayName("updateCondition >=")
    void testUpdateCondGE() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Ge);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-2, PredicateType.Le),
                                         fall.getConstraint(ZERO, xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(2), xs[0], PredicateType.Ge);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(2, PredicateType.Le),
                                         fall.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Ge);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(0, PredicateType.Le),
                                         fall.getConstraint(xs[3], xs[2])));
        }
    }

    @Test
    @DisplayName("updateCondition >")
    void testUpdateCondGT() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Gt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-3, PredicateType.Le),
                                         fall.getConstraint(ZERO, xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, IntConstant.v(4), xs[0], PredicateType.Gt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(5, PredicateType.Le),
                                         fall.getConstraint(xs[0])));
        }

        {
            DifferenceBoundedState fall = new DifferenceBoundedState(inState);
            fall.updateCond(inState, xs[2], xs[3], PredicateType.Gt);
            assertAll(() -> assertTrue(fall.isFeasible()),
                      () -> assertEquals(new Constraint(-1, PredicateType.Le),
                                         fall.getConstraint(xs[3], xs[2])));
        }
    }

    @Test
    @DisplayName("updateCond INVALID")
    void testUpdateCondWhenInvalid() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateCond(inState, xs[0], IntConstant.v(3), PredicateType.Invalid);
        assertFalse(state.isFeasible());
    }

    @Test
    void testSelfMerging() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[3], new Constraint(4));
        state.add(xs[1], xs[3], new Constraint(2));
        state.add(xs[2], xs[0], new Constraint(2));
        state.add(xs[2], xs[1], new Constraint(4));
        state.add(xs[2], xs[3], new Constraint(6));
        assertTrue(state.isFeasible());

        DifferenceBoundedState m = new DifferenceBoundedState(state);

        m.mergeWith(m);

        assertEquals(state, m);
    }

    @Test
    void testDoubleMerging() {
        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);
        state1.add(xs[0], xs[3], new Constraint(4));
        state1.add(xs[1], xs[3], new Constraint(2));
        state1.add(xs[2], xs[0], new Constraint(2));
        state1.add(xs[2], xs[1], new Constraint(4));
        state1.add(xs[2], xs[3], new Constraint(6));

        state2.add(xs[0], xs[3], new Constraint(2));
        state2.add(xs[1], xs[3], new Constraint(4));
        state2.add(xs[2], xs[0], new Constraint(4));
        state2.add(xs[2], xs[1], new Constraint(2));

        DifferenceBoundedState m = new DifferenceBoundedState(state1);
        DifferenceBoundedState n = new DifferenceBoundedState(state2);

        m.mergeWith(n);
        DifferenceBoundedState mMergeN = new DifferenceBoundedState(m);
        m.mergeWith(n);

        assertEquals(mMergeN, m);
    }

    @Test
    void testPathMerging() {

        Consumer<DifferenceBoundedState> check = (state -> {
                assertAll("path join",
                          () -> assertTrue(state.isFeasible()),
                          () -> assertEquals(new Constraint(4, PredicateType.Le),
                                             state.getConstraint(xs[0], xs[3])),
                          () -> assertEquals(new Constraint(4, PredicateType.Le),
                                             state.getConstraint(xs[1], xs[3])),
                          () -> assertEquals(new Constraint(4, PredicateType.Le),
                                             state.getConstraint(xs[2], xs[0])),
                          () -> assertEquals(new Constraint(4, PredicateType.Le),
                                             state.getConstraint(xs[2], xs[1])),
                          () -> assertEquals(new Constraint(6, PredicateType.Le),
                                             state.getConstraint(xs[2], xs[3])));
            });

        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);
        state1.add(xs[0], xs[3], new Constraint(4, PredicateType.Le));
        state1.add(xs[1], xs[3], new Constraint(2, PredicateType.Le));
        state1.add(xs[2], xs[0], new Constraint(2, PredicateType.Le));
        state1.add(xs[2], xs[1], new Constraint(4, PredicateType.Le));
        state1.add(xs[2], xs[3], new Constraint(6, PredicateType.Le));

        state2.add(xs[0], xs[3], new Constraint(2, PredicateType.Le));
        state2.add(xs[1], xs[3], new Constraint(4, PredicateType.Le));
        state2.add(xs[2], xs[0], new Constraint(4, PredicateType.Le));
        state2.add(xs[2], xs[1], new Constraint(2, PredicateType.Le));

        DifferenceBoundedState m = new DifferenceBoundedState(state1);
        DifferenceBoundedState n = new DifferenceBoundedState(state2);

        m.mergeWith(n);

        check.accept(m);

        m = new DifferenceBoundedState(state1);
        n = new DifferenceBoundedState(state2);

        n.mergeWith(m);

        check.accept(n);
    }

    @Test
    void testWidening() {
        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);

        state1.add(xs[0], xs[1], new Constraint(2, PredicateType.Le));
        state1.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        state1.add(xs[2], xs[0], new Constraint(5, PredicateType.Le));
        state1.add(xs[2], xs[3], new Constraint(4, PredicateType.Le));
        state1.add(xs[3], xs[0], new Constraint(1, PredicateType.Le));
        state1.add(xs[3], xs[1], new Constraint(5, PredicateType.Le));

        state2.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state2.add(xs[1], xs[2], new Constraint(3, PredicateType.Le));
        state2.add(xs[2], xs[0], new Constraint(4, PredicateType.Le));
        state2.add(xs[2], xs[3], new Constraint(5, PredicateType.Le));
        state2.add(xs[3], xs[0], new Constraint(0, PredicateType.Le));
        state2.add(xs[3], xs[1], new Constraint(4, PredicateType.Le));

        {
            DifferenceBoundedState m = new DifferenceBoundedState(state1);
            DifferenceBoundedState n = new DifferenceBoundedState(state2);

            m.widenWith(n);

            assertAll("m ▽ n",
                      () -> assertEquals(new Constraint(2, PredicateType.Le),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(new Constraint(3, PredicateType.Le),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.TOP(), m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(new Constraint(4, PredicateType.Le),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.TOP(), m.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.TOP(), m.getConstraint(xs[3], xs[1])));
        }

        {
            DifferenceBoundedState m = new DifferenceBoundedState(state1);
            DifferenceBoundedState n = new DifferenceBoundedState(state2);

            n.widenWith(m);

            assertAll("n ▽ m",
                      () -> assertEquals(Constraint.TOP(), n.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(new Constraint(3, PredicateType.Le),
                                         n.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(new Constraint(4, PredicateType.Le),
                                         n.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(), n.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(new Constraint(0, PredicateType.Le),
                                         n.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.TOP(), n.getConstraint(xs[3], xs[1])));
        }
    }

    @Test
    void testWideningSelf() {
        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState m;
        DifferenceBoundedState n;

        state1.add(xs[0], xs[1], new Constraint(3));
        state1.add(xs[1], xs[2], new Constraint(2));
        state1.add(xs[3], xs[0], new Constraint(5));

        m = new DifferenceBoundedState(state1);
        n = new DifferenceBoundedState(state1);

        m.widenWith(n);

        assertAll("m ▽ m",
                  () -> assertEquals(new Constraint(3), m.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(new Constraint(2), m.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(new Constraint(5), m.getConstraint(xs[3], xs[0])));
    }

    @Test
    void testIncrementalClosureAfterMerge() {
        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        state1.add(xs[0], xs[1], new Constraint(3));
        state1.add(xs[1], xs[2], new Constraint(2));
        state1.add(xs[2], new Constraint(5));
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);
        state2.add(xs[0], xs[1], new Constraint(4));
        state2.add(xs[1], xs[2], new Constraint(1));
        state2.add(xs[2], new Constraint(6));
        assertAll("projection after merge feasible",
                  () -> assertTrue(state1.isFeasible()),
                  () -> assertTrue(state2.isFeasible()));
        state1.mergeWith(state2);
        assertAll("projection after merge edges equal",
                  () -> assertEquals(new Constraint(4), state1.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(new Constraint(2), state1.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(new Constraint(6), state1.getConstraint(xs[2])));
        assertAll("merging should also project intervals",
                  () -> assertEquals(new Constraint(7), state1.getConstraint(xs[1])),
                  () -> assertEquals(new Constraint(11), state1.getConstraint(xs[0])));
        // this should be a no-op
        state1.incrementalClosure(xs[1], state1);
        state1.incrementalClosure(xs[0], state1);
        assertAll("projection no-op",
                  () -> assertEquals(new Constraint(7), state1.getConstraint(xs[1])),
                  () -> assertEquals(new Constraint(11), state1.getConstraint(xs[0])));
    }

    @Test
    void testIncrementalClosureUpdatesInferredEdges() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3));
        inState.add(xs[1], xs[2], new Constraint(2));
        inState.add(xs[0], xs[2], new Constraint(5));

        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[0], inState, xs[1], IntConstant.v(2), BinaryOperator.ADDITION);
        assertAll(() -> assertEquals(new Constraint(2), state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(new Constraint(4), state.getConstraint(xs[0], xs[2])));
    }

    @Test
    void testIsFeasibleWhenBottom() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(0));
        state.add(xs[1], xs[2], new Constraint(0));
        state.add(xs[2], Constraint.BOT());
        assertFalse(state.isFeasible());
    }

    @Test
    void testToSMT() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(2));
        state.add(xs[2], new Constraint(4, PredicateType.Le));
        String[] result = state.toSMT(solver).split("\n");
        Arrays.sort(result);
        String[] expected = new String[] {
            "x0->(<= x0 (+ 3 x1))",
            "x1->(= x1 (+ 2 x2))",
            "x2->(<= x2 4)",
        };
        assertEquals(expected.length, result.length);
        assertAll(IntStream.range(0, expected.length)
                  .mapToObj(i -> () -> assertEquals(expected[i], result[i])));
    }

    @Test
    void testToSMTIndividual() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], new Constraint(3));
        state.add(xs[1], new Constraint(4));
        state.add(xs[2], xs[3], new Constraint(2));

        assertAll("toSMT doesn't repeat equality constraints",
                  () -> assertEquals("(= x0 3)", state.toSMT(xs[0], solver)),
                  () -> assertEquals("(= x1 4)", state.toSMT(xs[1], solver)),
                  () -> assertEquals("(= x2 (+ 2 x3))", state.toSMT(xs[2], solver)),
                  () -> assertEquals("(= x2 (+ 2 x3))", state.toSMT(xs[3], solver)));
    }

    @Test
    void testForgetTransfer() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(2));
        state.add(xs[1], xs[2], new Constraint(3));
        state.add(xs[3], xs[0], new Constraint(1));
        state.forget(xs[0]);
        assertAll("forget transfer",
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(), state.getConstraint(xs[3], xs[0])),
                  () -> assertEquals(new Constraint(3), state.getConstraint(xs[3], xs[1])),
                  () -> assertEquals(new Constraint(6), state.getConstraint(xs[3], xs[2])));
    }

    @Test
    void testAddWithNarrowing() {
        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.addWithNarrowing(xs[0], xs[1], new Constraint(2));
            assertEquals(new Constraint(2), state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.add(xs[0], xs[1], new Constraint(2));
            state.addWithNarrowing(xs[0], xs[1], new Constraint(2, PredicateType.Le));
            assertEquals(new Constraint(2), state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.add(xs[0], xs[1], new Constraint(2, PredicateType.Le));
            state.addWithNarrowing(xs[0], xs[1], new Constraint(2));
            assertEquals(new Constraint(2), state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.add(xs[0], xs[1], new Constraint(3));
            state.addWithNarrowing(xs[0], xs[1], new Constraint(2));
            assertEquals(Constraint.BOT(), state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.add(xs[0], xs[1], new Constraint(2));
            state.addWithNarrowing(xs[0], xs[1], new Constraint(1, PredicateType.Le));
            assertEquals(Constraint.BOT(), state.getConstraint(xs[0], xs[1]));
        }

        {
            DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
            state.add(xs[0], xs[1], new Constraint(1, PredicateType.Le));
            state.addWithNarrowing(xs[0], xs[1], new Constraint(2));
            assertEquals(Constraint.BOT(), state.getConstraint(xs[0], xs[1]));
        }
    }
}
