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
import solver.SolverFactory;

public class IncZoneStateTest {

    private Set<Local> locals;
    private Local[] xs;
    private SolverWrapper solver;
    private DifferenceBoundedMatrix matrix;

    @BeforeEach
    void setupSolver() {
        this.solver = SolverFactory.getSolver();
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
        this.matrix = new DifferenceBoundedMatrix(this.locals, true);
    }

    @Test
    @DisplayName("test add when matrix is top")
    void testAdd01() {
        IncZoneState state = new IncZoneState(matrix);
        state.add(xs[1], xs[2], Constraint.of(1));
        assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test add when matrix contains existing constraint")
    void testAdd02() {
        this.matrix.putConstraint(xs[1], xs[2], Constraint.of(2));
        IncZoneState state = new IncZoneState(matrix);
        state.add(xs[1], xs[2], Constraint.of(3));
        assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[2]));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≤ constant)")
    void testUpdateCondLe01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ constant)")
    void testUpdateCondLe02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ constant)")
    void testUpdateCondLe03() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Le)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≤ local)")
    void testUpdateCondLe11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≤ local)")
    void testUpdateCondLe12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≤ local)")
    void testUpdateCondLe13() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≤ local)")
    void testUpdateCondLe31() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ local)")
    void testUpdateCondLe32() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ local)")
    void testUpdateCondLe33() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Le)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local < constant)")
    void testUpdateCondLt01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < constant)")
    void testUpdateCondLt02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < constant)")
    void testUpdateCondLt03() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant < local)")
    void testUpdateCondLt11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant < local)")
    void testUpdateCondLt12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(-1), xs[1], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant < local)")
    void testUpdateCondLt13() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local < local)")
    void testUpdateCondLt31() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < local)")
    void testUpdateCondLt32() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < local)")
    void testUpdateCondLt33() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Lt)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local = constant)")
    void testUpdateCondEq01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = constant)")
    void testUpdateCondEq02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = constant)")
    void testUpdateCondEq03() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(+1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant = local)")
    void testUpdateCondEq11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant = local)")
    void testUpdateCondEq12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(+3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(3), xs[1], PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+3), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant = local)")
    void testUpdateCondEq13() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local = local)")
    void testUpdateCondEq31() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = local)")
    void testUpdateCondEq32() {
        DifferenceBoundedMatrix m1 = new DifferenceBoundedMatrix(this.locals, true);
        m1.setConstraint(xs[1], xs[2], Constraint.of(-1));
        m1.setConstraint(xs[2], xs[1], Constraint.of(+1));
        IncZoneState in = new IncZoneState(m1);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = local)")
    void testUpdateCondEq33() {
        DifferenceBoundedMatrix m1 = new DifferenceBoundedMatrix(this.locals, true);
        m1.setConstraint(xs[1], xs[2], Constraint.of(3));
        m1.setConstraint(xs[2], xs[1], Constraint.of(-3));
        IncZoneState in = new IncZoneState(m1);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≥ constant)")
    void testUpdateCondGe01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ constant)")
    void testUpdateCondGe02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ constant)")
    void testUpdateCondGe03() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≥ local)")
    void testUpdateCondGe11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≥ local)")
    void testUpdateCondGe12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≥ local)")
    void testUpdateCondGe13() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≥ local)")
    void testUpdateCondGe31() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ local)")
    void testUpdateCondGe32() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(-1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ local)")
    void testUpdateCondGe33() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Ge)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local > constant)")
    void testUpdateCondGt01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > constant)")
    void testUpdateCondGt02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > constant)")
    void testUpdateCondGt03() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], IntConstant.v(-1), PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant > local)")
    void testUpdateCondGt11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant > local)")
    void testUpdateCondGt12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant > local)")
    void testUpdateCondGt13() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(4));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local > local)")
    void testUpdateCondGt31() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > local)")
    void testUpdateCondGt32() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > local)")
    void testUpdateCondGt33() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(0));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertTrue(out.updateCond(in, xs[1], xs[2], PredicateType.Gt)),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l = c)")
    void testUpdateState01() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l = c)")
    void testUpdateState02() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        in.add(xs[0], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l1 = l2)")
    void testUpdateState11() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2)")
    void testUpdateState12() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState001() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState002() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState003() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState004() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState011() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState012() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState013() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState014() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState021() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState022() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState023() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState024() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState031() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState032() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState033() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState034() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState101() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState102() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState103() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState104() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState111() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState112() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState113() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState114() {
        this.matrix.setConstraint(xs[1], xs[2], Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], Constraint.of(-3));
        IncZoneState in = new IncZoneState(this.matrix);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertTrue(out.isFeasible()),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(0),
                                     m2.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState121() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState122() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState123() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState124() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState131() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState132() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.of(1),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState133() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState134() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState201() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState202() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[3], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[3], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState203() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState204() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[1], Constraint.of(-3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState211() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState212() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[3], xs[0], Constraint.of(+4));
        in.add(xs[0], xs[3], Constraint.of(-4));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(-2),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(+2),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState213() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState214() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+4));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+2));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        assertAll(() -> assertEquals(Constraint.of(+2),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(+2),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState221() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState222() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[3], Constraint.of(-3));
        in.add(xs[3], xs[0], Constraint.of(+3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState223() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState224() {
        this.matrix.setConstraint(xs[1], xs[2], Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], Constraint.of(-4));
        this.matrix.setConstraint(xs[2], xs[0], Constraint.of(+4));
        IncZoneState in = new IncZoneState(matrix);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        assertAll(() -> assertEquals(Constraint.of(+8),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-8),
                                     m2.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-4),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.of(+4),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.of(-12),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+12),
                                     m2.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState231() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState232() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[3], xs[0], Constraint.of(-3));
        in.add(xs[0], xs[3], Constraint.of(+3));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState233() {
        IncZoneState in = new IncZoneState(this.locals, true);
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        assertEquals(new DifferenceBoundedMatrix(this.locals, true), matrix);
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState234() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        assertAll(() -> assertEquals(Constraint.of(+2),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-2),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState300() {
        this.matrix.setConstraint(xs[1], xs[2], Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], Constraint.of(-1));
        this.matrix.setConstraint(xs[2], xs[0], Constraint.of(+1));
        IncZoneState in = new IncZoneState(matrix);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[0], IntConstant.v(3), BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState301() {
        this.matrix.setConstraint(xs[1], xs[2], Constraint.of(+2));
        this.matrix.setConstraint(xs[2], xs[1], Constraint.of(-2));
        this.matrix.setConstraint(xs[0], xs[1], Constraint.of(-3));
        this.matrix.setConstraint(xs[1], xs[0], Constraint.of(+3));
        this.matrix.setConstraint(xs[0], xs[2], Constraint.of(-1));
        this.matrix.setConstraint(xs[2], xs[0], Constraint.of(+1));
        IncZoneState in = new IncZoneState(matrix);
        DifferenceBoundedMatrix m2 = new DifferenceBoundedMatrix(this.locals, true);
        IncZoneState out = new IncZoneState(m2);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     m2.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     m2.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m2.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState303() {
        IncZoneState in = new IncZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        IncZoneState out = new IncZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[3], xs[2], BinaryOperatorType.MODULUS);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[2], xs[1])));
    }
}
