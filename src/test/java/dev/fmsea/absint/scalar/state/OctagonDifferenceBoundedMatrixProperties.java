package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

public class OctagonDifferenceBoundedMatrixProperties {

    private void assertCanonical(OctagonDifferenceBoundedMatrix m) {
        assertAll(Stream.<Executable>concat(Stream.of(() -> assertTrue(m.isFeasible(), "Infeasible!")),
            IntStream.range(0, m.N).boxed().flatMap(k -> {
                return IntStream.range(0, m.N).boxed().flatMap(i -> {
                        return IntStream.range(0, m.N).boxed().flatMap(j -> {
                                if (i == j) {
                                    return Stream.<Executable>of(() -> assertEquals(Constraint.of(0),
                                        m.getConstraint(i, i),
                                        "self loop was not zero"));
                                } else {
                                    return Stream.<Executable>of(() -> assertTrue(Constraint.compare(m.getConstraint(i, j),
                                        Constraint.add(m.getConstraint(i, k),
                                            m.getConstraint(k, j))) <= 0,
                                        () -> String.format("closure is not closed: k = %d, i = %d, j = %d [%s > %s + %s = (%s)]",
                                            k, i, j, m.getConstraint(i, j),
                                            m.getConstraint(i, k),
                                            m.getConstraint(k, j),
                                            Constraint.add(m.getConstraint(i, k),
                                                m.getConstraint(k, j)))));
                                }
                            });
                    });
            })));
    }

    @Property
    boolean closureAndFeasibilityAreSame(@ForAll OctagonDifferenceBoundedMatrix m) {
        return m.computeClosure() == m.isFeasible();
    }

    @Property
    void canonicalOctagonsAreCoherent(@ForAll OctagonDifferenceBoundedMatrix m) {
        int N = m.size();
        if (m.canonicalize()) {
            assertAll(IntStream.range(0, N)
                      .boxed()
                      .flatMap(i -> {
                              return IntStream.range(0, N).boxed().flatMap(j -> {
                                      return Stream.<Executable>of(() -> assertEquals(m.getConstraint(i, j),
                                          m.getConstraint(j ^ 1, i ^ 1)));
                                  });
                          }));
        }
    }

    @Property
    void reduceOctagonsAreSubsetsOfFullyClosed(@ForAll OctagonDifferenceBoundedMatrix m) {
        if (m.canonicalize()) {
            OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(m);
            n.computeReducedClosure();
            assertTrue(n.isSubset(m));
        }
    }

    @Property
    void reducedOctagonsAreCoherent(@ForAll OctagonDifferenceBoundedMatrix m) {
        int N = m.size();
        if (m.canonicalize() && m.computeReducedClosure()) {
            assertAll(IntStream.range(0, N)
                      .boxed()
                      .flatMap(i -> {
                              return IntStream.range(0, N).boxed().flatMap(j -> {
                                      return Stream.<Executable>of(() -> assertEquals(m.getConstraint(i, j),
                                          m.getConstraint(j ^ 1, i ^ 1)));
                                  });
                          }));
        }
    }

    @Property
    void closureOfReducedOctagonsEqualClosure(@ForAll OctagonDifferenceBoundedMatrix m) {
        if (m.canonicalize()) {
            OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(m);
            n.computeReducedClosure();
            n.canonicalize();
            assertEquals(m, n);
        }
    }

    @Property
    void repeatedCanonicalizationIsIdempotent(@ForAll OctagonDifferenceBoundedMatrix m) {
        if (m.canonicalize()) {
            OctagonDifferenceBoundedMatrix copy = new OctagonDifferenceBoundedMatrix(m);
            m.canonicalize(true);
            assertEquals(copy, m);
        }
    }

    @Property
    void transitiveClosure(@ForAll OctagonDifferenceBoundedMatrix m) {
        int N = m.size();
        if (m.computeClosure()) {
            assertAll(Stream.concat(Stream.of(() -> assertTrue(m.isFeasible(), "Infeasible!")),
                                    IntStream.range(0, N).boxed().flatMap(k -> {
                                            return IntStream.range(0, N).boxed().flatMap(i -> {
                                                    return IntStream.range(0, N).boxed().flatMap(j -> {
                                                            if (i == j) {
                                                                return Stream.<Executable>of(() -> assertEquals(Constraint.of(0),
                                                                    m.getConstraint(i, j)));
                                                            } else {
                                                                return Stream.<Executable>of(() -> assertTrue(Constraint.compare(m.getConstraint(i, j),
                                                                    Constraint.add(m.getConstraint(i, k),
                                                                        m.getConstraint(k, j))) <= 0));
                                                            }
                                                        });
                                                });
                                        })));
        }
    }

    @Property
    void canonicalOctagons(@ForAll OctagonDifferenceBoundedMatrix m) {
        int N = m.size();
        OctagonDifferenceBoundedMatrix old = new OctagonDifferenceBoundedMatrix(m);
        if (m.canonicalize()) {
            assertCanonical(m);
        }
    }

    @Property
    void tightendOctagonsAreTight(@ForAll OctagonDifferenceBoundedMatrix m) {
        int N = m.size();
        m.computeClosure();
        m.tighten();
        assertAll(IntStream.range(0, N).boxed()
                  .flatMap(i -> Stream.of(() -> assertTrue(m.getConstraint(i, i ^ 1).bound().map(b -> b % 2 == 0).orElse(true),
                                                           () -> String.format("%d %d %s", i, i ^ 1, m.getConstraint(i, i ^ 1))))));
    }

    @Property
    boolean ZConsistentOctagons(@ForAll OctagonDifferenceBoundedMatrix m) {
        if (m.computeClosure()) {
            m.tighten();
            m.computeStrongClosure();

            return m.isZConsistent();
        } else {
            return true;
        }
    }

    @Property
    boolean tighteningDoesNotMakeInfeasible(@ForAll OctagonDifferenceBoundedMatrix m) {
        if (m.computeClosure()) {
            m.tighten();
            return m.isFeasible();
        }
        return true;
    }

    @Property
    boolean incrementallyClosedOctagonsAreCanonical(@ForAll OctagonDifferenceBoundedMatrix m,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= m.N;
        j %= m.N;
        if (m.canonicalize()) {
            if (m.incrementalClosure(i, j, c) && m.incrementalClosure(j ^ 1, i ^ 1, c)) {
                assertCanonical(m);
                return m.isZConsistent();
            }
        }
        return true;
    }

    @Property
    boolean deferredIncrementallyClosedOctagonsAreCanonical(@ForAll OctagonDifferenceBoundedMatrix m,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= m.N;
        j %= m.N;
        if (m.canonicalize()) {
            var thunks = List.of(
                ConstraintUpdateThunk.of(i, j, c),
                ConstraintUpdateThunk.of(j ^ 1, i ^ 1, c));
            if (m.deferredIncrementalClosure(thunks)) {
                assertCanonical(m);
                return m.isZConsistent();
            }
        }
        return true;
    }

    @Property
    boolean incrementalEquivalence(@ForAll OctagonDifferenceBoundedMatrix dbm,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= dbm.N;
        j %= dbm.N;
        if (dbm.canonicalize()) {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dbm);
            OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(dbm);
            if ((m.incrementalClosure(i, j, c) &&
                 m.incrementalClosure(j ^ 1, i ^ 1, c)) &&
                (n.incrementalZClosure(i, j, c) &&
                 n.incrementalZClosure(j ^ 1, i ^ 1, c))) {
                return m.equals(n);
            }
        }
        return true;
    }

    @Property
    boolean deferredIncremEquivalence(@ForAll OctagonDifferenceBoundedMatrix dbm,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= dbm.N;
        j %= dbm.N;
        if (dbm.canonicalize()) {
            var thunks = List.of(
                ConstraintUpdateThunk.of(i, j, c),
                ConstraintUpdateThunk.of(j ^ 1, i ^ 1, c));
            var m = new OctagonDifferenceBoundedMatrix(dbm);
            var n = new OctagonDifferenceBoundedMatrix(dbm);
            if (m.deferredIncrementalClosure(thunks, dbm) &&
                n.incrementalClosure(thunks, dbm)) {
                return m.equals(n);
            }
        }
        return true;
    }

    @Property
    boolean incrementalClosureEquivalence(@ForAll OctagonDifferenceBoundedMatrix dbm,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= dbm.N;
        j %= dbm.N;
        if (dbm.canonicalize()) {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dbm);
            OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(dbm);
            m.incrementalClosure(i, j, c);
            m.incrementalClosure(j ^ i, i ^ 1, c);
            n.putConstraint(i, j, c);
            n.putConstraint(j ^ 1, i ^ 1, c);
            n.canonicalize();
            if (m.isFeasible()) {
                if (n.isFeasible()) {
                    return m.equals(n);
                } else {
                    return false;
                }
            } else if (n.isFeasible()) {
                return false;
            }
        }
        return true;
    }

    @Property
    void deferredIncrementalClosureIsCanonical(@ForAll OctagonDifferenceBoundedMatrix dbm,
        @ForAll @Size(max=6) Set<ConstraintUpdateThunk> arbitraryThunks) {

        if (dbm.canonicalize()) {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dbm);
            int N = dbm.size();
            var thunks = arbitraryThunks.stream()
                .map(t -> ConstraintUpdateThunk.of(t.s % N, t.t % N, t.c))
                .collect(Collectors.toList());
            m.incrementalClosure(thunks, dbm);
            assertCanonical(m);
        }
    }

    @Property
    boolean deferredClosureEquivalence(@ForAll OctagonDifferenceBoundedMatrix dbm,
        @ForAll @IntRange(min = 0, max = 8) int i,
        @ForAll @IntRange(min = 0, max = 8) int j,
        @ForAll Constraint c) {
        i %= dbm.N;
        j %= dbm.N;
        if (dbm.canonicalize()) {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dbm);
            OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(dbm);
            m.incrementalClosure(i, j, c);
            m.incrementalClosure(j ^ i, i ^ 1, c);
            n.incrementalClosure(List.of(
                ConstraintUpdateThunk.of(i, j, c),
                ConstraintUpdateThunk.of(j ^ 1, i ^ 1, c)));
            if (m.isFeasible()) {
                if (n.isFeasible()) {
                    return m.equals(n);
                } else {
                    return false;
                }
            } else if (n.isFeasible()) {
                return false;
            }
        }
        return true;
    }

    @Property
    boolean wozReduecedReconcstruct(@ForAll OctagonDifferenceBoundedMatrix dbm) {
        if (dbm.canonicalize()) {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dbm);
            m.w0zReduction();
            m.canonicalize();
            return m.equals(dbm);
        }
        return true;
    }
}
