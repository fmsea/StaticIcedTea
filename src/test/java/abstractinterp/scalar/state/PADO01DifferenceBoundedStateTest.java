package abstractinterp.scalar.state;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

import solver.SolverWrapper;
import solver.SolverWrapperZ3;

public class PADO01DifferenceBoundedStateTest {

    private Set<Local> locals;
    private Local[] xs;
    private SolverWrapper solver;
    private PADO01DifferenceBoundedMatrix matrix;

    @BeforeEach
    void setupSolver() {
        this.solver = new SolverWrapperZ3();
    }

    @BeforeEach
    void setupLocals() {
        this.xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
            Jimple.v().newLocal("x4", IntType.v()),
        };
        this.locals = new HashSet<>(10);
        for (Local x : this.xs) { this.locals.add(x); }
        this.matrix = new PADO01DifferenceBoundedMatrix(this.locals, true);
    }

    @Test
    @DisplayName("test add when matrix is top")
    void testAdd01() {
        PADO01DifferenceBoundedState state = new PADO01DifferenceBoundedState(matrix);
        state.add(xs[1], xs[2], PADO01Constraint.of(1));
        assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test add when matrix contains existing constraint")
    void testAdd02() {
        this.matrix.putConstraint(xs[1], xs[2], PADO01Constraint.of(2));
        PADO01DifferenceBoundedState state = new PADO01DifferenceBoundedState(matrix);
        state.add(xs[1], xs[2], PADO01Constraint.of(3));
        assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≤ constant)")
    void testUpdateCondLe01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ constant)")
    void testUpdateCondLe02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ constant)")
    void testUpdateCondLe03() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≤ local)")
    void testUpdateCondLe11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≤ local)")
    void testUpdateCondLe12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≤ local)")
    void testUpdateCondLe13() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≤ local)")
    void testUpdateCondLe31() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ local)")
    void testUpdateCondLe32() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(-1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ local)")
    void testUpdateCondLe33() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local < constant)")
    void testUpdateCondLt01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < constant)")
    void testUpdateCondLt02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < constant)")
    void testUpdateCondLt03() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant < local)")
    void testUpdateCondLt11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant < local)")
    void testUpdateCondLt12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(-1), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant < local)")
    void testUpdateCondLt13() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local < local)")
    void testUpdateCondLt31() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < local)")
    void testUpdateCondLt32() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < local)")
    void testUpdateCondLt33() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local = constant)")
    void testUpdateCondEq01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = constant)")
    void testUpdateCondEq02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = constant)")
    void testUpdateCondEq03() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(+1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant = local)")
    void testUpdateCondEq11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant = local)")
    void testUpdateCondEq12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(3), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+3), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant = local)")
    void testUpdateCondEq13() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local = local)")
    void testUpdateCondEq31() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = local)")
    void testUpdateCondEq32() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[1], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = local)")
    void testUpdateCondEq33() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(3));
        in.add(xs[2], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≥ constant)")
    void testUpdateCondGe01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ constant)")
    void testUpdateCondGe02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ constant)")
    void testUpdateCondGe03() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≥ local)")
    void testUpdateCondGe11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≥ local)")
    void testUpdateCondGe12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≥ local)")
    void testUpdateCondGe13() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≥ local)")
    void testUpdateCondGe31() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ local)")
    void testUpdateCondGe32() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(-1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ local)")
    void testUpdateCondGe33() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local > constant)")
    void testUpdateCondGt01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > constant)")
    void testUpdateCondGt02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > constant)")
    void testUpdateCondGt03() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(-1), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant > local)")
    void testUpdateCondGt11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant > local)")
    void testUpdateCondGt12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant > local)")
    void testUpdateCondGt13() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(4));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local > local)")
    void testUpdateCondGt31() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > local)")
    void testUpdateCondGt32() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > local)")
    void testUpdateCondGt33() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(0));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l = c)")
    void testUpdateState01() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l = c)")
    void testUpdateState02() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l1 = l2)")
    void testUpdateState11() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2)")
    void testUpdateState12() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState001() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState002() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState003() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState004() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-5),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState011() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState012() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState013() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState014() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState021() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState022() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState023() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState024() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState031() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState032() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState033() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState034() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState101() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState102() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState103() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState104() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-5),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState111() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState112() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState113() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState114() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState121() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState122() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState123() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState124() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState131() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState132() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.of(1),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState133() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState134() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState201() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState202() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[3], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[3], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState203() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState204() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState211() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState212() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[3], xs[0], PADO01Constraint.of(+4));
        in.add(xs[0], xs[3], PADO01Constraint.of(-4));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(-2),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(+2),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState213() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState214() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+4));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+2),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+2),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState221() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState222() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[3], PADO01Constraint.of(-3));
        in.add(xs[3], xs[0], PADO01Constraint.of(+3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState223() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState224() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-4));
        in.add(xs[2], xs[0], PADO01Constraint.of(+4));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+8),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-8),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-4),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+4),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-12),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+12),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState231() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState232() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[3], xs[0], PADO01Constraint.of(-3));
        in.add(xs[0], xs[3], PADO01Constraint.of(+3));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState233() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState234() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+2),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-2),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState300() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[0], IntConstant.v(3), BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState301() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState303() {
        PADO01DifferenceBoundedState in = new PADO01DifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        PADO01DifferenceBoundedState out = new PADO01DifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[3], xs[2], BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])));
    }
}
