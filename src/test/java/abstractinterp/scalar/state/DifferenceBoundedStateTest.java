package abstractinterp.scalar.state;

import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import soot.Local;
import soot.IntType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class DifferenceBoundedStateTest {

    private Set<Local> locals;
    private Local[] xs;
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
        DifferenceBoundedState target = new DifferenceBoundedState(source);
        for (int i = 0; i < N2; i++) {
            Assertions.assertEquals(c, target.getValue(xs[i], xs[i + 1]));
        }
        for (Local l : locals) {
            Assertions.assertEquals(Constraint.TOP(), target.getValue(l));
        }

        target = new DifferenceBoundedState(locals, false);
        source.copyTo(target);
        for (int i = 0; i < N2; i++) {
            Assertions.assertEquals(c, target.getValue(xs[i], xs[i + 1]));
        }
        for (Local l : locals) {
            Assertions.assertEquals(Constraint.TOP(), target.getValue(l));
        }
    }

    @Test
    void testIsFeasible() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        Assertions.assertTrue(state.isFeasible());
        state.add(xs[0], new Constraint(0));
        Assertions.assertTrue(state.isFeasible());
        state.add(state.ZERO, xs[0], new Constraint(-1));
        Assertions.assertFalse(state.isFeasible());
        state.forgetConstraints(xs[0]);
        state.add(xs[0], xs[1], new Constraint(0));
        Assertions.assertTrue(state.isFeasible());
        state.add(xs[1], xs[0], new Constraint(0));
        Assertions.assertTrue(state.isFeasible());
        state.add(xs[1], xs[0], new Constraint(-1));
        Assertions.assertFalse(state.isFeasible());
        state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], new Constraint(6, PredicateType.Le));
        state.add(state.ZERO, xs[1], new Constraint(-11, PredicateType.Le));
        state.add(xs[1], xs[0], new Constraint(4, PredicateType.Le));
        Assertions.assertFalse(state.isFeasible());
        state = new DifferenceBoundedState(locals, true);
        state.add(state.ZERO, xs[0], new Constraint(-4, PredicateType.Le));
        state.add(xs[1], new Constraint(2, PredicateType.Le));
        state.add(xs[0], xs[1], new Constraint(-4, PredicateType.Le));
        Assertions.assertFalse(state.isFeasible());
        state = new DifferenceBoundedState(locals, true);
        state.add(state.ZERO, xs[0], new Constraint(1, PredicateType.Le));
        state.add(xs[0], xs[1], new Constraint(1, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(2, PredicateType.Le));
        state.add(xs[2], new Constraint(-5, PredicateType.Le));
        Assertions.assertFalse(state.isFeasible());
        state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(1, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(2, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(1, PredicateType.Le));
        state.add(xs[3], xs[1], new Constraint(-4, PredicateType.Le));
        Assertions.assertFalse(state.isFeasible());
    }

    @Test
    void testConstantTransfer() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, IntConstant.v(1));
        Assertions.assertEquals(new Constraint(1, PredicateType.Eq),
                                state.getValue(xs[0]));
        state.updateState(xs[0], inState, IntConstant.v(-1));
        Assertions.assertEquals(new Constraint(-1, PredicateType.Eq),
                                state.getValue(xs[0]));
    }

    @Test
    void testAliasAssignment() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, xs[1]);
        Assertions.assertEquals(new Constraint(0, PredicateType.Eq),
                                state.getValue(xs[0], xs[1]));
    }

    @Test
    void testUpdateVariableAddition() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[0], inState, xs[0], IntConstant.v(3), BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(6, PredicateType.Le),
                                state.getValue(xs[0], xs[1]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[0], inState, IntConstant.v(3), xs[0], BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(6, PredicateType.Le),
                                state.getValue(xs[0], xs[1]));
    }

    @Test
    void testUpdateVariableSubtraction() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[0], inState, xs[0], IntConstant.v(3), BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(new Constraint(0, PredicateType.Le),
                                state.getValue(xs[0], xs[1]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[0], inState, IntConstant.v(3), xs[0], BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
    }

    @Test
    void testUpdateVariableMultiplication() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[0], inState, IntConstant.v(2), xs[0], BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));
    }

    @Test
    void testUpdateVariableDivision() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[0], inState, IntConstant.v(2), xs[0], BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));
    }

    @Test
    void testSingleVariableAddition() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[1], inState, xs[0], IntConstant.v(1), BinaryOperator.ADDITION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(1, PredicateType.Eq),
                                state.getValue(xs[1], xs[0]));
    }

    @Test
    void testSingleVariableSubtraction() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[1], inState, xs[0], IntConstant.v(1), BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(-1, PredicateType.Eq),
                                state.getValue(xs[1], xs[0]));

        state = new DifferenceBoundedState(inState);
        state.updateState(xs[2], inState, IntConstant.v(2), xs[0], BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[2]));
        Assertions.assertEquals(new Constraint(-5, PredicateType.Le),
                                state.getValue(xs[2]));
    }

    @Test
    void testSingleVariableMultiplication() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[1], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1]));
    }

    @Test
    void testSingleVariableMultiplicationIntervalProjection() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[2], inState, xs[0], IntConstant.v(2), BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[2]));
        Assertions.assertEquals(new Constraint(14, PredicateType.Le), state.getValue(xs[2]));

        state = new DifferenceBoundedState(inState);
        state.updateState(xs[2], inState, IntConstant.v(2), xs[0], BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[2]));
        Assertions.assertEquals(new Constraint(14, PredicateType.Le), state.getValue(xs[2]));
    }

    @Test
    void testSingleVariableDivision() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[1], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1]));
    }

    @Test
    void testSingleVariableDivisionIntervalProjection() {
        DifferenceBoundedState inState = new DifferenceBoundedState(locals, true);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4));
        DifferenceBoundedState state = new DifferenceBoundedState(inState);
        state.updateState(xs[2], inState, xs[0], IntConstant.v(2), BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[2]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[0]));
        Assertions.assertEquals(new Constraint(3, PredicateType.Le), state.getValue(xs[2]));

        state = new DifferenceBoundedState(inState);
        state.updateState(xs[2], inState, IntConstant.v(14), xs[0], BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0], xs[2]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[0]));
        Assertions.assertEquals(new Constraint(2, PredicateType.Le), state.getValue(xs[2]));
    }

    @Test
    void testTransferInfersInterval() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], new Constraint(5, PredicateType.Eq));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateState(xs[0], inState, xs[0], IntConstant.v(2), BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(5, PredicateType.Le),
                                state.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(5, PredicateType.Eq),
                                state.getValue(xs[1]));
        Assertions.assertEquals(new Constraint(10, PredicateType.Le),
                                state.getValue(xs[0]));
    }

    @Test
    void testIntervalProjection() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(1, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(2, PredicateType.Le));
        state.add(xs[3], new Constraint(4, PredicateType.Eq));
        for (Local x : xs) {
            state.projectInterval(x, state);
        }
        Assertions.assertEquals(new Constraint(6, PredicateType.Le),
                                state.getValue(xs[2]));
        Assertions.assertEquals(new Constraint(7, PredicateType.Le),
                                state.getValue(xs[1]));
        Assertions.assertEquals(new Constraint(10, PredicateType.Le),
                                state.getValue(xs[0]));
    }

    @Test
    void testIntervalProjectionOverflow() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(0, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(Integer.MAX_VALUE, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(0, PredicateType.Le));
        state.add(xs[3], new Constraint(1, PredicateType.Eq));
        for (Local x : xs) {
            state.projectInterval(x, state);
        }
        Assertions.assertEquals(new Constraint(1, PredicateType.Le),
                                state.getValue(xs[2]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1]));
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));
    }

    @Test
    void testIntervalProjectionWhenNoEdgesToZero() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(1, PredicateType.Le));
        state.add(xs[2], xs[3], new Constraint(2, PredicateType.Le));
        for (Local x : xs) {
            state.projectInterval(x, state);
        }
        for (Local x : xs) {
            Assertions.assertEquals(Constraint.TOP(), state.getValue(x));
        }
    }

    @Test
    void testTransferTwoVariablesAddition() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(3, PredicateType.Le),
                                state.getValue(xs[2]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(3, PredicateType.Le),
                                state.getValue(xs[2]));
    }

    @Test
    void testTransferTwoVariablesSubtraction() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[2], inState, xs[1], xs[0], BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(new Constraint(-3, PredicateType.Le),
                                state.getValue(xs[2]));

        state = new DifferenceBoundedState(locals, true);
        state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(new Constraint(3, PredicateType.Le),
                                state.getValue(xs[2], state.ZERO));
    }

    @Test
    void testTransferTwoVariablesMultiplication() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2]));
    }

    @Test
    void testTransferTwoVariablesDivision() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.updateState(xs[2], inState, xs[0], xs[1], BinaryOperator.DIVISION);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2]));
    }

    @Test
    void testTransferTwoVariableAdditionWithProjection() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));
        inState.add(xs[2], new Constraint(3, PredicateType.Eq));
        state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.ADDITION);
        Assertions.assertEquals(new Constraint(7, PredicateType.Eq),
                                state.getValue(xs[3], state.ZERO));
    }

    @Test
    void testTransferTwoVariableSubtractionWithProjection() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));
        inState.add(xs[2], new Constraint(3, PredicateType.Eq));
        state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.SUBTRACTION);
        Assertions.assertEquals(new Constraint(1, PredicateType.Eq),
                                state.getValue(xs[3], state.ZERO));
    }

    @Test
    void testTransferTwoVariableMultiplicationWithProjection() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));
        inState.add(xs[2], new Constraint(3, PredicateType.Eq));
        state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.MULTIPLICATION);
        Assertions.assertEquals(new Constraint(12, PredicateType.Eq),
                                state.getValue(xs[3]));
    }

    @Test
    void testTransferTwoVariableDivisionWithProjection() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        inState.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        inState.add(xs[1], new Constraint(4, PredicateType.Eq));
        inState.add(xs[2], new Constraint(3, PredicateType.Eq));
        state.updateState(xs[3], inState, xs[1], xs[2], BinaryOperator.DIVISION);
        Assertions.assertEquals(new Constraint(1, PredicateType.Eq),
                                state.getValue(xs[3]));
    }

    @Test
    void testTransferConditionLessEqual() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Le);
        Assertions.assertEquals(new Constraint(2, PredicateType.Le),
                                state.getValue(xs[0]));
        state.updateCond(inState, IntConstant.v(2), xs[1], PredicateType.Le);
        Assertions.assertEquals(new Constraint(-2, PredicateType.Le),
                                state.getValue(state.ZERO, xs[1]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Le);
        Assertions.assertEquals(new Constraint(0, PredicateType.Le),
                                state.getValue(xs[2], xs[3]));
    }

    @Test
    void testTransferConditionLessThan() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Lt);
        Assertions.assertEquals(new Constraint(1, PredicateType.Le),
                                state.getValue(xs[0]));
        state.updateCond(inState, IntConstant.v(2), xs[1], PredicateType.Lt);
        Assertions.assertEquals(new Constraint(-3, PredicateType.Le),
                                state.getValue(state.ZERO, xs[1]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Lt);
        Assertions.assertEquals(new Constraint(-1, PredicateType.Le),
                                state.getValue(xs[2], xs[3]));
    }

    @Test
    void testTransferConditionEqual() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Eq);
        Assertions.assertEquals(new Constraint(2), state.getValue(xs[0]));
        state.updateCond(inState, IntConstant.v(0), xs[2], PredicateType.Eq);
        Assertions.assertEquals(new Constraint(0), state.getValue(xs[2]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Eq);
        Assertions.assertEquals(new Constraint(0), state.getValue(xs[2], xs[3]));
    }

    @Test
    void testTransferConditionNotEqual() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Ne);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[0]));
        state.updateCond(inState, IntConstant.v(3), xs[1], PredicateType.Ne);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[1]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Ne);
        Assertions.assertEquals(Constraint.TOP(), state.getValue(xs[2], xs[3]));
    }

    @Test
    void testTransferConditionGreaterEqual() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Ge);
        Assertions.assertEquals(new Constraint(-2, PredicateType.Le),
                                state.getValue(state.ZERO, xs[0]));
        state.updateCond(inState, IntConstant.v(2), xs[1], PredicateType.Ge);
        Assertions.assertEquals(new Constraint(2, PredicateType.Le),
                                state.getValue(xs[1]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Ge);
        Assertions.assertEquals(new Constraint(0, PredicateType.Le),
                                state.getValue(xs[3], xs[2]));
    }

    @Test
    void testTransferConditionGreaterThan() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        DifferenceBoundedState inState = new DifferenceBoundedState(state);
        state.updateCond(inState, xs[0], IntConstant.v(2), PredicateType.Gt);
        Assertions.assertEquals(new Constraint(-3, PredicateType.Le),
                                state.getValue(state.ZERO, xs[0]));
        state.updateCond(inState, IntConstant.v(2), xs[1], PredicateType.Gt);
        Assertions.assertEquals(new Constraint(3, PredicateType.Le),
                                state.getValue(xs[1]));
        state.updateCond(inState, xs[2], xs[3], PredicateType.Gt);
        Assertions.assertEquals(new Constraint(-1, PredicateType.Le),
                                state.getValue(xs[3], xs[2]));
    }

    @Test
    void testPathMerging() {

        Consumer<DifferenceBoundedState> check = (state -> {
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[0], xs[3]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[1], xs[3]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[2], xs[0]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[2], xs[1]));
                Assertions.assertEquals(new Constraint(6), state.getValue(xs[2], xs[3]));
            });

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

        check.accept(m);

        m = new DifferenceBoundedState(state1);
        n = new DifferenceBoundedState(state2);

        n.mergeWith(m);

        check.accept(n);
    }

    @Test
    void testMergingWithUnevenEdges() {
        Consumer<DifferenceBoundedState> check = (state -> {
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[0], xs[3]));
                Assertions.assertEquals(new Constraint(5), state.getValue(xs[0], xs[1]));
                Assertions.assertEquals(new Constraint(3), state.getValue(xs[0], xs[2]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[1], xs[3]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[2], xs[0]));
                Assertions.assertEquals(new Constraint(4), state.getValue(xs[2], xs[1]));
                Assertions.assertEquals(new Constraint(6), state.getValue(xs[2], xs[3]));
            });

        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);
        state1.add(xs[0], xs[3], new Constraint(4));
        state1.add(xs[1], xs[3], new Constraint(2));
        state1.add(xs[2], xs[0], new Constraint(2));
        state1.add(xs[2], xs[1], new Constraint(4));
        state1.add(xs[2], xs[3], new Constraint(6));

        state2.add(xs[0], xs[3], new Constraint(2));
        state2.add(xs[0], xs[1], new Constraint(6));
        state2.add(xs[0], xs[2], new Constraint(3));
        state2.add(xs[1], xs[3], new Constraint(4));
        state2.add(xs[2], xs[0], new Constraint(4));
        state2.add(xs[2], xs[1], new Constraint(2));

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

        state1.add(xs[0], xs[1], new Constraint(2));
        state1.add(xs[1], xs[2], new Constraint(3));
        state1.add(xs[2], xs[0], new Constraint(5));
        state1.add(xs[2], xs[3], new Constraint(4));
        state1.add(xs[3], xs[0], new Constraint(1));
        state1.add(xs[3], xs[1], new Constraint(5));

        state2.add(xs[0], xs[1], new Constraint(3));
        state2.add(xs[1], xs[2], new Constraint(3));
        state2.add(xs[2], xs[0], new Constraint(4));
        state2.add(xs[2], xs[3], new Constraint(5));
        state2.add(xs[3], xs[0], new Constraint(0));
        state2.add(xs[3], xs[1], new Constraint(4));

        DifferenceBoundedState m = new DifferenceBoundedState(state1);
        DifferenceBoundedState n = new DifferenceBoundedState(state2);

        m.widenWith(n);

        Assertions.assertEquals(Constraint.TOP(), m.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(3), m.getValue(xs[1], xs[2]));
        Assertions.assertEquals(new Constraint(5), m.getValue(xs[2], xs[0]));
        Assertions.assertEquals(Constraint.TOP(), m.getValue(xs[2], xs[3]));
        Assertions.assertEquals(new Constraint(1), m.getValue(xs[3], xs[0]));
        Assertions.assertEquals(new Constraint(5), m.getValue(xs[3], xs[1]));

        m = new DifferenceBoundedState(state1);
        n = new DifferenceBoundedState(state2);

        n.widenWith(m);

        Assertions.assertEquals(new Constraint(3), n.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(3), n.getValue(xs[1], xs[2]));
        Assertions.assertEquals(Constraint.TOP(), n.getValue(xs[2], xs[0]));
        Assertions.assertEquals(new Constraint(5), n.getValue(xs[2], xs[3]));
        Assertions.assertEquals(Constraint.TOP(), n.getValue(xs[3], xs[0]));
        Assertions.assertEquals(new Constraint(4), n.getValue(xs[3], xs[1]));
    }

    @Test
    void testProjectionAfterMerge() {
        DifferenceBoundedState state1 = new DifferenceBoundedState(locals, true);
        state1.add(xs[0], xs[1], new Constraint(3));
        state1.add(xs[1], xs[2], new Constraint(2));
        state1.add(xs[2], new Constraint(5));
        DifferenceBoundedState state2 = new DifferenceBoundedState(locals, true);
        state2.add(xs[0], xs[1], new Constraint(4));
        state2.add(xs[1], xs[2], new Constraint(1));
        state2.add(xs[2], new Constraint(6));
        Assertions.assertTrue(state1.isFeasible());
        Assertions.assertTrue(state2.isFeasible());
        state1.mergeWith(state2);
        Assertions.assertEquals(new Constraint(4), state1.getValue(xs[0], xs[1]));
        Assertions.assertEquals(new Constraint(2), state1.getValue(xs[1], xs[2]));
        Assertions.assertEquals(new Constraint(6), state1.getValue(xs[2]));
        // "merging" should also "project intervals"
        Assertions.assertEquals(new Constraint(7), state1.getValue(xs[1]));
        Assertions.assertEquals(new Constraint(11), state1.getValue(xs[0]));
        // this should be a no-op
        state1.projectIntervals(state1);
        Assertions.assertEquals(new Constraint(7), state1.getValue(xs[1]));
        Assertions.assertEquals(new Constraint(11), state1.getValue(xs[0]));
    }

    @Test
    void testIsFeasibleWhenBottom() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(0));
        state.add(xs[1], xs[2], new Constraint(0));
        state.add(xs[2], Constraint.BOT());
        Assertions.assertFalse(state.isFeasible());
    }

    @Test
    void testToSMTFormula() {
        DifferenceBoundedState state = new DifferenceBoundedState(locals, true);
        state.add(xs[0], xs[1], new Constraint(3, PredicateType.Le));
        state.add(xs[1], xs[2], new Constraint(2));
        state.add(xs[2], new Constraint(4, PredicateType.Le));
        Assertions.assertEquals("x0->(<= x0 (+ 3 x1))\n" +
                                "x1->(= x1 (+ 2 x2))\n" +
                                "x2->(<= x2 4)\n",
                                state.toSMTFormula(solver));
    }
}
