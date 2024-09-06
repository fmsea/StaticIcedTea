package dev.fmsea.absint.scalar.state;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import soot.Local;
import soot.IntType;
import soot.jimple.Jimple;

import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverFactory;

public class ZoneDifferenceBoundedMatrixTest {

    SolverWrapper solver;
    Set<Local> locals;
    Local[] xs;

    @BeforeEach
    void setup() {
        this.solver = SolverFactory.getSolver();
        this.xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
        };
        this.locals = Stream.of(this.xs).collect(Collectors.toSet());
    }

    private void checkMatrixCondition(BiConsumer<Local, Local> test) {
        this.locals.forEach(s -> {
                this.locals.forEach(t -> {
                        test.accept(s, t);
                    });
            });
    }

    @Test
    void testInitializeWithTop() {
        ZoneDifferenceBoundedMatrix matrix = new ZoneDifferenceBoundedMatrix(this.locals, true);
        checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    assertEquals(Constraint.of(0),
                                 matrix.getConstraint(s, t));
                } else {
                    assertEquals(Constraint.TOP(),
                                 matrix.getConstraint(s, t));
                }
            });
    }

    @Test
    void testInitializeWithoutTop() {
        ZoneDifferenceBoundedMatrix matrix = new ZoneDifferenceBoundedMatrix(this.locals, false);
        checkMatrixCondition((s, t) -> {
                assertEquals(Constraint.BOT(), matrix.getConstraint(s, t));
            });
    }

    @Test
    void testCopyConstructor() {
        ZoneDifferenceBoundedMatrix source = new ZoneDifferenceBoundedMatrix(this.locals, true);
        ZoneDifferenceBoundedMatrix target = new ZoneDifferenceBoundedMatrix(source);
        assertAll(() -> assertEquals(source, target),
                  () -> checkMatrixCondition((s, t) -> {
                          assertFalse(source.getConstraint(s, t) == target.getConstraint(s, t));
                      }));
    }

    @Test
    void testPutConstraint() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.putConstraint(xs[0], xs[1], Constraint.of(3));
            assertEquals(Constraint.of(3),
                         m.getConstraint(xs[0], xs[1]));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(3));
            m.putConstraint(xs[0], xs[1], Constraint.of(4));
            assertEquals(Constraint.of(3),
                         m.getConstraint(xs[0], xs[1]));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(4));
            m.putConstraint(xs[0], xs[1], Constraint.of(3));
            assertEquals(Constraint.of(3),
                         m.getConstraint(xs[0], xs[1]));
        }
    }

    @Test
    void testSetConstraint() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[1], Constraint.of(3));
            assertEquals(Constraint.of(0),
                         m.getConstraint(xs[1], xs[1]));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[1], Constraint.of(-3));
            assertAll(() -> assertFalse(m.isFeasible()),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[1], xs[1])));
        }
    }

    @Test
    void testUnion() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, false);
            m.union(n);
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.union(n);
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-2));
            n.setConstraint(xs[0], xs[2], Constraint.of(-3));
            n.setConstraint(xs[1], xs[0], Constraint.of(5));
            n.setConstraint(xs[1], xs[2], Constraint.of(2));
            n.setConstraint(xs[2], xs[0], Constraint.of(6));
            n.setConstraint(xs[2], xs[1], Constraint.of(1));
            m.union(n);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(5),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(6),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])));
        }
    }

    @Test
    void testIntersection() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, false);
            m.intersection(n);
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.intersection(n);
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-2));
            n.setConstraint(xs[0], xs[2], Constraint.of(-3));
            n.setConstraint(xs[1], xs[0], Constraint.of(5));
            n.setConstraint(xs[1], xs[2], Constraint.of(2));
            n.setConstraint(xs[2], xs[0], Constraint.of(6));
            n.setConstraint(xs[2], xs[1], Constraint.of(1));
            m.intersection(n);
            assertAll(() -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-3),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(1),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(1),
                                         m.getConstraint(xs[2], xs[1])));
        }
    }

    @Test
    void testIsFeasible() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertTrue(m.isFeasible());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertFalse(m.isFeasible());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertTrue(m.computeClosure()),
                      () -> assertTrue(m.isFeasible()));
        }
    }

    @Test
    void testIsSubset() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertAll(() -> assertFalse(m.isSubset(n)),
                      () -> assertTrue(n.isSubset(m)));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertAll(() -> assertTrue(m.isSubset(n)),
                      () -> assertFalse(n.isSubset(m)));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertTrue(m.isSubset(m));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-2));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-1));
            n.setConstraint(xs[0], xs[2], Constraint.of(0));
            n.setConstraint(xs[1], xs[0], Constraint.of(4));
            n.setConstraint(xs[1], xs[2], Constraint.of(2));
            n.setConstraint(xs[2], xs[0], Constraint.of(6));
            assertAll(() -> assertTrue(m.isSubset(n)),
                      () -> assertFalse(n.isSubset(m)));
        }
    }

    @Test
    void testClosure() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertTrue(m.computeClosure());
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertFalse(m.computeClosure());
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(5));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertTrue(m.computeClosure()),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(1),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(0));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertFalse(m.computeClosure()),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.BOT(),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(+2));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[2], Constraint.of(-3));
            m.setConstraint(xs[2], xs[1], Constraint.of(+2));
            assertFalse(m.computeClosure());
        }

        {
            Local[] xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
            };
            Set<Local> locals = new HashSet<>(5);
            for (Local x : xs) { locals.add(x); }

            // Figure 3 from https://doi.org/10.1109/REAL.1997.641265
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[3], Constraint.of(5));
            m.setConstraint(xs[1], xs[0], Constraint.of(3));
            m.setConstraint(xs[1], xs[2], Constraint.of(10));
            m.setConstraint(xs[1], xs[3], Constraint.of(2));
            m.setConstraint(xs[2], xs[1], Constraint.of(-4));
            m.setConstraint(xs[3], xs[2], Constraint.of(2));
            assertAll(() -> assertTrue(m.computeClosure()),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(7),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(5),
                                         m.getConstraint(xs[0], xs[3])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-4),
                                         m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.of(1),
                                         m.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[3], xs[1])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[3], xs[2])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[3], xs[3])));
        }
    }

    @Test
    void testReducedClosure() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertTrue(m.computeClosure());
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertFalse(m.computeClosure());
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(5));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertTrue(m.computeReducedClosure()),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(1),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            Local[] xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
            };
            Set<Local> locals = new HashSet<>(5);
            for (Local x : xs) { locals.add(x); }

            // Figure 3 from https://doi.org/10.1109/REAL.1997.641265
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[3], Constraint.of(5));
            m.setConstraint(xs[1], xs[0], Constraint.of(3));
            m.setConstraint(xs[1], xs[2], Constraint.of(10));
            m.setConstraint(xs[1], xs[3], Constraint.of(2));
            m.setConstraint(xs[2], xs[1], Constraint.of(-4));
            m.setConstraint(xs[3], xs[2], Constraint.of(2));
            assertAll(() -> assertTrue(m.computeReducedClosure()),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[0], xs[3])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-4),
                                         m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[3], xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[3], xs[2])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[3], xs[3])));
        }
    }

    @Test
    void testW0ZReduction() {
        Local[] xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
        };
        Set<Local> locals = Stream.of(xs).collect(Collectors.toSet());
        ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
        m.setConstraint(xs[0], xs[1], Constraint.of(-1));
        m.setConstraint(xs[0], xs[2], Constraint.of(-1));
        m.setConstraint(xs[1], xs[0], Constraint.of(4));
        m.setConstraint(xs[1], xs[2], Constraint.of(1));
        m.setConstraint(xs[2], xs[0], Constraint.of(3));
        m.setConstraint(xs[3], xs[0], Constraint.of(2));
        m.w0zReduction();
        assertAll(() -> assertEquals(Constraint.of(-1),
                                     m.getConstraint(xs[0], xs[1])),
                  () -> assertEquals(Constraint.of(-1),
                                     m.getConstraint(xs[0], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[0], xs[3])),
                  () -> assertEquals(Constraint.of(4),
                                     m.getConstraint(xs[1], xs[0])),
                  () -> assertEquals(Constraint.of(1),
                                     m.getConstraint(xs[1], xs[2])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[1], xs[3])),
                  () -> assertEquals(Constraint.of(3),
                                     m.getConstraint(xs[2], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[2], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[2], xs[3])),
                  () -> assertEquals(Constraint.of(2),
                                     m.getConstraint(xs[3], xs[0])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[3], xs[1])),
                  () -> assertEquals(Constraint.TOP(),
                                     m.getConstraint(xs[3], xs[2])));
    }

    @Test
    void testWidening() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, false);
            ZoneDifferenceBoundedMatrix c = ZoneDifferenceBoundedMatrix.widen(m, n);
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), c.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), c.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            ZoneDifferenceBoundedMatrix c = ZoneDifferenceBoundedMatrix.widen(m, n);
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), c.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), c.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-2));
            m.setConstraint(xs[0], xs[2], Constraint.of(0));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(2));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-1));
            n.setConstraint(xs[0], xs[2], Constraint.of(-1));
            n.setConstraint(xs[1], xs[0], Constraint.of(4));
            n.setConstraint(xs[1], xs[2], Constraint.of(1));
            n.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix c = ZoneDifferenceBoundedMatrix.widen(m, n);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         c.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(4),
                                         c.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(2),
                                         c.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         c.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         c.getConstraint(xs[2], xs[1])));
        }
    }

    @Test
    void testWideningWithSteps() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-2));
            m.setConstraint(xs[0], xs[2], Constraint.of(0));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(2));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(this.locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-1));
            n.setConstraint(xs[0], xs[2], Constraint.of(-1));
            n.setConstraint(xs[1], xs[0], Constraint.of(4));
            n.setConstraint(xs[1], xs[2], Constraint.of(1));
            n.setConstraint(xs[2], xs[0], Constraint.of(3));
            ZoneDifferenceBoundedMatrix c = ZoneDifferenceBoundedMatrix.widen(m, n, Set.of(10));
            assertAll(() -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.of(10),
                                         c.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         c.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(4),
                                         c.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(2),
                                         c.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         c.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         c.getConstraint(xs[2], xs[1])));
        }
    }

    @Test
    void testForget() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            m.computeClosure();
            m.forgetConstraints(xs[1]);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            m.computeClosure();
            m.forgetConstraints(xs[2]);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[2])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            Local[] xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
            };
            Set<Local> locals = new HashSet<>(5);
            for (Local x : xs) { locals.add(x); }
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(0));
            m.setConstraint(xs[1], xs[0], Constraint.of(0));
            m.setConstraint(xs[2], xs[0], Constraint.of(0));
            m.setConstraint(xs[0], xs[2], Constraint.of(0));
            m.forgetConstraints(xs[3]);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[0])));
            m.setConstraint(xs[3], xs[4], Constraint.of(-1));
            m.setConstraint(xs[4], xs[3], Constraint.of(+1));
            assertAll(() -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(0),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[3], xs[4])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[4], xs[3])));
        }

        {
            Local[] xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
                Jimple.v().newLocal("x5", IntType.v()),
                Jimple.v().newLocal("x6", IntType.v()),
            };
            int N = xs.length;
            Set<Local> locals = new HashSet<>();
            for (Local x : xs) { locals.add(x); }
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            // [[0, -1, -2, -3, -4, -5, -6],
            //  [1,  0, -1, -2, -3, -4, -5],
            //  [2,  1,  0, -1, -2, -3, -4],
            //  [3,  2,  1,  0, -1, -2, -3],
            //  [4,  3,  2,  1,  0, -1, -2],
            //  [5,  4,  3,  2,  1,  0, -1],
            //  [6,  5,  4,  3,  2,  1,  0]]
            IntStream.range(0, N).forEach(i -> {
                    IntStream.range(i, N).forEach(j -> {
                            m.setConstraint(xs[i], xs[j], Constraint.of(i - j));
                            m.setConstraint(xs[j], xs[i], Constraint.of(j - i));
                        });
                });
            m.forgetConstraints(xs[4]);
            IntStream.range(0, N).forEach(i -> {
                    IntStream.range(i, N).forEach(j -> {
                            if (i == j) {
                                assertAll(() -> assertEquals(Constraint.of(0),
                                                             m.getConstraint(xs[i], xs[j])));
                            } else if (i == 4 || j == 4) {
                                assertAll(() -> assertEquals(Constraint.TOP(),
                                                             m.getConstraint(xs[i], xs[j])),
                                          () -> assertEquals(Constraint.TOP(),
                                                             m.getConstraint(xs[j], xs[i])));
                            } else {
                                assertAll(() -> assertEquals(Constraint.of(i - j),
                                                             m.getConstraint(xs[i], xs[j])),
                                          () -> assertEquals(Constraint.of(j - i),
                                                             m.getConstraint(xs[j], xs[i])));
                            }
                        });
                });
        }

        {
            Local[] xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
            };
            Set<Local> locals = new HashSet<>();
            for (Local x : xs) { locals.add(x); }
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(0));
            m.setConstraint(xs[2], xs[1], Constraint.of(0));
            m.setConstraint(xs[2], xs[3], Constraint.of(-1));
            m.forgetConstraints(xs[2]);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[3])));
        }
    }

    @Test
    void testSimpleForget() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-2));
            m.setConstraint(xs[1], xs[0], Constraint.of(+1));
            m.setConstraint(xs[1], xs[2], Constraint.of(-1));
            m.setConstraint(xs[2], xs[0], Constraint.of(+2));
            m.setConstraint(xs[2], xs[1], Constraint.of(+1));
            m.forgetConstraintsSimple(xs[2]);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])));
        }

        {
            int N = 100;
            Local[] xs = Stream.concat(Stream.of(Variable.ZERO),
                                       IntStream.range(1, N+1).mapToObj(i -> Jimple.v().newLocal("x" + i, IntType.v())))
                .toArray(Local[]::new);
            Set<Local> locals = new HashSet<>();
            for (Local x : xs) { locals.add(x); }
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            // m = [[0, -1, -2, -3, -4, -5, -6, ..., -99],
            //      [1,  0, -1, -2, -3, -4, -5, ..., -98],
            //      [2,  1,  0, -1, -2, -3, -4, ..., -97],
            //      [3,  2,  1,  0, -1, -2, -3, ..., -96],
            //      [4,  3,  2,  1,  0, -1, -2, ..., -95],
            //      [5,  4,  3,  2,  1,  0, -1, ..., -94],
            //      [6,  5,  4,  3,  2,  1,  0, ..., -93],
            //      ...                                  ]
            IntStream.range(0, N).forEach(i -> {
                    IntStream.range(i, N).forEach(j -> {
                            m.setConstraint(xs[i], xs[j], Constraint.of(i - j));
                            m.setConstraint(xs[j], xs[i], Constraint.of(j - i));
                        });
                });
            m.forgetConstraintsSimple(xs[4]);
            IntStream.range(0, N).forEach(i -> {
                    IntStream.range(i, N).forEach(j -> {
                            if (i == j) {
                                assertAll(() -> assertEquals(Constraint.of(0),
                                                             m.getConstraint(xs[i], xs[j])));
                            } else if (i == 4 || j == 4) {
                                assertAll(() -> assertEquals(Constraint.TOP(),
                                                             m.getConstraint(xs[i], xs[j])),
                                          () -> assertEquals(Constraint.TOP(),
                                                             m.getConstraint(xs[j], xs[i])));
                            } else {
                                assertAll(() -> assertEquals(Constraint.of(i - j),
                                                             m.getConstraint(xs[i], xs[j])),
                                          () -> assertEquals(Constraint.of(j - i),
                                                             m.getConstraint(xs[j], xs[i])));
                            }
                        });
                });
        }
    }

    @Test
    void testProjection() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[2], xs[1], Constraint.of(3));
            m.setConstraint(xs[1], xs[2], Constraint.of(2));
            assertEquals(Interval32Box.TOP(), m.projectToInterval(xs[2]));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[2], xs[1], Constraint.of(3));
            m.setConstraint(xs[1], xs[0], Constraint.of(2));
            m.setConstraint(xs[2], xs[0], Constraint.of(5));
            assertAll(() -> assertEquals(new Interval32Box(null, 2),
                                         m.projectToInterval(xs[1])),
                      () -> assertEquals(new Interval32Box(null, 5),
                                         m.projectToInterval(xs[2])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[2], xs[1], Constraint.of(3));
            m.setConstraint(xs[1], xs[0], Constraint.of(2));
            m.setConstraint(xs[2], xs[0], Constraint.of(5));
            m.setConstraint(xs[0], xs[1], Constraint.of(3));
            m.setConstraint(xs[0], xs[2], Constraint.of(-2));
            assertAll(() -> assertEquals(new Interval32Box(-3, 2),
                                         m.projectToInterval(xs[1])),
                      () -> assertEquals(new Interval32Box(2, 5),
                                         m.projectToInterval(xs[2])));
        }
    }

    @Test
    void testMakeInfeasible() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            m.makeInfeasible();
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.makeInfeasible();
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            m.makeInfeasible();
            checkMatrixCondition((s, t) -> {
                    assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                });
        }
    }

    @Test
    void testToString() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            String expected = Stream.of("",
                                        "[[0, ⟙, ⟙],",
                                        " [⟙, 0, ⟙],",
                                        " [⟙, ⟙, 0]]").collect(Collectors.joining("\n"));
            assertEquals(expected, m.toString());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            String expected = Stream.of("",
                                        "[[⟘, ⟘, ⟘],",
                                        " [⟘, ⟘, ⟘],",
                                        " [⟘, ⟘, ⟘]]").collect(Collectors.joining("\n"));
            assertEquals(expected, m.toString());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("",
                                        "[[0, -1, -1],",
                                        " [4, 0, 1],",
                                        " [3, ⟙, 0]]").collect(Collectors.joining("\n"));
            assertEquals(expected, m.toString());
        }
    }

    @Test
    void testToSMT() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertEquals("true\n", m.toSMT(this.solver));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertEquals("false\n", m.toSMT(this.solver));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("(and (>= x1 1)",
                                        "(>= x2 1)",
                                        "(<= x2 3)",
                                        "(<= x1 (+ x2 1)))\n").collect(Collectors.joining(" "));
            assertEquals(expected, m.toSMT(this.solver));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(1));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("(and (= x1 1)",
                                        "(>= x2 1)",
                                        "(<= x2 3))\n").collect(Collectors.joining(" "));
            assertEquals(expected, m.toSMT(this.solver));
        }
    }

    @Test
    void testToBinop() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertTrue(m.toBinop().isEmpty());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertEquals("(= 0 1)", this.solver.smt2(m.toBinop().get()));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("(and (>= x1 1)",
                                        "(>= x2 1)",
                                        "(<= x2 3)",
                                        "(<= x1 (+ x2 1)))").collect(Collectors.joining(" "));
            assertEquals(expected, this.solver.smt2(m.toBinop().get()));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(1));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("(and (= x1 1)",
                                        "(>= x2 1)",
                                        "(<= x2 3))").collect(Collectors.joining(" "));
            assertEquals(expected, this.solver.smt2(m.toBinop().get()));
        }
    }

    @Test
    void testLocalToSMT() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertEquals("false", m.toSMT(xs[1], this.solver));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertAll(() -> assertEquals("true", m.toSMT(xs[1], this.solver)),
                      () -> assertEquals("true", m.toSMT(xs[2], this.solver)));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]), m.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Stream.of("(and (<= x1 4)",
                                                   "(<= x1 (+ x2 1))",
                                                   "(>= x1 1)",
                                                   "(<= x2 3)",
                                                   "(>= x2 1))")
                                         .collect(Collectors.joining(" ")),
                                         m.toSMT(xs[1], this.solver)),
                      () -> assertEquals(Set.of(xs[1], xs[2]), m.getConnectedVariablesOf(xs[2])),
                      () -> assertEquals(Stream.of("(and (<= x1 4)",
                                                   "(<= x1 (+ x2 1))",
                                                   "(>= x1 1)",
                                                   "(<= x2 3)",
                                                   "(>= x2 1))")
                                         .collect(Collectors.joining(" ")),
                                         m.toSMT(xs[2], this.solver)));
        }
    }

    @Test
    void testReachableQueries() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertEquals(Set.of(xs[1]), m.getReachableVariablesOf(xs[1]));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertAll(() -> assertEquals(Set.of(xs[1]), m.getReachableVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), m.getReachableVariablesOf(xs[2])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(-2));
            m.computeClosure();
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]),
                                         m.getReachableVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), m.getReachableVariablesOf(xs[2])));
        }

        {
            this.xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
            };
            this.locals = Stream.of(this.xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(-1));
            m.setConstraint(xs[3], xs[2], Constraint.of(+3));
            m.computeClosure();
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]),
                                         m.getReachableVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), m.getReachableVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[2], xs[3]), m.getReachableVariablesOf(xs[3])));
        }

        {
            this.xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
            };
            this.locals = Stream.of(this.xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(0));
            m.setConstraint(xs[1], xs[3], Constraint.of(0));
            m.setConstraint(xs[1], xs[4], Constraint.of(0));
            m.setConstraint(xs[4], xs[1], Constraint.of(0));
            m.computeClosure();
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[4]), m.getReachableVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), m.getReachableVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[3]), m.getReachableVariablesOf(xs[3])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[4]), m.getReachableVariablesOf(xs[4])));
        }

        {
            this.xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
                Jimple.v().newLocal("x5", IntType.v()),
                Jimple.v().newLocal("x6", IntType.v()),
            };
            this.locals = Stream.of(this.xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[2], xs[1], Constraint.of(0));
            m.setConstraint(xs[2], xs[3], Constraint.of(0));
            m.setConstraint(xs[2], xs[4], Constraint.of(0));
            m.setConstraint(xs[3], xs[2], Constraint.of(0));
            m.setConstraint(xs[4], xs[6], Constraint.of(0));
            m.setConstraint(xs[5], xs[4], Constraint.of(0));
            m.computeClosure();
            assertAll(() -> assertEquals(Set.of(xs[1]), m.getReachableVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[6]), m.getReachableVariablesOf(xs[6])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[4], xs[6]),
                                         m.getReachableVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[4], xs[6]),
                                         m.getReachableVariablesOf(xs[3])),
                      () -> assertEquals(Set.of(xs[4], xs[6]),
                                         m.getReachableVariablesOf(xs[4])),
                      () -> assertEquals(Set.of(xs[4], xs[5], xs[6]),
                                         m.getReachableVariablesOf(xs[5])));
        }
    }
    @Test
    void testLocalsToSMT() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, false);
            assertAll(() -> assertEquals("(<= x1 (+ x1 1))",
                                         m.toSMT(xs[1], xs[1], this.solver)),
                      () -> assertEquals("(and (<= x1 (+ x2 0)) (> x1 (+ x2 0)))",
                                         m.toSMT(xs[1], xs[2], this.solver)));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            assertAll(() -> assertEquals("(or (<= x1 0) (> x1 0))",
                                         m.toSMT(xs[0], xs[1], this.solver)),
                      () -> assertEquals("(or (<= x1 0) (> x1 0))",
                                         m.toSMT(xs[1], xs[0], this.solver)),
                      () -> assertEquals("(or (<= x1 (+ x2 0)) (> x1 (+ x2 0)))",
                                         m.toSMT(xs[1], xs[2], this.solver)));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(4));
            m.setConstraint(xs[1], xs[2], Constraint.of(1));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            String expected = Stream.of("(and (>= x1 1)",
                                        "(>= x2 1)",
                                        "(<= x1 4)",
                                        "(<= x1 (+ x2 1))",
                                        "(<= x2 3))").collect(Collectors.joining(" "));
            assertAll(() -> assertEquals("(>= x1 1)",
                                         m.toSMT(xs[0], xs[1], this.solver)),
                      () -> assertEquals("(<= x1 4)",
                                         m.toSMT(xs[1], xs[0], this.solver)),
                      () -> assertEquals("(<= x1 (+ x2 1))",
                                         m.toSMT(xs[1], xs[2], this.solver)));
        }
    }

    @Test
    void testAddIncoming() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.addIncoming(xs[1], Constraint.of(3));
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(+2));
            m.setConstraint(xs[2], xs[1], Constraint.of(+3));
            m.setConstraint(xs[2], xs[0], Constraint.of(+3));
            m.setConstraint(xs[0], xs[2], Constraint.of(-3));
            m.addIncoming(xs[1], Constraint.of(2));
            assertAll(() -> assertEquals(Constraint.of(0), m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(2), m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(5), m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-3), m.getConstraint(xs[0], xs[2])));
        }
    }

    @Test
    void testAddOutgoing() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.addIncoming(xs[1], Constraint.of(3));
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(+2));
            m.setConstraint(xs[2], xs[1], Constraint.of(+3));
            m.setConstraint(xs[2], xs[0], Constraint.of(+3));
            m.setConstraint(xs[0], xs[2], Constraint.of(-3));
            m.addOutgoing(xs[1], Constraint.of(2));
            assertAll(() -> assertEquals(Constraint.of(0), m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(4), m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-3), m.getConstraint(xs[0], xs[2])));
        }
    }

    @Test
    void testSubIncoming() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.addIncoming(xs[1], Constraint.of(3));
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(+2));
            m.setConstraint(xs[2], xs[1], Constraint.of(+3));
            m.setConstraint(xs[2], xs[0], Constraint.of(+3));
            m.setConstraint(xs[0], xs[2], Constraint.of(-3));
            m.subIncoming(xs[1], Constraint.of(2));
            assertAll(() -> assertEquals(Constraint.of(0), m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(2), m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(1), m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-3), m.getConstraint(xs[0], xs[2])));
        }
    }

    @Test
    void testSubOutgoing() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.addIncoming(xs[1], Constraint.of(3));
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                });
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(this.locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(+2));
            m.setConstraint(xs[2], xs[1], Constraint.of(+3));
            m.setConstraint(xs[2], xs[0], Constraint.of(+3));
            m.setConstraint(xs[0], xs[2], Constraint.of(-3));
            m.subOutgoing(xs[1], Constraint.of(2));
            assertAll(() -> assertEquals(Constraint.of(0), m.getConstraint(xs[1], xs[1])),
                      () -> assertEquals(Constraint.of(0), m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(3), m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(-3), m.getConstraint(xs[0], xs[2])));
        }
    }

    @Test
    void testIncrementalClosure() {
        Local[] xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
            Jimple.v().newLocal("x4", IntType.v()),
        };
        Set<Local> locals = new HashSet<>();
        for (Local x : xs) { locals.add(x); }
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(1));
            m.setConstraint(xs[0], xs[2], Constraint.of(4));
            // add and close
            assertAll(() -> assertTrue(m.putIncremental(xs[1], xs[2], Constraint.of(2))),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[1], xs[2])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(2));
            m.setConstraint(xs[1], xs[4], Constraint.of(15));
            m.setConstraint(xs[3], xs[4], Constraint.of(5));
            // add and close
            assertAll(() -> assertTrue(m.putIncremental(xs[2], xs[3], Constraint.of(4))),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.of(6),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.of(9),
                                         m.getConstraint(xs[2], xs[4])),
                      () -> assertEquals(Constraint.of(11),
                                         m.getConstraint(xs[1], xs[4])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[4], Constraint.of(3));
            m.setConstraint(xs[1], xs[0], Constraint.of(5));
            m.setConstraint(xs[1], xs[2], Constraint.of(2));
            m.setConstraint(xs[1], xs[3], Constraint.of(4));
            m.setConstraint(xs[1], xs[4], Constraint.of(8));
            m.setConstraint(xs[2], xs[0], Constraint.of(3));
            m.setConstraint(xs[2], xs[3], Constraint.of(2));
            m.setConstraint(xs[2], xs[4], Constraint.of(6));

            assertAll(() -> assertTrue(m.putIncremental(xs[2], xs[0], Constraint.of(2))),
                      () -> assertEquals(Constraint.of(3),
                                         m.getConstraint(xs[0], xs[4])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.of(4),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.of(7),
                                         m.getConstraint(xs[1], xs[4])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.of(2),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.of(5),
                                         m.getConstraint(xs[2], xs[4])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(+2));
            m.setConstraint(xs[1], xs[3], Constraint.of(-1));
            m.setConstraint(xs[2], xs[3], Constraint.of(-3));
            assertAll(() -> assertFalse(m.putIncremental(xs[3], xs[2], Constraint.of(2))));
        }
    }

    @Test
    void testConstantsCache() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            assertTrue(m.getConstants().isEmpty());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(+2));
            assertTrue(m.getConstants().isEmpty());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(+2));
            m.setConstraint(xs[1], xs[0], Constraint.of(-2));
            assertAll(() -> assertFalse(m.getConstants().isEmpty()),
                      () -> assertTrue(m.getConstants().contains(xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(+2));
            m.setConstraint(xs[1], xs[0], Constraint.of(-2));
            m.setConstraint(xs[1], xs[2], Constraint.of(+3));
            assertAll(() -> assertFalse(m.getConstants().isEmpty()),
                      () -> assertTrue(m.getConstants().contains(xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(+2));
            m.setConstraint(xs[1], xs[0], Constraint.of(-2));
            m.setConstraint(xs[1], xs[0], Constraint.of(+3));
            assertTrue(m.getConstants().isEmpty());
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(+1));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-1));
            n.setConstraint(xs[1], xs[0], Constraint.of(+1));
            m.union(n);
            assertAll(() -> assertFalse(m.getConstants().isEmpty()),
                      () -> assertTrue(m.getConstants().contains(xs[1])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(+1));
            m.setConstraint(xs[2], xs[0], Constraint.of(+1));
            ZoneDifferenceBoundedMatrix n = new ZoneDifferenceBoundedMatrix(locals, true);
            n.setConstraint(xs[0], xs[1], Constraint.of(-1));
            n.setConstraint(xs[0], xs[2], Constraint.of(-1));
            n.setConstraint(xs[1], xs[0], Constraint.of(+2));
            n.setConstraint(xs[2], xs[0], Constraint.of(+1));
            m.union(n);
            assertAll(() -> assertFalse(m.getConstants().isEmpty()),
                      () -> assertFalse(m.getConstants().contains(xs[1])),
                      () -> assertTrue(m.getConstants().contains(xs[2])));
        }
    }

    @Test
    void testCloseConstants() {
        Local[] xs = new Local[] {
            Variable.ZERO,
            Jimple.v().newLocal("x1", IntType.v()),
            Jimple.v().newLocal("x2", IntType.v()),
            Jimple.v().newLocal("x3", IntType.v()),
            Jimple.v().newLocal("x4", IntType.v()),
        };
        Set<Local> locals= new HashSet<>(5);
        Set<Local> notConstant = new HashSet<>();
        notConstant.add(xs[3]);
        for (Local x : xs) { locals.add(x); }
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-0));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[0], xs[3], Constraint.of(-2));
            m.setConstraint(xs[1], xs[0], Constraint.of(+0));
            m.setConstraint(xs[2], xs[0], Constraint.of(+1));
            m.setConstraint(xs[3], xs[0], Constraint.of(+2));
            m.closeConstants(notConstant);
            assertAll(() -> assertEquals(Constraint.of(-0),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[0], xs[3])),
                      () -> assertEquals(Constraint.of(+0),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.of(+2),
                                         m.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.of(+2),
                                         m.getConstraint(xs[3], xs[1])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[3], xs[2])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-0));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[0], xs[3], Constraint.of(-2));
            m.setConstraint(xs[1], xs[0], Constraint.of(+0));
            m.setConstraint(xs[2], xs[0], Constraint.of(+1));
            m.setConstraint(xs[3], xs[0], Constraint.of(+2));
            m.setConstraint(xs[4], xs[0], Constraint.of(+1));
            m.setConstraint(xs[4], xs[2], Constraint.of(+0));
            m.closeConstants(notConstant);
            assertAll(() -> assertEquals(Constraint.of(-0),
                                         m.getConstraint(xs[0], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[0], xs[2])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[0], xs[3])),
                      () -> assertEquals(Constraint.of(+0),
                                         m.getConstraint(xs[1], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[1], xs[2])),
                      () -> assertEquals(Constraint.of(-2),
                                         m.getConstraint(xs[1], xs[3])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[2], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[2], xs[1])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[2], xs[3])),
                      () -> assertEquals(Constraint.of(+2),
                                         m.getConstraint(xs[3], xs[0])),
                      () -> assertEquals(Constraint.of(+2),
                                         m.getConstraint(xs[3], xs[1])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[3], xs[2])),
                      () -> assertEquals(Constraint.of(+1),
                                         m.getConstraint(xs[4], xs[0])),
                      () -> assertEquals(Constraint.TOP(),
                                         m.getConstraint(xs[4], xs[1])),
                      () -> assertEquals(Constraint.of(+0),
                                         m.getConstraint(xs[4], xs[2])),
                      () -> assertEquals(Constraint.of(-1),
                                         m.getConstraint(xs[4], xs[3])));
        }
    }

    @Test
    void testGetConnectedVariables() {
        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[0], Constraint.of(0));
            m.setConstraint(xs[2], xs[0], Constraint.of(1));
            m.setConstraint(xs[0], xs[1], Constraint.of(0));
            m.setConstraint(xs[0], xs[2], Constraint.of(1));
            assertAll(() -> assertEquals(Set.of(), m.getConnectedVariablesOf(xs[0])),
                      () -> assertEquals(Set.of(xs[1]), m.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[2]), m.getConnectedVariablesOf(xs[2])));
        }

        {
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(0));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2]),
                                         m.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2]), m.getConnectedVariablesOf(xs[2])));
        }

        {
            xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
            };
            locals = Stream.of(xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[1], xs[2], Constraint.of(0));
            m.setConstraint(xs[2], xs[3], Constraint.of(1));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2], xs[3]),
                                         m.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3]),
                                         m.getConnectedVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3]),
                                         m.getConnectedVariablesOf(xs[3])));
        }

        {
            xs = new Local[] {
                Variable.ZERO,
                Jimple.v().newLocal("x1", IntType.v()),
                Jimple.v().newLocal("x2", IntType.v()),
                Jimple.v().newLocal("x3", IntType.v()),
                Jimple.v().newLocal("x4", IntType.v()),
                Jimple.v().newLocal("x5", IntType.v()),
                Jimple.v().newLocal("x6", IntType.v()),
            };
            locals = Stream.of(xs).collect(Collectors.toSet());
            ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(locals, true);
            m.setConstraint(xs[0], xs[1], Constraint.of(-1));
            m.setConstraint(xs[0], xs[2], Constraint.of(-1));
            m.setConstraint(xs[0], xs[5], Constraint.of(-1));
            m.setConstraint(xs[1], xs[0], Constraint.of(+4));
            m.setConstraint(xs[1], xs[2], Constraint.of(+1));
            m.setConstraint(xs[2], xs[0], Constraint.of(+3));
            m.setConstraint(xs[3], xs[1], Constraint.of(+1));
            m.setConstraint(xs[3], xs[2], Constraint.of(+2));
            m.setConstraint(xs[3], xs[6], Constraint.of(+2));
            m.setConstraint(xs[5], xs[0], Constraint.of(+1));
            assertAll(() -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         m.getConnectedVariablesOf(xs[1])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         m.getConnectedVariablesOf(xs[2])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         m.getConnectedVariablesOf(xs[3])),
                      () -> assertEquals(Set.of(xs[1], xs[2], xs[3], xs[6]),
                                         m.getConnectedVariablesOf(xs[6])),
                      () -> assertEquals(Set.of(xs[5]),
                                         m.getConnectedVariablesOf(xs[5])));
        }
    }

}
