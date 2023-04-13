package processing.smt;

import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import soot.Local;
import soot.IntType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.grimp.Grimp;
import processing.Locals;

public class SmtExpressionFixpointTest {

    private static final Grimp g = Grimp.v();

    private static BinaryOperator<Value> and = (a, b) -> g.newAndExpr(a, b);

    @ParameterizedTest
    @MethodSource("smtConnectedFixpointProvider")
    void testConnectedFixpointUnion(String left,
                                    String right,
                                    Set<Local> changedVariables,
                                    Set<Local> expectedVariables,
                                    Optional<Value> leftProjection,
                                    Optional<Value> rightProjection) {
        SmtExpression leftExpr = SmtExpressionReader.parse(left);
        SmtExpression rightExpr = SmtExpressionReader.parse(right);
        Set<Local> projectedVariables = SmtExpression.connectedUnion(changedVariables, leftExpr, rightExpr);
        assertAll(() -> assertEquals(expectedVariables, projectedVariables),
                  () -> assertEquals(leftProjection.map(expr -> expr.toString()),
                                     leftExpr.getConnectedValue(projectedVariables).map(expr -> expr.toString())),
                  () -> assertEquals(rightProjection.map(expr -> expr.toString()),
                                     rightExpr.getConnectedValue(projectedVariables).map(expr -> expr.toString())));
    }

    @ParameterizedTest
    @MethodSource("smtReachableFixpointProvider")
    void testReachableFixpointUnion(String left,
                                    String right,
                                    Set<Local> changedVariables,
                                    Set<Local> expectedVariables,
                                    Optional<Value> leftProjection,
                                    Optional<Value> rightProjection) {
        SmtExpression leftExpr = SmtExpressionReader.parse(left);
        SmtExpression rightExpr = SmtExpressionReader.parse(right);
        Set<Local> projectedVariables = SmtExpression.reachableUnion(changedVariables, leftExpr, rightExpr);
        assertAll(() -> assertEquals(expectedVariables, projectedVariables),
                  () -> assertEquals(leftProjection.map(expr -> expr.toString()),
                                     leftExpr.getValue(projectedVariables).map(expr -> expr.toString())),
                  () -> assertEquals(rightProjection.map(expr -> expr.toString()),
                                     rightExpr.getValue(projectedVariables).map(expr -> expr.toString())));
    }

    private static Stream<Arguments> smtConnectedFixpointProvider() {
        return Stream.of(Stream.of(Arguments.arguments("(<= x (+ y 3))",
                                                       "(and (<= k (+ y 0)) (<= x (+ y 3)))",
                                                       Set.of(Locals.get("x")),
                                                       Locals.get("x", "y"),
                                                       Stream.of(g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("k"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments("(and (<= x (+ y 3)))",
                                                       "(and (<= k (+ y 0)) (<= x (+ y 3)))",
                                                       Locals.get("x", "y"),
                                                       Locals.get("k", "x", "y"),
                                                       Stream.of(g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value) e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("k"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Locals.get("a", "b"),
                                                       Locals.get("a", "b", "c", "d"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3))",
                                                                 "(<= f (+ d 4)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(3))),
                                                                 g.newLeExpr(Locals.get("f"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ e 2)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and)))
                         ).flatMap(s -> s.map(v -> v));
    }

    private static Stream<Arguments> smtReachableFixpointProvider() {
        return Stream.of(// 1
                         Stream.of(Arguments.arguments("(<= x (+ y 3))",
                                                       "(and (<= k (+ y 0)) (<= x (+ y 3)))",
                                                       Set.of(Locals.get("x")),
                                                       Locals.get("x", "y"),
                                                       Stream.of(g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 2
                         Stream.of(Arguments.arguments("(and (<= x (+ y 3)))",
                                                       "(and (<= k (+ y 0)) (<= x (+ y 3)))",
                                                       Locals.get("x", "y"),
                                                       Locals.get("k", "x", "y"),
                                                       Stream.of(g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value) e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("k"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"), IntConstant.v(3))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 3
                         Stream.of(Arguments.arguments(Stream.of("(and (<= b (+ a 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= e (+ d 2))",
                                                                 "(<= d (+ g 3))",
                                                                 "(<= f (+ d 4)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ e 0))",
                                                                 "(<= b (+ a 1))",
                                                                 "(<= c (+ b 2))",
                                                                 "(<= d (+ g 3))",
                                                                 "(<= g (+ f 4)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("b")),
                                                       Locals.get("a", "b", "c", "d", "e", "f", "g"),
                                                       Stream.of(g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("a"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("g"),
                                                                                          IntConstant.v(3))),
                                                                 g.newLeExpr(Locals.get("e"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("f"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value) e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("a"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("c"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("g"),
                                                                                          IntConstant.v(3))),
                                                                 g.newLeExpr(Locals.get("g"),
                                                                             g.newAddExpr(Locals.get("f"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 4
                         Stream.of(Arguments.arguments(Stream.of("(and (<= b (+ a 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= e (+ d 2))",
                                                                 "(<= d (+ g 3))",
                                                                 "(<= f (+ d 4)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ e 0))",
                                                                 "(<= b (+ a 1))",
                                                                 "(<= c (+ b 2))",
                                                                 "(<= d (+ g 3))",
                                                                 "(<= f (+ g 4)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("b")),
                                                       Locals.get("a", "b", "c", "d", "e", "g", "f"),
                                                       Stream.of(g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("a"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("g"),
                                                                                          IntConstant.v(3))),
                                                                 g.newLeExpr(Locals.get("e"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("f"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value) e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("a"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("c"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("g"),
                                                                                          IntConstant.v(3))),
                                                                 g.newLeExpr(Locals.get("f"),
                                                                             g.newAddExpr(Locals.get("g"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 5
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 6
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Locals.get("a", "b"),
                                                       Locals.get("a", "b", "c", "d"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 7
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3))",
                                                                 "(<= f (+ d 4)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 8
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ b 2))",
                                                                 "(<= d (+ e 3)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ e 2)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 9
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= d (+ c 1))",
                                                                 "(<= f (+ d 2)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b (+ c 1))",
                                                                 "(<= d (+ e 2)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("a")),
                                                       Locals.get("a", "b", "c", "d", "e", "f"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("f"),
                                                                             g.newAddExpr(Locals.get("d"),
                                                                                          IntConstant.v(2))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             g.newAddExpr(Locals.get("c"),
                                                                                          IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("d"),
                                                                             g.newAddExpr(Locals.get("e"),
                                                                                          IntConstant.v(2))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 10
                         Stream.of(Arguments.arguments(Stream.of("(and (<= a (+ b 0))",
                                                                 "(<= b 3)",
                                                                 "(>= b 0))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (<= a (+ b 2))",
                                                                 "(<= b 4)",
                                                                 "(>= b (- 1)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("b")),
                                                       Locals.get("a", "b"),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(0))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             IntConstant.v(3)),
                                                                 g.newGeExpr(Locals.get("b"),
                                                                             IntConstant.v(0)))
                                                       .map(e -> (Value)e)
                                                       .reduce(and),
                                                       Stream.of(g.newLeExpr(Locals.get("a"),
                                                                             g.newAddExpr(Locals.get("b"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("b"),
                                                                             IntConstant.v(4)),
                                                                 g.newGeExpr(Locals.get("b"),
                                                                             g.newNegExpr(IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         // 11 - base64_17:147
                         Stream.of(Arguments.arguments(Stream.of("(and (= $i1 0)",
                                                                 "(>= i56 0)",
                                                                 "(= $b19 (+ b78 0))",
                                                                 "(= $b20 (+ b80 0))",
                                                                 "(= $b21 (+ b82 0))",
                                                                 "(= $b22 (+ b83 0))",
                                                                 "(= $b5 (+ b60 0))",
                                                                 "(= $b6 (+ b62 0))",
                                                                 "(= $i3 (+ $i4 0))",
                                                                 "(= $i3 (+ $i76 0))",
                                                                 "(= $i3 (+ i2 (- 1)))",
                                                                 "(<= i56 (+ $i3 (- 1)))",
                                                                 "(<= i56 (+ i74 0))",
                                                                 "(= i57 (+ i59 (- 1)))",
                                                                 "(= i57 (+ i61 (- 2)))",
                                                                 "(= i57 (+ i65 (- 3)))",
                                                                 "(<= i74 (+ i75 (- 4)))",
                                                                 "(= i74 (+ i88 (- 1)))",
                                                                 "(= i75 (+ i77 3))",
                                                                 "(= i75 (+ i79 2))",
                                                                 "(= i75 (+ i81 1)))").collect(Collectors.joining(" ")),
                                                       Stream.of("(and (= $i1 0)",
                                                                 "(>= i56 0)",
                                                                 "(= $b19 (+ b78 0))",
                                                                 "(= $b20 (+ b80 0))",
                                                                 "(= $b21 (+ b82 0))",
                                                                 "(= $b22 (+ b83 0))",
                                                                 "(= $b5 (+ b60 0))",
                                                                 "(= $b6 (+ b62 0))",
                                                                 "(= $i3 (+ $i4 0))",
                                                                 "(= $i3 (+ $i76 0))",
                                                                 "(= $i3 (+ i2 (- 1)))",
                                                                 "(<= i56 (+ $i3 (- 1)))",
                                                                 "(<= i56 (+ i74 0))",
                                                                 "(= i57 (+ i59 (- 1)))",
                                                                 "(= i57 (+ i61 (- 2)))",
                                                                 "(= i57 (+ i65 (- 3)))",
                                                                 "(<= i74 (+ i75 (- 4)))",
                                                                 "(= i74 (+ i88 (- 1)))",
                                                                 "(= i75 (+ i77 3))",
                                                                 "(= i75 (+ i79 2))",
                                                                 "(= i75 (+ i81 1)))").collect(Collectors.joining(" ")),
                                                       Locals.get("i74", "i88"),
                                                       Locals.get("i56", "i74", "i75", "i77", "i79", "i81", "i88"),
                                                       Stream.of(newLeExpr("i56", "i74", 0),
                                                                 newGeExpr("i56", 0),
                                                                 newLeExpr("i74", "i75", -4),
                                                                 newEqExpr("i74", "i88", -1),
                                                                 newEqExpr("i75", "i77", 3),
                                                                 newEqExpr("i75" , "i79", 2),
                                                                 newEqExpr("i75", "i81", 1))
                                                       .reduce(and),
                                                       Stream.of(newLeExpr("i56", "i74", 0),
                                                                 newGeExpr("i56", 0),
                                                                 newLeExpr("i74", "i75", -4),
                                                                 newEqExpr("i74", "i88", -1),
                                                                 newEqExpr("i75", "i77", 3),
                                                                 newEqExpr("i75" , "i79", 2),
                                                                 newEqExpr("i75", "i81", 1))
                                                       .reduce(and)))
                         ).flatMap(s -> s.map(v -> v));
    }

    private static Value newIntValue(int value) {
        if (value >= 0) {
            return IntConstant.v(value);
        } else {
            return g.newNegExpr(IntConstant.v(value * -1));
        }
    }

    private static Value newEqExpr(String x, int value) {
        return g.newEqExpr(Locals.get(x), newIntValue(value));
    }

    private static Value newEqExpr(String x, String y, int value) {
        return g.newEqExpr(Locals.get(x), g.newAddExpr(Locals.get(y), newIntValue(value)));
    }

    private static Value newLeExpr(String x, int value) {
        return g.newLeExpr(Locals.get(x), newIntValue(value));
    }

    private static Value newLeExpr(String x, String y, int value) {
        return g.newLeExpr(Locals.get(x), g.newAddExpr(Locals.get(y), newIntValue(value)));
    }

    private static Value newGeExpr(String x, int value) {
        return g.newGeExpr(Locals.get(x), newIntValue(value));
    }

    private static Value newGeExpr(String x, String y, int value) {
        return g.newGeExpr(Locals.get(x), g.newAddExpr(Locals.get(y), newIntValue(value)));
    }
}
