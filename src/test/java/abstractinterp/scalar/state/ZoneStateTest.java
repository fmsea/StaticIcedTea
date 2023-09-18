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

public class ZoneStateTest {

    private Set<Local> locals;
    private Local[] xs;
    private SolverWrapper solver;
    private ZoneDifferenceBoundedMatrix matrix;

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
        this.locals = Stream.of(xs).collect(Collectors.toSet());
        this.matrix = new ZoneDifferenceBoundedMatrix(this.locals, true);
    }

    @Test
    @DisplayName("test add when matrix is top")
    void testAdd01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        out.add(xs[1], xs[2], Constraint.of(1));
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test add when matrix contains existing constraint")
    void testAdd02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.add(xs[1], xs[2], Constraint.of(3));
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≤ constant)")
    void testUpdateCondLe01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ constant)")
    void testUpdateCondLe02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ constant)")
    void testUpdateCondLe03() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≤ local)")
    void testUpdateCondLe11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≤ local)")
    void testUpdateCondLe12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = (out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Le));
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≤ local)")
    void testUpdateCondLe13() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≤ local)")
    void testUpdateCondLe31() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≤ local)")
    void testUpdateCondLe32() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≤ local)")
    void testUpdateCondLe33() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Le);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local < constant)")
    void testUpdateCondLt01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < constant)")
    void testUpdateCondLt02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < constant)")
    void testUpdateCondLt03() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant < local)")
    void testUpdateCondLt11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant < local)")
    void testUpdateCondLt12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(-1), xs[1], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant < local)")
    void testUpdateCondLt13() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local < local)")
    void testUpdateCondLt31() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local < local)")
    void testUpdateCondLt32() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local < local)")
    void testUpdateCondLt33() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Lt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[1], xs[2])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local = constant)")
    void testUpdateCondEq01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = constant)")
    void testUpdateCondEq02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(+1));
        in.add(xs[0], xs[1], Constraint.of(-1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = constant)")
    void testUpdateCondEq03() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(+2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(1), PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(+1), matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant = local)")
    void testUpdateCondEq11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant = local)")
    void testUpdateCondEq12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(3), xs[1], PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+3), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant = local)")
    void testUpdateCondEq13() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(0), xs[1], PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local = local)")
    void testUpdateCondEq31() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Eq);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local = local)")
    void testUpdateCondEq32() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[1], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)),
                  () -> assertFalse(matrix.computeClosure()));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local = local)")
    void testUpdateCondEq33() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(3));
        in.add(xs[2], xs[1], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        assertAll(() -> assertFalse(out.updateCond(in, xs[1], xs[2], PredicateType.Eq)),
                  () -> assertFalse(matrix.computeClosure()));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local ≥ constant)")
    void testUpdateCondGe01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ constant)")
    void testUpdateCondGe02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ constant)")
    void testUpdateCondGe03() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant ≥ local)")
    void testUpdateCondGe11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(2), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant ≥ local)")
    void testUpdateCondGe12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant ≥ local)")
    void testUpdateCondGe13() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(1), xs[1], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local ≥ local)")
    void testUpdateCondGe31() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local ≥ local)")
    void testUpdateCondGe32() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(-1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local ≥ local)")
    void testUpdateCondGe33() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Ge);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix ( local > constant)")
    void testUpdateCondGt01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(2), PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-3), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > constant)")
    void testUpdateCondGt02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(0), PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > constant)")
    void testUpdateCondGt03() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[1], Constraint.of(2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], IntConstant.v(-1), PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (constant > local)")
    void testUpdateCondGt11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (constant > local)")
    void testUpdateCondGt12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(0), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (constant > local)")
    void testUpdateCondGt13() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(4));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, IntConstant.v(2), xs[1], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(1), matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update condition with empty matrix (local > local)")
    void testUpdateCondGt31() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with more precise constraint (local > local)")
    void testUpdateCondGt32() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-2), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update condition with less precise constraint (local > local)")
    void testUpdateCondGt33() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[1], Constraint.of(0));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        Boolean result = out.updateCond(in, xs[1], xs[2], PredicateType.Gt);
        matrix.computeClosure();
        assertAll(() -> assertTrue(result),
                  () -> assertEquals(Constraint.of(-1), matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l = c)")
    void testUpdateState01() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l = c)")
    void testUpdateState02() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[0], Constraint.of(2));
        in.add(xs[0], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3));
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with open matrix (l1 = l2)")
    void testUpdateState11() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2)")
    void testUpdateState12() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2]);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState001() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState002() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState003() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState004() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.ADDITION);
        matrix.computeClosure();
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
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState012() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState013() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState014() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
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
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState022() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState023() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState024() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+8),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-8),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-9),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+9),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ c)")
    void testUpdateState031() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ c)")
    void testUpdateState032() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], IntConstant.v(3), BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ c)")
    void testUpdateState033() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ c)")
    void testUpdateState034() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], IntConstant.v(3), BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState101() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState102() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+3),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-3),
                                     matrix.getConstraint(xs[2], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState103() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState104() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
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
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState112() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
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
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState114() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState121() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState122() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState123() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState124() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+8),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-8),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-9),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+9),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = c ★ l2)")
    void testUpdateState131() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = c ★ l2)")
    void testUpdateState132() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(1),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = c ★ l1)")
    void testUpdateState133() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = c ★ l1)")
    void testUpdateState134() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[1], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+1),
                                     matrix.getConstraint(xs[1], xs[0])));

    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState201() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState202() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[3], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[3], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState203() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState204() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[1], Constraint.of(-3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.ADDITION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+5),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-5),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState211() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState212() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[3], xs[0], Constraint.of(+4));
        in.add(xs[0], xs[3], Constraint.of(-4));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(-2),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(+2),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState213() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState214() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+4));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+2));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.SUBTRACTION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(2),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(1),
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
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState222() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[3], Constraint.of(-3));
        in.add(xs[3], xs[0], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+6),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(-6),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState223() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState224() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[1], Constraint.of(+1));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-4));
        in.add(xs[2], xs[0], Constraint.of(+4));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.MULTIPLICATION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(+8),
                                     matrix.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.of(-8),
                                     matrix.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.of(-4),
                                     matrix.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.of(+4),
                                     matrix.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.of(-12),
                                     matrix.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(+12),
                                     matrix.getConstraint(xs[1], xs[0])));
    }

    @Test
    @DisplayName("test update state with empty matrix (l1 = l2 ★ l3)")
    void testUpdateState231() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state with bound matrix (l1 = l2 ★ l3)")
    void testUpdateState232() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[2], xs[0], Constraint.of(+2));
        in.add(xs[0], xs[2], Constraint.of(-2));
        in.add(xs[3], xs[0], Constraint.of(-3));
        in.add(xs[0], xs[3], Constraint.of(+3));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[2], xs[3], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(0),
                                     matrix.getConstraint(xs[0], xs[1])));
    }

    @Test
    @DisplayName("test update state, update value, with empty matrix (l1 = l1 ★ l2)")
    void testUpdateState233() {
        ZoneState in = new ZoneState(this.locals, true);
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
        assertAll(() -> assertEquals(new ZoneDifferenceBoundedMatrix(this.locals, true), matrix));
    }

    @Test
    @DisplayName("test update state, update value, with bound matrix (l1 = l1 ★ l2)")
    void testUpdateState234() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[1], xs[2], BinaryOperatorType.DIVISION);
        matrix.computeClosure();
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
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[0], IntConstant.v(3), BinaryOperatorType.MODULUS);
        matrix.computeClosure();
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

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState301() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, IntConstant.v(3), xs[2], BinaryOperatorType.MODULUS);
        matrix.computeClosure();
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

    @Test
    @DisplayName("test update state, forgets value if unknown binary operator")
    void testUpdateState303() {
        ZoneState in = new ZoneState(this.locals, true);
        in.add(xs[1], xs[2], Constraint.of(+2));
        in.add(xs[2], xs[1], Constraint.of(-2));
        in.add(xs[0], xs[1], Constraint.of(-3));
        in.add(xs[1], xs[0], Constraint.of(+3));
        in.add(xs[0], xs[2], Constraint.of(-1));
        in.add(xs[2], xs[0], Constraint.of(+1));
        ZoneState out = new ZoneState(matrix);
        in.copyTo(out);
        out.updateState(xs[1], in, xs[3], xs[2], BinaryOperatorType.MODULUS);
        matrix.computeClosure();
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
