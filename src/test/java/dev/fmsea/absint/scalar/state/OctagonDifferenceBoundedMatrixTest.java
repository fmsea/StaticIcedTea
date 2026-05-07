package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OctagonDifferenceBoundedMatrixTest {

    static int N = 6;
    private static Stream<Executable> checkMatrixCondition(BiFunction<Integer, Integer, Executable> test) {
        return IntStream.range(0, N)
            .boxed()
            .flatMap(i -> {
                    return IntStream.range(0, N)
                        .mapToObj(j -> test.apply(i, j));
                });
    }

    @Test
    void testInitializeWithTop() {
        OctagonDifferenceBoundedMatrix matrix = new OctagonDifferenceBoundedMatrix(N, true);
        assertAll(checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0),
                                                  matrix.getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(),
                                                  matrix.getConstraint(s, t));
                    }
                }));
    }

    @Test
    void testInitializeWithoutTop() {
        OctagonDifferenceBoundedMatrix matrix = new OctagonDifferenceBoundedMatrix(N, false);
        assertAll(checkMatrixCondition((s, t) -> {
                    return () -> assertEquals(Constraint.BOT(), matrix.getConstraint(s, t));
                }));
    }

    @Test
    void testCopyConstructor() {
        OctagonDifferenceBoundedMatrix source = new OctagonDifferenceBoundedMatrix(N, true);
        OctagonDifferenceBoundedMatrix target = new OctagonDifferenceBoundedMatrix(source);
        assertAll(Stream.concat(Stream.of(() -> assertEquals(source, target)),
                                checkMatrixCondition((s, t) -> {
                                        return () -> assertFalse(source.getConstraint(s, t) == target.getConstraint(s, t));
                                    })));
    }

    private static Stream<Arguments> putConstraintArguments() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(3))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(4))
            .build(),
        };

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops = List.of((m) -> m.putConstraint(0, 1, Constraint.of(3)),
                                                                     (m) -> m.putConstraint(0, 1, Constraint.of(4)),
                                                                     (m) -> m.putConstraint(0, 1, Constraint.of(3)));

        List<Stream<Executable>> oracles = List.of(Stream.of(() -> assertEquals(Constraint.of(3), ms[0].getConstraint(0, 1))),
                                                   Stream.of(() -> assertEquals(Constraint.of(3), ms[1].getConstraint(0, 1))),
                                                   Stream.of(() -> assertEquals(Constraint.of(3), ms[2].getConstraint(0, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("putConstraintArguments")
    void testPutConstraint(OctagonDifferenceBoundedMatrix matrix,
                           Consumer<OctagonDifferenceBoundedMatrix> action,
                           Stream<Executable> oracles) {
        action.accept(matrix);
        assertAll(oracles);
    }

    @Test
    void testSettingSelfLoopsDoesNotAlterLoop() {
        {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
            for (int i = 0; i < N; i++) {
                m.setConstraint(i, i, Constraint.of(3));
            }
            assertAll(IntStream.range(0, N)
                      .mapToObj(i -> () -> assertEquals(Constraint.of(0),
                                                        m.getConstraint(i, i))));
        }
    }

    @Test
    void testSettingSelfLoopToNegativeValueMakesMatrixInfeasiable() {
        {
            OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(N, true);
            for (int i = 0; i < N; i++) {
                m.setConstraint(i, i, Constraint.of(-1));
            }
            assertAll(Stream.concat(Stream.of(() -> assertFalse(m.isFeasible())),
                                    IntStream.range(0, N)
                                    .mapToObj(i -> () -> assertEquals(Constraint.BOT(),
                                                                      m.getConstraint(i, i)))));
        }
    }

    private static Stream<Arguments> unionTestData() {
        OctagonDifferenceBoundedMatrix[][] mn = new OctagonDifferenceBoundedMatrix[][] {
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, false),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, false),
                new OctagonDifferenceBoundedMatrix(N, true),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-2))
                .setConstraint(0, 2, Constraint.of(-3))
                .setConstraint(1, 0, Constraint.of(5))
                .setConstraint(1, 2, Constraint.of(2))
                .setConstraint(2, 0, Constraint.of(6))
                .setConstraint(2, 1, Constraint.of(1))
                .build(),
            },
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), mn[0][0].getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), mn[0][0].getConstraint(s, t));
                    }
                }),
            checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), mn[1][0].getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), mn[1][0].getConstraint(s, t));
                    }
                }),
            Stream.of(() -> assertEquals(Constraint.of(-1),
                                         mn[2][0].getConstraint(0, 1)),
                      () -> assertEquals(Constraint.of(-1),
                                         mn[2][0].getConstraint(0, 2)),
                      () -> assertEquals(Constraint.of(5),
                                         mn[2][0].getConstraint(1, 0)),
                      () -> assertEquals(Constraint.of(2),
                                         mn[2][0].getConstraint(1, 2)),
                      () -> assertEquals(Constraint.of(6),
                                         mn[2][0].getConstraint(2, 0)),
                      () -> assertEquals(Constraint.TOP(),
                                         mn[2][0].getConstraint(2, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(mn[i][0], mn[i][1], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("unionTestData")
    void testUnion(OctagonDifferenceBoundedMatrix m,
                   OctagonDifferenceBoundedMatrix n,
                   Stream<Executable> oracles) {
        m.union(n);
        assertAll(oracles);
    }

    private static Stream<Arguments> intersectionTestData() {
        OctagonDifferenceBoundedMatrix[][] mn = new OctagonDifferenceBoundedMatrix[][] {
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, false),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, false),
                new OctagonDifferenceBoundedMatrix(N, true),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-2))
                .setConstraint(0, 2, Constraint.of(-3))
                .setConstraint(1, 0, Constraint.of(5))
                .setConstraint(1, 2, Constraint.of(2))
                .setConstraint(2, 0, Constraint.of(6))
                .setConstraint(2, 1, Constraint.of(1))
                .build(),
            },
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                    return () -> assertEquals(Constraint.BOT(), mn[0][0].getConstraint(s, t));
                }),
            checkMatrixCondition((s, t) -> {
                    return () -> assertEquals(Constraint.BOT(), mn[1][0].getConstraint(s, t));
                }),

            Stream.of(() -> assertEquals(Constraint.of(-2),
                                         mn[2][0].getConstraint(0, 1)),
                      () -> assertEquals(Constraint.of(-3),
                                         mn[2][0].getConstraint(0, 2)),
                      () -> assertEquals(Constraint.of(4),
                                         mn[2][0].getConstraint(1, 0)),
                      () -> assertEquals(Constraint.of(1),
                                         mn[2][0].getConstraint(1, 2)),
                      () -> assertEquals(Constraint.of(3),
                                         mn[2][0].getConstraint(2, 0)),
                      () -> assertEquals(Constraint.of(1),
                                         mn[2][0].getConstraint(2, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(mn[i][0], mn[i][1], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("intersectionTestData")
    void testIntersection(OctagonDifferenceBoundedMatrix m,
                          OctagonDifferenceBoundedMatrix n,
                          Stream<Executable> oracles) {
        m.intersection(n);
        assertAll(oracles);

    }

    private static Stream<Arguments> isFeasibleTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(-1))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 0, Constraint.of(4))
            .setConstraint(1, 2, Constraint.of(1))
            .setConstraint(2, 0, Constraint.of(3))
            .build(),
        };

        List<Stream<Executable>> oracles = List.of(Stream.of(() -> assertTrue(ms[0].isFeasible())),
                                                   Stream.of(() -> assertFalse(ms[1].isFeasible())),
                                                   Stream.of(() -> assertTrue(ms[2].computeClosure()),
                                                             () -> assertTrue(ms[2].isFeasible())));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("isFeasibleTestData")
    void testIsFeasible(OctagonDifferenceBoundedMatrix m, Stream<Executable> oracles) {
        assertAll(oracles);
    }

    private static Stream<Arguments> isSubsetTestData() {
        OctagonDifferenceBoundedMatrix[][] mn = new OctagonDifferenceBoundedMatrix[][] {
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, false),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
                new OctagonDifferenceBoundedMatrix(N, true),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-2))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(0))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(2))
                .setConstraint(2, 0, Constraint.of(6))
                .build(),
            },
        };

        boolean[][] oracles = new boolean[][] {
            new boolean[] {false, true,},
            new boolean[] {true, false,},
            new boolean[] {true, true,},
            new boolean[] {true, false,},
        };

        return IntStream.range(0, oracles.length)
            .mapToObj(i -> Arguments.arguments(mn[i][0], mn[i][1], oracles[i][0], oracles[i][1]));
    }

    @ParameterizedTest
    @MethodSource("isSubsetTestData")
    void testIsSubset(OctagonDifferenceBoundedMatrix m,
                      OctagonDifferenceBoundedMatrix n,
                      boolean oracle1,
                      boolean oracle2) {
        assertAll(() -> assertEquals(oracle1, m.isSubset(n)),
                  () -> assertEquals(oracle2, n.isSubset(m)));
    }

    private static Stream<Arguments> closureTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(-1))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 0, Constraint.of(5))
            .setConstraint(1, 2, Constraint.of(1))
            .setConstraint(2, 0, Constraint.of(3))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(-1))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 0, Constraint.of(0))
            .setConstraint(1, 2, Constraint.of(1))
            .setConstraint(2, 0, Constraint.of(3))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(+2))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 2, Constraint.of(-3))
            .setConstraint(2, 1, Constraint.of(+2))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(6))
            .setConstraint(0, 2, Constraint.of(10))
            .setConstraint(0, 4, Constraint.of(2))
            .setConstraint(1, 3, Constraint.of(-4))
            .setConstraint(2, 0, Constraint.of(-4))
            .setConstraint(3, 1, Constraint.of(10))
            .setConstraint(3, 5, Constraint.of(2))
            .setConstraint(4, 2, Constraint.of(2))
            .setConstraint(5, 1, Constraint.of(2))
            .setConstraint(5, 4, Constraint.of(10))
            .build(),
        };
        boolean[] expectedFeasibility = new boolean[] {
            true,
            false,
            true,
            false,
            false,
            true,
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), ms[0].getConstraint(s, t), "not zero");
                    } else {
                        return () -> assertEquals(Constraint.TOP(), ms[0].getConstraint(s, t), "not top");
                    }
                    }),
            checkMatrixCondition((s, t) -> {
                    return () -> assertEquals(Constraint.BOT(), ms[1].getConstraint(s, t), "Not Bottom?");
                }),
            Stream.of(() -> assertEquals(Constraint.of(0),
                                         ms[2].getConstraint(0, 0)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[2].getConstraint(1, 1)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[2].getConstraint(2, 2)),
                      () -> assertEquals(Constraint.of(-1),
                                         ms[2].getConstraint(0, 1)),
                      () -> assertEquals(Constraint.of(-1),
                                         ms[2].getConstraint(0, 2)),
                      () -> assertEquals(Constraint.of(4),
                                         ms[2].getConstraint(1, 0)),
                      () -> assertEquals(Constraint.of(1),
                                         ms[2].getConstraint(1, 2)),
                      () -> assertEquals(Constraint.of(3),
                                         ms[2].getConstraint(2, 0)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[2].getConstraint(2, 1))),
            Stream.of(() -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(0, 0)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(1, 1)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(2, 2)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(0, 1)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(0, 2)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(1, 0)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(1, 2)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(2, 0)),
                      () -> assertEquals(Constraint.BOT(),
                                         ms[3].getConstraint(2, 1))),
            Stream.of(),
            Stream.of(() -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(0, 0)),
                      () -> assertEquals(Constraint.of(6),
                                         ms[5].getConstraint(0, 1)),
                      () -> assertEquals(Constraint.of(4),
                                         ms[5].getConstraint(0, 2)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(0, 3)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(0, 4)),
                      () -> assertEquals(Constraint.of(4),
                                         ms[5].getConstraint(0, 5)),
                      () -> assertEquals(Constraint.of(6),
                                         ms[5].getConstraint(1, 0)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(1, 1)),
                      () -> assertEquals(Constraint.of(10),
                                         ms[5].getConstraint(1, 2)),
                      () -> assertEquals(Constraint.of(-4),
                                         ms[5].getConstraint(1, 3)),
                      () -> assertEquals(Constraint.of(8),
                                         ms[5].getConstraint(1, 4)),
                      () -> assertEquals(Constraint.of(-2),
                                         ms[5].getConstraint(1, 5)),
                      () -> assertEquals(Constraint.of(-4),
                                         ms[5].getConstraint(2, 0)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(2, 1)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(2, 2)),
                      () -> assertEquals(Constraint.of(-2),
                                         ms[5].getConstraint(2, 3)),
                      () -> assertEquals(Constraint.of(-2),
                                         ms[5].getConstraint(2, 4)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(2, 5)),
                      () -> assertEquals(Constraint.of(10),
                                         ms[5].getConstraint(3, 0)),
                      () -> assertEquals(Constraint.of(4),
                                         ms[5].getConstraint(3, 1)),
                      () -> assertEquals(Constraint.of(14),
                                         ms[5].getConstraint(3, 2)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(3, 3)),
                      () -> assertEquals(Constraint.of(12),
                                         ms[5].getConstraint(3, 4)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(3, 5)),
                      () -> assertEquals(Constraint.of(-2),
                                         ms[5].getConstraint(4, 0)),
                      () -> assertEquals(Constraint.of(4),
                                         ms[5].getConstraint(4, 1)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(4, 2)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(4, 3)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(4, 4)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(4, 5)),
                      () -> assertEquals(Constraint.of(8),
                                         ms[5].getConstraint(5, 0)),
                      () -> assertEquals(Constraint.of(2),
                                         ms[5].getConstraint(5, 1)),
                      () -> assertEquals(Constraint.of(12),
                                         ms[5].getConstraint(5, 2)),
                      () -> assertEquals(Constraint.of(-2),
                                         ms[5].getConstraint(5, 3)),
                      () -> assertEquals(Constraint.of(10),
                                         ms[5].getConstraint(5, 4)),
                      () -> assertEquals(Constraint.of(0),
                                         ms[5].getConstraint(5, 5)))
                      );

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], expectedFeasibility[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("closureTestData")
    void testClosure(OctagonDifferenceBoundedMatrix m,
                     boolean feasibility,
                     Stream<Executable> oracles) {
        assertAll(Stream.concat(Stream.of(() -> assertEquals(feasibility, m.computeClosure())),
                                oracles));
    }

    private static Stream<Arguments> strongClosureTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(1, 0, Constraint.of(2))
            .setConstraint(2, 3, Constraint.of(4))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(1, 0, Constraint.of(2))
            .setConstraint(2, 3, Constraint.of(4))
            .setConstraint(2, 0, Constraint.of(3))
            .setConstraint(1, 3, Constraint.of(3))
            .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(() -> assertFalse(m.isFeasible())),
                    (m) -> checkMatrixCondition((i, j) -> {
                            if (i == j) {
                                return () -> assertEquals(Constraint.of(0),
                                                          m.getConstraint(i, j));
                            } else {
                                return () -> assertEquals(Constraint.TOP(),
                                                          m.getConstraint(i, j));
                            }
                        }),
                    (m) -> Stream.of(() -> assertEquals(Constraint.of(3),
                                                        m.getConstraint(2, 0)),
                                     () -> assertEquals(Constraint.of(3),
                                                        m.getConstraint(1, 3))),
                    (m) -> Stream.of(() -> assertEquals(Constraint.of(3),
                                                        m.getConstraint(2, 0)),
                                     () -> assertEquals(Constraint.of(3),
                                                        m.getConstraint(1, 3))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("strongClosureTestData")
    void testStrongClosure(OctagonDifferenceBoundedMatrix m,
                           Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        m.computeClosure();
        m.computeStrongClosure();
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> tightenTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(1, 0, Constraint.of(2))
            .setConstraint(2, 3, Constraint.of(4))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(+1))
            .setConstraint(1, 0, Constraint.of(-1))
            .setConstraint(2, 3, Constraint.of(+3))
            .setConstraint(3, 2, Constraint.of(-3))
            .build(),
        };

        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles = (m) -> IntStream.range(0, N).boxed()
                  .flatMap(i -> Stream.of(() -> assertTrue(m.getConstraint(i, i ^ 1).bound().map(b -> b % 2 == 0).orElse(true)),
                                          () -> assertTrue(m.getConstraint(i ^ 1, i).bound().map(b -> b % 2 == 0).orElse(true))));

        return IntStream.range(0, ms.length)
            .mapToObj(i -> Arguments.arguments(ms[i], oracles));
    }

    @ParameterizedTest
    @MethodSource("tightenTestData")
    void testTighten(OctagonDifferenceBoundedMatrix m,
                     Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        m.tighten();
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> reducedClosureTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(-1))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 0, Constraint.of(5))
            .setConstraint(1, 2, Constraint.of(1))
            .setConstraint(2, 0, Constraint.of(3))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(-1))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 0, Constraint.of(0))
            .setConstraint(1, 2, Constraint.of(1))
            .setConstraint(2, 0, Constraint.of(3))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(+2))
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(1, 2, Constraint.of(-3))
            .setConstraint(2, 1, Constraint.of(+2))
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(6))
            .setConstraint(0, 2, Constraint.of(10))
            .setConstraint(0, 4, Constraint.of(2))
            .setConstraint(1, 3, Constraint.of(-4))
                .setConstraint(2, 0, Constraint.of(-4))
                .setConstraint(3, 1, Constraint.of(10))
                .setConstraint(3, 5, Constraint.of(2))
                .setConstraint(4, 2, Constraint.of(2))
                .setConstraint(5, 1, Constraint.of(2))
                .setConstraint(5, 4, Constraint.of(10))
                .build(),
        };
        boolean[] expectedFeasibility = new boolean[] {
            true,
            false,
            true,
            false,
            false,
            true,
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    return () -> assertEquals(Constraint.of(0), ms[0].getConstraint(s, t));
                } else {
                    return () -> assertEquals(Constraint.TOP(), ms[0].getConstraint(s, t));
                }
            }),
            checkMatrixCondition((s, t) -> {
                return () -> assertEquals(Constraint.BOT(), ms[1].getConstraint(s, t));
            }),
            Stream.of(() -> assertEquals(Constraint.of(0),
                ms[2].getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    ms[2].getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    ms[2].getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(0),
                    ms[2].getConstraint(0, 1)),
                () -> assertEquals(Constraint.TOP(),
                    ms[2].getConstraint(0, 2)),
                () -> assertEquals(Constraint.of(4),
                    ms[2].getConstraint(1, 0)),
                () -> assertEquals(Constraint.TOP(),
                    ms[2].getConstraint(1, 2)),
                () -> assertEquals(Constraint.of(3),
                    ms[2].getConstraint(2, 0)),
                () -> assertEquals(Constraint.of(2),
                    ms[2].getConstraint(2, 1))),
            Stream.of(() -> assertEquals(Constraint.BOT(),
                ms[3].getConstraint(0, 0)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(1, 1)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(2, 2)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(0, 1)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(0, 2)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(1, 0)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(1, 2)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(2, 0)),
                () -> assertEquals(Constraint.BOT(),
                    ms[3].getConstraint(2, 1))),
            Stream.of(),
            Stream.of(() -> assertEquals(Constraint.of(0),
                ms[5].getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(6),
                    ms[5].getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(4),
                    ms[5].getConstraint(0, 2)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(0, 3)),
                () -> assertEquals(Constraint.of(2),
                    ms[5].getConstraint(0, 4)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(0, 5)),
                () -> assertEquals(Constraint.of(6),
                    ms[5].getConstraint(1, 0)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(1, 1)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(1, 2)),
                () -> assertEquals(Constraint.of(-4),
                    ms[5].getConstraint(1, 3)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(1, 4)),
                () -> assertEquals(Constraint.of(-2),
                    ms[5].getConstraint(1, 5)),
                () -> assertEquals(Constraint.of(-4),
                    ms[5].getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(2, 1)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(-2),
                    ms[5].getConstraint(2, 3)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(2, 4)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(2, 5)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(3, 0)),
                () -> assertEquals(Constraint.of(4),
                    ms[5].getConstraint(3, 1)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(3, 2)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(3, 3)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(3, 4)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(3, 5)),
                () -> assertEquals(Constraint.of(-2),
                    ms[5].getConstraint(4, 0)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(4, 1)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(4, 2)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(4, 3)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(4, 4)),
                () -> assertEquals(Constraint.of(2),
                    ms[5].getConstraint(4, 5)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(5, 0)),
                () -> assertEquals(Constraint.of(2),
                    ms[5].getConstraint(5, 1)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(5, 2)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(5, 3)),
                () -> assertEquals(Constraint.TOP(),
                    ms[5].getConstraint(5, 4)),
                () -> assertEquals(Constraint.of(0),
                    ms[5].getConstraint(5, 5))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], expectedFeasibility[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("reducedClosureTestData")
    void testReducedClosure(OctagonDifferenceBoundedMatrix m,
        boolean expectedFeasibility,
        Stream<Executable> oracles) {
        assertAll(Stream.concat(Stream.of(() -> assertEquals(expectedFeasibility, m.computeReducedClosure())),
            oracles));

    }

    private static Stream<Arguments> canonicalClosureTestData() {
        int smallN = 4;
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
                .setConstraint(0, 1, Constraint.of(4))
                .setConstraint(2, 3, Constraint.of(8))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
                .setConstraint(0, 1, Constraint.of(2))
                .setConstraint(0, 2, Constraint.of(2))
                .setConstraint(3, 1, Constraint.of(2))
                .setConstraint(3, 2, Constraint.of(2))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
                .setConstraint(0, 1, Constraint.of(2))
                .setConstraint(0, 2, Constraint.of(2))
                .setConstraint(2, 3, Constraint.of(2))
                .setConstraint(3, 1, Constraint.of(2))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(4, true)
                .setConstraint(0, 1, Constraint.of(10))
                .setConstraint(1, 0, Constraint.of(-10))
                .setConstraint(0, 2, Constraint.of(3))
                .setConstraint(3, 1, Constraint.of(3))
                .setConstraint(0, 3, Constraint.of(7))
                .setConstraint(2, 1, Constraint.of(7))
                .setConstraint(2, 0, Constraint.of(4))
                .setConstraint(1, 3, Constraint.of(4))
                .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(() -> assertEquals(Constraint.of(4),
                m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(6),
                    m.getConstraint(0, 3)),
                () -> assertEquals(Constraint.of(8),
                    m.getConstraint(2, 3)),
                () -> assertEquals(Constraint.of(6),
                    m.getConstraint(2, 1))),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(2, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 2)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(3, 3))),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(2, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(3, 2)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(3, 3))),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(10),
                    m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(3),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.of(7),
                        m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.of(-10),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(-7),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(-3),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.of(-3),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(7),
                        m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(4),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.of(-7),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(3),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.of(-4),
                        m.getConstraint(3, 2)))
            );
        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("canonicalClosureTestData")
    void testCanonicalClosure(OctagonDifferenceBoundedMatrix m,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        boolean closure = m.canonicalize();
        assertAll(Stream.concat(Stream.of(() -> assertTrue(closure)),
            oracles.apply(m)));
    }

    private static Stream<Arguments> wideningTestData() {
        OctagonDifferenceBoundedMatrix[][] mn = new OctagonDifferenceBoundedMatrix[][] {
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, false),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, true),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                    .setConstraint(0, 1, Constraint.of(-2))
                    .setConstraint(0, 2, Constraint.of(0))
                    .setConstraint(3, 1, Constraint.of(0))
                    .setConstraint(1, 0, Constraint.of(4))
                    .setConstraint(1, 2, Constraint.of(2))
                    .setConstraint(3, 0, Constraint.of(2))
                    .setConstraint(2, 0, Constraint.of(3))
                    .setConstraint(1, 3, Constraint.of(3))
                    .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, false)
                    .setConstraint(0, 1, Constraint.of(-4))
                    .setConstraint(0, 2, Constraint.of(-1))
                    .setConstraint(3, 1, Constraint.of(-1))
                    .setConstraint(1, 0, Constraint.of(4))
                    .setConstraint(1, 2, Constraint.of(2))
                    .setConstraint(3, 0, Constraint.of(2))
                    .setConstraint(2, 0, Constraint.of(3))
                    .setConstraint(1, 3, Constraint.of(3))
                    .build(),
            },
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    return () -> assertEquals(Constraint.of(0), mn[0][0].getConstraint(s, t));
                } else {
                    return () -> assertEquals(Constraint.TOP(), mn[0][0].getConstraint(s, t));
                }
            }),
            checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    return () -> assertEquals(Constraint.of(0), mn[1][0].getConstraint(s, t));
                } else {
                    return () -> assertEquals(Constraint.TOP(), mn[1][0].getConstraint(s, t));
                }
            }),
            Stream.of(() -> assertEquals(Constraint.of(0),
                mn[2][0].getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(-2),
                    mn[2][0].getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(0, 2)),
                () -> assertEquals(Constraint.of(4),
                    mn[2][0].getConstraint(1, 0)),
                () -> assertEquals(Constraint.of(2),
                    mn[2][0].getConstraint(1, 2)),
                () -> assertEquals(Constraint.of(3),
                    mn[2][0].getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    mn[2][0].getConstraint(2, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(mn[i][0], mn[i][1], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("wideningTestData")
    void testWidening(OctagonDifferenceBoundedMatrix m,
        OctagonDifferenceBoundedMatrix n,
        Stream<Executable> oracles) {
        m.widenWith(n, Set.of());
        assertAll(oracles);
    }

    private static Stream<Arguments> wideningWithStepsTestData() {
        OctagonDifferenceBoundedMatrix[][] mn = new OctagonDifferenceBoundedMatrix[][] {
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, false),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrix(N, true),
                new OctagonDifferenceBoundedMatrix(N, true),
            },
            new OctagonDifferenceBoundedMatrix[] {
                new OctagonDifferenceBoundedMatrixBuilder(N, true)
                    .setConstraint(0, 1, Constraint.of(-2))
                    .setConstraint(0, 2, Constraint.of(0))
                    .setConstraint(3, 1, Constraint.of(0))
                    .setConstraint(1, 0, Constraint.of(4))
                    .setConstraint(1, 2, Constraint.of(2))
                    .setConstraint(3, 0, Constraint.of(2))
                    .setConstraint(2, 0, Constraint.of(3))
                    .setConstraint(1, 3, Constraint.of(3))
                    .build(),
                new OctagonDifferenceBoundedMatrixBuilder(N, false)
                    .setConstraint(0, 1, Constraint.of(-4))
                    .setConstraint(0, 2, Constraint.of(0))
                    .setConstraint(3, 1, Constraint.of(0))
                    .setConstraint(1, 0, Constraint.of(4))
                    .setConstraint(1, 2, Constraint.of(2))
                    .setConstraint(3, 0, Constraint.of(2))
                    .setConstraint(2, 0, Constraint.of(3))
                    .setConstraint(1, 3, Constraint.of(3))
                    .build(),
            },
        };

        List<Stream<Executable>> oracles = List.of(checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    return () -> assertEquals(Constraint.of(0), mn[0][0].getConstraint(s, t));
                } else {
                    return () -> assertEquals(Constraint.TOP(), mn[0][0].getConstraint(s, t));
                }
            }),
            checkMatrixCondition((s, t) -> {
                if (s.equals(t)) {
                    return () -> assertEquals(Constraint.of(0), mn[1][0].getConstraint(s, t));
                } else {
                    return () -> assertEquals(Constraint.TOP(), mn[1][0].getConstraint(s, t));
                }
            }),
            Stream.of(() -> assertEquals(Constraint.of(0),
                mn[2][0].getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(-2),
                    mn[2][0].getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(0),
                    mn[2][0].getConstraint(0, 2)),
                () -> assertEquals(Constraint.of(4),
                    mn[2][0].getConstraint(1, 0)),
                () -> assertEquals(Constraint.of(2),
                    mn[2][0].getConstraint(1, 2)),
                () -> assertEquals(Constraint.of(3),
                    mn[2][0].getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    mn[2][0].getConstraint(2, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(mn[i][0], mn[i][1], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("wideningWithStepsTestData")
    void testWideningWithSteps(OctagonDifferenceBoundedMatrix m,
        OctagonDifferenceBoundedMatrix n,
        Stream<Executable> oracles) {
        m.widenWith(n, Set.of(10));
        assertAll(oracles);
    }

    private static Stream<Arguments> forgetTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(0))
                .setConstraint(1, 0, Constraint.of(0))
                .setConstraint(2, 0, Constraint.of(0))
                .setConstraint(0, 2, Constraint.of(0))
                .build(),
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(0))
                .setConstraint(2, 1, Constraint.of(0))
                .setConstraint(2, 3, Constraint.of(-1))
                .build(),
        };

        IntStream.range(0, 6).forEach(i -> {
                IntStream.range(i, 6).forEach(j -> {
                        ms[3].setConstraint(i, j, Constraint.of(i - j));
                        ms[3].setConstraint(j, i, Constraint.of(j - i));
                    });
            });

        // [[0, -1, -2, -3, -4, -5, -6],
        //  [1,  0, -1, -2, -3, -4, -5],
        //  [2,  1,  0, -1, -2, -3, -4],
        //  [3,  2,  1,  0, -1, -2, -3],
        //  [4,  3,  2,  1,  0, -1, -2],
        //  [5,  4,  3,  2,  1,  0, -1],
        //  [6,  5,  4,  3,  2,  1,  0]]

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles = List.of(
            m -> Stream.<Executable>of(
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(2, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(-1),
                    m.getConstraint(0, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.of(3),
                    m.getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 1))),
            m -> Stream.<Executable>of(
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(-1),
                    m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(0, 2)),
                () -> assertEquals(Constraint.of(4),
                    m.getConstraint(1, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 1))),
            m -> Stream.<Executable>of(
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(0, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(0, 3)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(1, 0)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(1, 1)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 3)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(2, 0)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(2, 1)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(2, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 3)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(3, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(3, 1)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(3, 2)),
                () -> assertEquals(Constraint.of(0),
                    m.getConstraint(3, 3))),
            m -> IntStream.range(0, 6)
                .boxed()
                .flatMap(i -> IntStream.range(0, 6).boxed().flatMap(j -> {
                            if (i == j) {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.of(0),
                                    m.getConstraint(i, j)));
                            } else if (i == 4 || j == 4) {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.TOP(),
                                    m.getConstraint(i, j)),
                                    () -> assertEquals(Constraint.TOP(),
                                        m.getConstraint(j, i)));
                            } else {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.of(i - j),
                                    m.getConstraint(i, j)),
                                    () -> assertEquals(Constraint.of(j - i),
                                        m.getConstraint(j, i)));
                            }
                        })),
            m -> Stream.<Executable>of(
                () -> assertEquals(Constraint.of(-1),
                    m.getConstraint(1, 3)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 1)),
                () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 3))));

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.forgetConstraints(1),
                (m) -> m.forgetConstraints(2),
                (m) -> m.forgetConstraints(3),
                (m) -> m.forgetConstraints(4),
                (m) -> m.forgetConstraints(2));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("forgetTestData")
    void testForget(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        m.computeClosure();
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> simpleForgetTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-2))
                .setConstraint(1, 0, Constraint.of(+1))
                .setConstraint(1, 2, Constraint.of(-1))
                .setConstraint(2, 0, Constraint.of(+2))
                .setConstraint(2, 1, Constraint.of(+1))
                .build(),
            new OctagonDifferenceBoundedMatrix(100, true),
        };

        // m = [[0, -1, -2, -3, -4, -5, -6, ..., -99],
        //      [1,  0, -1, -2, -3, -4, -5, ..., -98],
        //      [2,  1,  0, -1, -2, -3, -4, ..., -97],
        //      [3,  2,  1,  0, -1, -2, -3, ..., -96],
        //      [4,  3,  2,  1,  0, -1, -2, ..., -95],
        //      [5,  4,  3,  2,  1,  0, -1, ..., -94],
        //      [6,  5,  4,  3,  2,  1,  0, ..., -93],
        //      ...                                  ]
        IntStream.range(0, 100).forEach(i -> {
                IntStream.range(i, 100).forEach(j -> {
                        ms[1].setConstraint(i, j, Constraint.of(i - j));
                        ms[1].setConstraint(j, i, Constraint.of(j - i));
                    });
            });

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.forgetConstraintsSimple(2),
                (m) -> m.forgetConstraintsSimple(4));

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(() -> assertEquals(Constraint.of(-1),
                m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(0, 2)),
                () -> assertEquals(Constraint.of(+1),
                    m.getConstraint(1, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(),
                    m.getConstraint(2, 1))),
                (m) -> IntStream.range(0, 100).boxed().flatMap(i -> IntStream.range(0, 100).boxed().flatMap(j -> {
                            if (i == j) {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.of(0),
                                    m.getConstraint(i, j)));
                            } else if (i == 4 || j == 4) {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.TOP(),
                                    m.getConstraint(i, j)),
                                    () -> assertEquals(Constraint.TOP(),
                                        m.getConstraint(j, i)));
                            } else {
                                return Stream.<Executable>of(() -> assertEquals(Constraint.of(i - j),
                                    m.getConstraint(i, j)),
                                    () -> assertEquals(Constraint.of(j - i),
                                        m.getConstraint(j, i)));
                            }
                        })));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("simpleForgetTestData")
    void testSimpleForget(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> projectionTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(2, 1, Constraint.of(3))
                .setConstraint(1, 2, Constraint.of(2))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(2, 1, Constraint.of(3))
                .setConstraint(1, 0, Constraint.of(2))
                .setConstraint(2, 0, Constraint.of(5))
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(2, 1, Constraint.of(3))
                .setConstraint(1, 0, Constraint.of(2))
                .setConstraint(2, 0, Constraint.of(5))
                .setConstraint(0, 1, Constraint.of(3))
                .setConstraint(0, 2, Constraint.of(-2))
                .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(() -> assertEquals(Interval32Box.TOP(),
                m.projectToInterval(0, 2)),
                () -> assertEquals(Interval32Box.TOP(),
                    m.projectToInterval(2, 0))),
                (m) -> Stream.of(() -> assertEquals(Interval32Box.of(-1, null),
                    m.projectToInterval(0, 1)),
                    () -> assertEquals(Interval32Box.of(-5, null),
                        m.projectToInterval(0, 2))),
                (m) -> Stream.of(() -> assertEquals(Interval32Box.of(-1, 1),
                    m.projectToInterval(0, 1)),
                    () -> assertEquals(Interval32Box.of(-5, -2),
                        m.projectToInterval(0, 2))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("projectionTestData")
    void testProjection(OctagonDifferenceBoundedMatrix m,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> makeInfeasibleTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, false),
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(-1))
                .setConstraint(0, 2, Constraint.of(-1))
                .setConstraint(1, 0, Constraint.of(4))
                .setConstraint(1, 2, Constraint.of(1))
                .setConstraint(2, 0, Constraint.of(3))
                .build(),
        };

        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracle = (m) -> {
                return checkMatrixCondition((s, t) -> {
                        return () -> assertEquals(Constraint.BOT(), m.getConstraint(s, t));
                    });
            };

        return IntStream.range(0, ms.length)
            .mapToObj(i -> Arguments.arguments(ms[i], oracle));
    }

    @ParameterizedTest
    @MethodSource("makeInfeasibleTestData")
    void testMakeInfeasible(OctagonDifferenceBoundedMatrix m,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        m.makeInfeasible();
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> addIncomingTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(+2))
                .setConstraint(2, 1, Constraint.of(+3))
                .setConstraint(2, 0, Constraint.of(+3))
                .setConstraint(0, 2, Constraint.of(-3))
                .build(),
        };

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.addIncoming(1, Constraint.of(3)),
                (m) -> m.addIncoming(1, Constraint.of(2)));

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                }),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(+0), m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.of(+2), m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(+5), m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(-3), m.getConstraint(0, 2))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("addIncomingTestData")
    void testAddIncoming(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> addOutgoingTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(+2))
                .setConstraint(2, 1, Constraint.of(+3))
                .setConstraint(2, 0, Constraint.of(+3))
                .setConstraint(0, 2, Constraint.of(-3))
                .build(),
        };

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.addOutgoing(1, Constraint.of(3)),
                (m) -> m.addOutgoing(1, Constraint.of(2)));

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                }),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(+0), m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.of(+4), m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(-3), m.getConstraint(0, 2))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("addOutgoingTestData")
    void testAddOutgoing(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> subIncomingTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(+2))
                .setConstraint(2, 1, Constraint.of(+3))
                .setConstraint(2, 0, Constraint.of(+3))
                .setConstraint(0, 2, Constraint.of(-3))
                .build(),
        };

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.subIncoming(1, Constraint.of(3)),
                (m) -> m.subIncoming(1, Constraint.of(2)));

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                }),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(+0), m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.of(+2), m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(+1), m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(-3), m.getConstraint(0, 2))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("subIncomingTestData")
    void testSubIncoming(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> subOutgoingTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrix(N, true),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(+2))
                .setConstraint(2, 1, Constraint.of(+3))
                .setConstraint(2, 0, Constraint.of(+3))
                .setConstraint(0, 2, Constraint.of(-3))
                .build(),
        };

        List<Consumer<OctagonDifferenceBoundedMatrix>> ops =
            List.of((m) -> m.subOutgoing(1, Constraint.of(3)),
                (m) -> m.subOutgoing(1, Constraint.of(2)));

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> checkMatrixCondition((s, t) -> {
                    if (s.equals(t)) {
                        return () -> assertEquals(Constraint.of(0), m.getConstraint(s, t));
                    } else {
                        return () -> assertEquals(Constraint.TOP(), m.getConstraint(s, t));
                    }
                }),
                (m) -> Stream.of(() -> assertEquals(Constraint.of(+0), m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.of(+0), m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(+3), m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(-3), m.getConstraint(0, 2))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], ops.get(i), oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("subOutgoingTestData")
    void testSubOutgoing(OctagonDifferenceBoundedMatrix m,
        Consumer<OctagonDifferenceBoundedMatrix> op,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        op.accept(m);
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> incrementalClosureTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(2))
                .setConstraint(0, 2, Constraint.of(4))
                .setConstraint(3, 1, Constraint.of(4))
                .close()
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(2))
                .setConstraint(3, 0, Constraint.of(2))
                .setConstraint(1, 4, Constraint.of(15))
                .setConstraint(5, 0, Constraint.of(15))
                .setConstraint(3, 4, Constraint.of(5))
                .setConstraint(5, 2, Constraint.of(5))
                .close()
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 4, Constraint.of(3))
                .setConstraint(1, 0, Constraint.of(6))
                .setConstraint(1, 2, Constraint.of(2))
                .setConstraint(1, 3, Constraint.of(4))
                .setConstraint(1, 4, Constraint.of(8))
                .setConstraint(2, 0, Constraint.of(4))
                .setConstraint(2, 3, Constraint.of(2))
                .setConstraint(2, 4, Constraint.of(6))
                .setConstraint(3, 0, Constraint.of(2))
                .setConstraint(5, 0, Constraint.of(8))
                .setConstraint(5, 1, Constraint.of(3))
                .setConstraint(5, 3, Constraint.of(6))
                .close()
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(1, 2, Constraint.of(+2))
                .setConstraint(3, 0, Constraint.of(+2))
                .setConstraint(1, 3, Constraint.of(-1))
                .setConstraint(2, 0, Constraint.of(-1))
                .setConstraint(2, 3, Constraint.of(-4))
                .setConstraint(3, 2, Constraint.of(-4))
                .close()
                .build(),
            new OctagonDifferenceBoundedMatrixBuilder(N, true)
                .setConstraint(0, 1, Constraint.of(4))
                .setConstraint(2, 3, Constraint.of(6))
                .setConstraint(0, 3, Constraint.of(5))
                .setConstraint(2, 1, Constraint.of(5))
                .close()
                .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of(
                (m) -> Stream.of(() -> assertTrue(m.putIncremental(1, 2, Constraint.of(2))),
                    () -> assertTrue(m.putIncremental(3, 0, Constraint.of(2))),
                    () -> assertEquals(Constraint.of(4),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(4),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.of(6),
                        m.getConstraint(3, 2))),
                (m) -> Stream.of(() -> assertTrue(m.putIncremental(2, 3, Constraint.of(4))),
                    () -> assertEquals(Constraint.of(4),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.of(8),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(6),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.of(6),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(9),
                        m.getConstraint(2, 4)),
                    () -> assertEquals(Constraint.of(9),
                        m.getConstraint(5, 3)),
                    () -> assertEquals(Constraint.of(11),
                        m.getConstraint(1, 4)),
                    () -> assertEquals(Constraint.of(11),
                        m.getConstraint(5, 0)),
                    () -> assertEquals(Constraint.of(14),
                        m.getConstraint(5, 4))),
                (m) -> Stream.of(() -> assertTrue(m.putIncremental(2, 0, Constraint.of(2))),
                    () -> assertTrue(m.putConstraint(1, 3, Constraint.of(2))),
                    () -> assertEquals(Constraint.of(3),
                        m.getConstraint(0, 4)),
                    () -> assertEquals(Constraint.of(4),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.of(7),
                        m.getConstraint(1, 4)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.of(5),
                        m.getConstraint(2, 4)),
                    () -> assertEquals(Constraint.of(10),
                        m.getConstraint(5, 4))),
                (m) -> Stream.of(() -> assertFalse(m.putIncremental(3, 2, Constraint.of(2)))),
                (m) -> Stream.of(() -> assertTrue(m.putIncremental(0, 1, Constraint.of(2))),
                    () -> assertEquals(Constraint.of(2), m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(6), m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.of(4), m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.of(4), m.getConstraint(2, 1))));

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("incrementalClosureTestData")
    void testIncrementalClosure(OctagonDifferenceBoundedMatrix m,
                                Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        assertAll(oracles.apply(m));
    }

    @Test
    void testMultipleIncrementalClosure() {
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrixBuilder(6, true)
            .setConstraint(2, 3, Constraint.of(4))
            .setConstraint(3, 2, Constraint.of(-4))
            .setConstraint(4, 5, Constraint.of(6))
            .setConstraint(5, 4, Constraint.of(-6))
            .close()
            .build();
        assertAll(
            () -> assertTrue(m.incrementalClosure(
                List.of(
                    ConstraintUpdateThunk.of(0, 1, Constraint.of(+12)),
                    ConstraintUpdateThunk.of(1, 0, Constraint.of(-12)),
                    ConstraintUpdateThunk.of(0, 4, Constraint.of(+3)),
                    ConstraintUpdateThunk.of(4, 0, Constraint.of(-3)),
                    ConstraintUpdateThunk.of(0, 2, Constraint.of(+4)),
                    ConstraintUpdateThunk.of(2, 0, Constraint.of(-4))))),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(0, 0)),
            () -> assertEquals(Constraint.of(+12), m.getConstraint(0, 1)),
            () -> assertEquals(Constraint.of(+4), m.getConstraint(0, 2)),
            () -> assertEquals(Constraint.of(+8), m.getConstraint(0, 3)),
            () -> assertEquals(Constraint.of(+3), m.getConstraint(0, 4)),
            () -> assertEquals(Constraint.of(+9), m.getConstraint(0, 5)),
            () -> assertEquals(Constraint.of(-12), m.getConstraint(1, 0)),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(1, 1)),
            () -> assertEquals(Constraint.of(-8), m.getConstraint(1, 2)),
            () -> assertEquals(Constraint.of(-4), m.getConstraint(1, 3)),
            () -> assertEquals(Constraint.of(-9), m.getConstraint(1, 4)),
            () -> assertEquals(Constraint.of(-3), m.getConstraint(1, 5)),
            () -> assertEquals(Constraint.of(-4), m.getConstraint(2, 0)),
            () -> assertEquals(Constraint.of(+8), m.getConstraint(2, 1)),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(2, 2)),
            () -> assertEquals(Constraint.of(+4), m.getConstraint(2, 3)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(2, 4)),
            () -> assertEquals(Constraint.of(+5), m.getConstraint(2, 5)),
            () -> assertEquals(Constraint.of(-8), m.getConstraint(3, 0)),
            () -> assertEquals(Constraint.of(+4), m.getConstraint(3, 1)),
            () -> assertEquals(Constraint.of(-4), m.getConstraint(3, 2)),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(3, 3)),
            () -> assertEquals(Constraint.of(-5), m.getConstraint(3, 4)),
            () -> assertEquals(Constraint.of(+1), m.getConstraint(3, 5)),
            () -> assertEquals(Constraint.of(-3), m.getConstraint(4, 0)),
            () -> assertEquals(Constraint.of(+9), m.getConstraint(4, 1)),
            () -> assertEquals(Constraint.of(+1), m.getConstraint(4, 2)),
            () -> assertEquals(Constraint.of(+5), m.getConstraint(4, 3)),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(4, 4)),
            () -> assertEquals(Constraint.of(+6), m.getConstraint(4, 5)),
            () -> assertEquals(Constraint.of(-9), m.getConstraint(5, 0)),
            () -> assertEquals(Constraint.of(+3), m.getConstraint(5, 1)),
            () -> assertEquals(Constraint.of(-5), m.getConstraint(5, 2)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(5, 3)),
            () -> assertEquals(Constraint.of(-6), m.getConstraint(5, 4)),
            () -> assertEquals(Constraint.of(+0), m.getConstraint(5, 5)));
    }

    private static Stream<Arguments> incrementalCanonicalizationTestData() {
        int smallN = 4;
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
            .setConstraint(0, 1, Constraint.of(4))
            .setConstraint(2, 3, Constraint.of(8))
            .close()
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
            .setConstraint(0, 1, Constraint.of(2))
            .setConstraint(0, 2, Constraint.of(2))
            .setConstraint(3, 1, Constraint.of(2))
            .setConstraint(3, 2, Constraint.of(2))
            .close()
            .build(),
            new OctagonDifferenceBoundedMatrixBuilder(smallN, true)
            .setConstraint(0, 1, Constraint.of(2))
            .setConstraint(0, 2, Constraint.of(2))
            .setConstraint(2, 3, Constraint.of(2))
            .setConstraint(3, 1, Constraint.of(2))
            .close()
            .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(() -> assertTrue(m.incrementalClosure(0, 1, Constraint.of(5))),
                () -> assertTrue(m.incrementalClosure(1, 0, Constraint.of(5))),
                () -> assertEquals(Constraint.of(4),
                    m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.of(6),
                    m.getConstraint(0, 3)),
                () -> assertEquals(Constraint.of(8),
                    m.getConstraint(2, 3)),
                () -> assertEquals(Constraint.of(6),
                    m.getConstraint(2, 1))),
                (m) -> Stream.of(() -> assertTrue(m.incrementalClosure(0, 1, Constraint.of(2))),
                    () -> assertTrue(m.incrementalZClosure(1, 0, Constraint.of(2))),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(0, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(2, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 2)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(3, 3))),
                (m) -> Stream.of(() -> assertTrue(m.incrementalClosure(3, 1, Constraint.of(3))),
                    () -> assertTrue(m.incrementalClosure(2, 1, Constraint.of(3))),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(0, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 1)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(0, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 0)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(1, 1)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 2)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(1, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(2, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 1)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(2, 2)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(2, 3)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(3, 0)),
                    () -> assertEquals(Constraint.of(2),
                        m.getConstraint(3, 1)),
                    () -> assertEquals(Constraint.TOP(),
                        m.getConstraint(3, 2)),
                    () -> assertEquals(Constraint.of(0),
                        m.getConstraint(3, 3)))
            );

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("incrementalCanonicalizationTestData")
    void testIncrementalTightClosure(OctagonDifferenceBoundedMatrix m,
                                     Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
        assertAll(oracles.apply(m));
    }

    private static Stream<Arguments> w0zReductionTestData() {
        OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
            new OctagonDifferenceBoundedMatrixBuilder(6, true)
                .setConstraint(0, 1, Constraint.of(0))
                .setConstraint(2, 3, Constraint.of(0))
                .setConstraint(3, 2, Constraint.of(0))
                .setConstraint(4, 5, Constraint.of(4))
                .close()
                .build(),
        };

        List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
            List.of((m) -> Stream.of(
                () -> assertEquals(Constraint.of(0), m.getConstraint(0, 0)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(0, 1)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 2)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 3)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 4)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 5)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 0)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(1, 1)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 2)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 3)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 4)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 5)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 0)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 1)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(2, 2)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(2, 3)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 4)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 5)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 0)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 1)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(3, 2)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(3, 3)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 4)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 5)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 0)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 1)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 2)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 3)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(4, 4)),
                () -> assertEquals(Constraint.of(4), m.getConstraint(4, 5)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 0)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 1)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 2)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 3)),
                () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 4)),
                () -> assertEquals(Constraint.of(0), m.getConstraint(5, 5)))
            );

        return IntStream.range(0, oracles.size())
            .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    }

    @ParameterizedTest
    @MethodSource("w0zReductionTestData")
    void testw0zReduction(OctagonDifferenceBoundedMatrix m,
        Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {

        m.w0zReduction();
        assertAll(oracles.apply(m));
    }

    // private static Stream<Arguments> constantsCacheTestData() {

    //     OctagonDifferenceBoundedMatrix[] ms = new OctagonDifferenceBoundedMatrix[] {
    //         new OctagonDifferenceBoundedMatrix(N, true),
    //         new OctagonDifferenceBoundedMatrixBuilder(N, true)
    //         .setConstraint(0, 1, Constraint.of(2))
    //         .build(),
    //         new OctagonDifferenceBoundedMatrixBuilder(N, true)
    //         .setConstraint(0, 1, Constraint.of(+2))
    //         .setConstraint(1, 0, Constraint.of(-2))
    //         .setConstraint(1, 2, Constraint.of(+3))
    //         .build(),
    //         new OctagonDifferenceBoundedMatrixBuilder(N, true)
    //         .setConstraint(0, 1, Constraint.of(+2))
    //         .setConstraint(1, 0, Constraint.of(+3))
    //         .build(),
    //     };

    //     List<Function<OctagonDifferenceBoundedMatrix, Stream<Executable>>> oracles =
    //         List.of((m) -> Stream.of(() -> assertTrue(m.getConstants().isEmpty())),
    //                 (m) -> Stream.of(() -> assertTrue(m.getConstants().isEmpty())),
    //                 (m) -> Stream.of(() -> assertFalse(m.getConstants().isEmpty()),
    //                                  () -> assertTrue(m.getConstants().contains(xs[0])),
    //                                  () -> assertTrue(m.getConstants().contains(xs[1]))),
    //                 (m) -> Stream.of(() -> assertTrue(m.getConstants().isEmpty())));

    //     return IntStream.range(0, oracles.size())
    //         .mapToObj(i -> Arguments.arguments(ms[i], oracles.get(i)));
    // }

    // @ParameterizedTest
    // @MethodSource("constantsCacheTestData")
    // void testConstantsCache(OctagonDifferenceBoundedMatrix m,
    //                         Function<OctagonDifferenceBoundedMatrix, Stream<Executable>> oracles) {
    //     m.checkConstants();
    //     assertAll(oracles.apply(m));
    // }

    @Test
    void testIncrementalClosureInstances() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(4, true)
            .setConstraint(0, 1, Constraint.of(4))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(2, 3, Constraint.of(6))
            .setConstraint(3, 2, Constraint.of(-4))
            .close()
            .build();

        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dba);
        OctagonDifferenceBoundedMatrix n = new OctagonDifferenceBoundedMatrix(dba);

        if ((m.deferredIncrementalClosure(0, 2, Constraint.of(-2)) &&
             m.deferredIncrementalClosure(3, 1, Constraint.of(-2))) &&
            n.incrementalZClosure(0, 2, Constraint.of(-2))) {
            assertEquals(n, m);
        }
    }

    @Test
    void testDeferredIncrementalClosureInstances() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(8, true)
            .setConstraint(0, 1, Constraint.of(0))
            .setConstraint(1, 0, Constraint.of(0))
            .setConstraint(4, 0, Constraint.of(2))
            .setConstraint(1, 5, Constraint.of(2))
            .setConstraint(4, 5, Constraint.of(4))
            .setConstraint(6, 7, Constraint.of(4))
            .close()
            .build();
        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dba);
        var thunks = List.of(
            ConstraintUpdateThunk.of(2, 0, Constraint.of(0)),
            ConstraintUpdateThunk.of(1, 3, Constraint.of(0)),
            ConstraintUpdateThunk.of(2, 3, Constraint.of(0)));
        assertAll(
            () -> assertTrue(m.deferredIncrementalClosure(thunks)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 0)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 4)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(0, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 6)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(0, 7)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(1, 0)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(1, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(1, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 4)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(1, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 6)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(1, 7)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 0)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 1)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 4)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(2, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 6)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(2, 7)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(3, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 4)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 6)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 7)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(4, 0)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(4, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 2)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(4, 3)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(4, 4)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(4, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 6)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(4, 7)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 4)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(5, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 6)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 7)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(6, 0)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(6, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(6, 2)),
            () -> assertEquals(Constraint.of(2), m.getConstraint(6, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(6, 4)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(6, 5)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(6, 6)),
            () -> assertEquals(Constraint.of(4), m.getConstraint(6, 7)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 4)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(7, 6)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(7, 7)));
    }

    @Test
    void testDeferredIncrementalClosureProducesBottom() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(4, true)
            .setConstraint(0, 1, Constraint.of(+2))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(2, 3, Constraint.of(+4))
            .setConstraint(3, 2, Constraint.of(-4))
            .close()
            .build();

        var thunks = List.of(
            ConstraintUpdateThunk.of(0, 3, Constraint.of(2)),
            ConstraintUpdateThunk.of(2, 1, Constraint.of(2)));
        assertFalse(dba.deferredIncrementalClosure(thunks));
    }

    @Test
    void testDeferredClosureInstances() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(6, true)
            .setConstraint(0, 2, Constraint.of(-1))
            .setConstraint(3, 1, Constraint.of(-1))
            .close()
            .build();

        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dba);
        var thunks = List.of(
            ConstraintUpdateThunk.of(4, 2, Constraint.of(0)),
            ConstraintUpdateThunk.of(2, 4, Constraint.of(0)),
            ConstraintUpdateThunk.of(3, 5, Constraint.of(0)),
            ConstraintUpdateThunk.of(5, 3, Constraint.of(0)));
        assertAll(
            () -> assertTrue(m.deferredIncrementalClosure(thunks, dba)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(0, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 1)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(0, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 3)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(0, 4), "AAAHHHH"),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(0, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 0)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(1, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 4)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(1, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 1)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 3)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(2, 4), "coherence"),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(2, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 0)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(3, 1)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(3, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(3, 4)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(3, 5), "Coherence"),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 0)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 1)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(4, 2)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 3)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(4, 4)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(4, 5)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 0)),
            () -> assertEquals(Constraint.of(-1), m.getConstraint(5, 1), "AAAHHHAAHH"),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 2)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(5, 3)),
            () -> assertEquals(Constraint.TOP(), m.getConstraint(5, 4)),
            () -> assertEquals(Constraint.of(0), m.getConstraint(5, 5)));
    }

    @Test
    void testDeferredClosureInstancesCE() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(10, true)
            .setConstraint(0, 2, Constraint.of(1))
            .setConstraint(3, 1, Constraint.of(1))
            .setConstraint(4, 6, Constraint.of(2))
            .setConstraint(7, 5, Constraint.of(2))
            .close()
            .build();

        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dba);
        var thunks = List.of(
            ConstraintUpdateThunk.of(2, 4, Constraint.of(5)),
            ConstraintUpdateThunk.of(5, 3, Constraint.of(5)),
            ConstraintUpdateThunk.of(6, 8, Constraint.of(7)),
            ConstraintUpdateThunk.of(9, 7, Constraint.of(7))
        );

        assertAll(
            () -> assertTrue(m.incrementalClosure(thunks, dba)),
            () -> assertEquals(Constraint.of(15), m.getConstraint(0, 8)),
            () -> assertEquals(Constraint.of(15), m.getConstraint(9, 1)));
    }

    @Test
    void testRelationalVariablePromotion() {
        OctagonDifferenceBoundedMatrix dba = new OctagonDifferenceBoundedMatrixBuilder(N, true)
            .setConstraint(0, 1, Constraint.of(2))
            .setConstraint(1, 0, Constraint.of(-2))
            .setConstraint(4, 5, Constraint.of(6))
            .setConstraint(5, 4, Constraint.of(6))
            .setConstraint(2, 4, Constraint.of(3))
            .setConstraint(5, 3, Constraint.of(3))
            .build();

        OctagonDifferenceBoundedMatrix m = new OctagonDifferenceBoundedMatrix(dba);
        var thunks = List.of(
            ConstraintUpdateThunk.of(4, 5, Constraint.of(8)),
            ConstraintUpdateThunk.of(5, 4, Constraint.of(-8))
        );

        m.forgetConstraints(4);
        m.forgetConstraints(5);

        assertAll(
            () -> assertTrue(m.incrementalClosure(thunks, dba)),
            () -> assertEquals(Constraint.of(5), m.getConstraint(2, 0)),
            () -> assertEquals(Constraint.of(5), m.getConstraint(1, 3))
        );
    }
}
