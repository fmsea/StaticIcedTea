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

public class IncDifferenceBoundedStateTest {

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
        IncDifferenceBoundedState state = new IncDifferenceBoundedState(matrix);
        state.add(xs[1], xs[2], PADO01Constraint.of(1));
        assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test add when matrix contains existing constraint")
    void testAdd02() {
        this.matrix.putConstraint(xs[1], xs[2], PADO01Constraint.of(2));
        IncDifferenceBoundedState state = new IncDifferenceBoundedState(matrix);
        state.add(xs[1], xs[2], PADO01Constraint.of(3));
        assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≤ constant)")
    void testUpdateCondLe01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ constant)")
    void testUpdateCondLe02() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ constant)")
    void testUpdateCondLe03() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≤ local)")
    void testUpdateCondLe11() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≤ local)")
    void testUpdateCondLe12() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≤ local)")
    void testUpdateCondLe13() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≤ local)")
    void testUpdateCondLe31() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ local)")
    void testUpdateCondLe32() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(-1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ local)")
    void testUpdateCondLe33() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local < constant)")
    void testUpdateCondLt01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < constant)")
    void testUpdateCondLt02() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < constant)")
    void testUpdateCondLt03() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant < local)")
    void testUpdateCondLt11() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant < local)")
    void testUpdateCondLt12() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(-1), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant < local)")
    void testUpdateCondLt13() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local < local)")
    void testUpdateCondLt31() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < local)")
    void testUpdateCondLt32() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < local)")
    void testUpdateCondLt33() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local = constant)")
    void testUpdateCondEq01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = constant)")
    void testUpdateCondEq02() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = constant)")
    void testUpdateCondEq03() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(+1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant = local)")
    void testUpdateCondEq11() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant = local)")
    void testUpdateCondEq12() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(3), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+3), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant = local)")
    void testUpdateCondEq13() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local = local)")
    void testUpdateCondEq31() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = local)")
    void testUpdateCondEq32() {
        PADO01DifferenceBoundedMatrix m1 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        m1.setConstraint(xs[1], xs[2], PADO01Constraint.of(-1));
        m1.setConstraint(xs[2], xs[1], PADO01Constraint.of(+1));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(m1);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = local)")
    void testUpdateCondEq33() {
        PADO01DifferenceBoundedMatrix m1 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        m1.setConstraint(xs[1], xs[2], PADO01Constraint.of(3));
        m1.setConstraint(xs[2], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(m1);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≥ constant)")
    void testUpdateCondGe01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ constant)")
    void testUpdateCondGe02() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ constant)")
    void testUpdateCondGe03() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≥ local)")
    void testUpdateCondGe11() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≥ local)")
    void testUpdateCondGe12() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≥ local)")
    void testUpdateCondGe13() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≥ local)")
    void testUpdateCondGe31() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ local)")
    void testUpdateCondGe32() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(-1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ local)")
    void testUpdateCondGe33() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local > constant)")
    void testUpdateCondGt01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > constant)")
    void testUpdateCondGt02() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > constant)")
    void testUpdateCondGt03() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[1], PADO01Constraint.of(2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(-1), PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant > local)")
    void testUpdateCondGt11() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant > local)")
    void testUpdateCondGt12() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant > local)")
    void testUpdateCondGt13() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(4));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local > local)")
    void testUpdateCondGt31() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > local)")
    void testUpdateCondGt32() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-2), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > local)")
    void testUpdateCondGt33() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[1], PADO01Constraint.of(0));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(PADO01Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l = c)")
    void testUpdateState01() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[0], PADO01Constraint.of(2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState004() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState014() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState022() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState024() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState032() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState034() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState104() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState114() {
        this.matrix.setConstraint(xs[1], xs[2], PADO01Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], PADO01Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.matrix);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertTrue(out.isFeasible()),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     m2.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState121() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState122() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState124() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState132() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState134() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[3], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[3], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState204() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[3], xs[0], PADO01Constraint.of(+4));
        in.add(xs[0], xs[3], PADO01Constraint.of(-4));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState214() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+4));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState222() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[3], PADO01Constraint.of(-3));
        in.add(xs[3], xs[0], PADO01Constraint.of(+3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState224() {
        this.matrix.setConstraint(xs[1], xs[2], PADO01Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], PADO01Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], PADO01Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], PADO01Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], PADO01Constraint.of(-4));
        this.matrix.setConstraint(xs[2], xs[0], PADO01Constraint.of(+4));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(matrix);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(PADO01Constraint.of(+8),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(-8),
                                     m2.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-4),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+4),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.of(-12),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(+12),
                                     m2.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState231() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState232() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[2], xs[0], PADO01Constraint.of(+2));
        in.add(xs[0], xs[2], PADO01Constraint.of(-2));
        in.add(xs[3], xs[0], PADO01Constraint.of(-3));
        in.add(xs[0], xs[3], PADO01Constraint.of(+3));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new PADO01DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState234() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
        this.matrix.setConstraint(xs[1], xs[2], PADO01Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], PADO01Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], PADO01Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], PADO01Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], PADO01Constraint.of(-1));
        this.matrix.setConstraint(xs[2], xs[0], PADO01Constraint.of(+1));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(matrix);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[0], IntConstant.v(3), BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState301() {
        this.matrix.setConstraint(xs[1], xs[2], PADO01Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], PADO01Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], PADO01Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], PADO01Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], PADO01Constraint.of(-1));
        this.matrix.setConstraint(xs[2], xs[0], PADO01Constraint.of(+1));
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(matrix);
        PADO01DifferenceBoundedMatrix m2 = new PADO01DifferenceBoundedMatrix(this.locals, true);
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(PADO01Constraint.of(-1),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(PADO01Constraint.of(+1),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState303() {
        IncDifferenceBoundedState in = new IncDifferenceBoundedState(this.locals, true);
        in.add(xs[1], xs[2], PADO01Constraint.of(+2));
        in.add(xs[2], xs[1], PADO01Constraint.of(-2));
        in.add(xs[0], xs[1], PADO01Constraint.of(-3));
        in.add(xs[1], xs[0], PADO01Constraint.of(+3));
        in.add(xs[0], xs[2], PADO01Constraint.of(-1));
        in.add(xs[2], xs[0], PADO01Constraint.of(+1));
        IncDifferenceBoundedState out = new IncDifferenceBoundedState(matrix);
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
