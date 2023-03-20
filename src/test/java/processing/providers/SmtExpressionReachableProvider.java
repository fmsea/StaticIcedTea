package processing.providers;

import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;
import soot.grimp.Grimp;

import processing.Locals;

public class SmtExpressionReachableProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        Grimp g = Grimp.v();

        BinaryOperator<Value> or = (a, b) -> g.newOrExpr(a, b);
        BinaryOperator<Value> and = (a, b) -> g.newAndExpr(a, b);


        return Stream.of(Stream.of(Arguments.arguments("(= i0 0)",
                                                       Set.of(Locals.get("i0")),
                                                       Optional.of(g.newEqExpr(Locals.get("i0"),
                                                                               IntConstant.v(0))))),
                         Stream.of(Arguments.arguments("(= i0 (+ i1 0))",
                                                       Set.of(Locals.get("i1")),
                                                       Optional.of(g.newEqExpr(Locals.get("i0"),
                                                                               g.newAddExpr(Locals.get("i1"),
                                                                                            IntConstant.v(0)))))),
                         Stream.of(Arguments.arguments("(= i0 0)", Set.of(Locals.get("i1")), Optional.empty())),
                         Locals.values().stream().map(id -> Arguments.arguments("true", Set.of(id), Optional.empty())),
                         Locals.values().stream().map(id -> Arguments.arguments("false", Set.of(id), Optional.empty())),
                         Stream.of(Arguments.arguments("i0", Set.of(Locals.get("i0")), Optional.of(Locals.get("i0")))),
                         Stream.of(Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                                       Set.of(Locals.get("i0")),
                                                       Optional.of(g.newEqExpr(Locals.get("i0"),
                                                                               IntConstant.v(1))))),
                         Stream.of(Arguments.arguments("(and (<= i0 (+ i1 0)) (or (> i1 0) (< i1 0)))",
                                             Set.of(Locals.get("i0"), Locals.get("i1")),
                                             Optional.of(g.newAndExpr(g.newLeExpr(Locals.get("i0"),
                                                                                  g.newAddExpr(Locals.get("i1"),
                                                                                               IntConstant.v(0))),
                                                                      g.newOrExpr(g.newLtExpr(Locals.get("i1"),
                                                                                              IntConstant.v(0)),
                                                                                  g.newGtExpr(Locals.get("i1"),
                                                                                              IntConstant.v(0))))))),
                         Stream.of(Arguments.arguments("(and (<= i0 (+ i1 0)) (or (> i1 0) (< i1 0)))",
                                             Set.of(Locals.get("i1")),
                                             Optional.of(g.newOrExpr(g.newLtExpr(Locals.get("i1"),
                                                                                 IntConstant.v(0)),
                                                                     g.newGtExpr(Locals.get("i1"),
                                                                                 IntConstant.v(0)))))),
                         Locals.values().stream().map(id -> Arguments.arguments("(- 1)", Set.of(id), Optional.empty())),
                         Stream.of(Arguments.arguments("(- i0)",
                                                       Set.of(Locals.get("i0")),
                                                       Optional.of(g.newNegExpr(Locals.get("i0"))))),
                         Stream.of(Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                                       Set.of(Locals.get("$z0")),
                                                       Optional.of(g.newEqExpr(Locals.get("$z0"),
                                                                               IntConstant.v(0))))),
                         Stream.of(Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                                       Set.of(Locals.get("i3")),
                                                       Stream.of(g.newLeExpr(Locals.get("i3"),
                                                                             g.newAddExpr(Locals.get("i4"),
                                                                                          IntConstant.v(0))),
                                                                 g.newGeExpr(Locals.get("i3"),
                                                                             IntConstant.v(0)),
                                                                 g.newLeExpr(Locals.get("i4"),
                                                                             g.newAddExpr(Locals.get("i0"),
                                                                                          g.newNegExpr(IntConstant.v(1)))))
                                                       .map(e -> (Value) e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                                       Set.of(Locals.get("i4")),
                                                       Optional.of(g.newLeExpr(Locals.get("i4"),
                                                                               g.newAddExpr(Locals.get("i0"),
                                                                                            g.newNegExpr(IntConstant.v(1))))))),
                         Stream.of(Arguments.arguments("(and (or (<= i0 0) (> i0 1)) (or (<= i1 0) (> i1 2)) (<= i0 (+ i3 4)))",
                                                       Set.of(Locals.get("i0")),
                                                       Stream.of(g.newOrExpr(g.newLeExpr(Locals.get("i0"),
                                                                                         IntConstant.v(0)),
                                                                             g.newGtExpr(Locals.get("i0"),
                                                                                         IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("i0"),
                                                                             g.newAddExpr(Locals.get("i3"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments("(and (or (<= i0 0) (> i0 1)) (or (<= i1 0) (> i1 2)) (<= i0 (+ i3 4)) (>= i3 (- 4)))",
                                                       Set.of(Locals.get("i3")),
                                                       Stream.of(g.newGeExpr(Locals.get("i3"),
                                                                             g.newNegExpr(IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= w 2)",
                                                                 "(>= w 2)",
                                                                 "(<= x 2)",
                                                                 "(<= x (+ y (- 1)))",
                                                                 "(<= x (+ u 2))",
                                                                 "(<= y 3)",
                                                                 "(>= y 3)",
                                                                 "(<= y (+ u 3))",
                                                                 "(>= u 0)",
                                                                 ")").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("u")),
                                                       Optional.of(g.newGeExpr(Locals.get("u"), IntConstant.v(0))))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= w 2)",
                                                                 "(>= w 2)",
                                                                 "(<= x 2)",
                                                                 "(<= x (+ y (- 1)))",
                                                                 "(<= x (+ u 2))",
                                                                 "(<= y 3)",
                                                                 "(>= y 3)",
                                                                 "(<= y (+ u 3))",
                                                                 "(>= u 0)",
                                                                 ")").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("y")),
                                                       Stream.of(g.newGeExpr(Locals.get("u"), IntConstant.v(0)),
                                                                 g.newLeExpr(Locals.get("y"), IntConstant.v(3)),
                                                                 g.newLeExpr(Locals.get("y"),
                                                                             g.newAddExpr(Locals.get("u"),
                                                                                          IntConstant.v(3))),
                                                                 g.newGeExpr(Locals.get("y"), IntConstant.v(3)))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= w 2)",
                                                                 "(>= w 2)",
                                                                 "(<= x 2)",
                                                                 "(<= x (+ y (- 1)))",
                                                                 "(<= x (+ u 2))",
                                                                 "(<= y 3)",
                                                                 "(>= y 3)",
                                                                 "(<= y (+ u 3))",
                                                                 "(>= u 0)",
                                                                 ")").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("x")),
                                                       Stream.of(g.newGeExpr(Locals.get("u"), IntConstant.v(0)),
                                                                 g.newLeExpr(Locals.get("x"), IntConstant.v(2)),
                                                                 g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("u"),
                                                                                          IntConstant.v(2))),
                                                                 g.newLeExpr(Locals.get("x"),
                                                                             g.newAddExpr(Locals.get("y"),
                                                                                          g.newNegExpr(IntConstant.v(1)))),
                                                                 g.newLeExpr(Locals.get("y"), IntConstant.v(3)),
                                                                 g.newLeExpr(Locals.get("y"),
                                                                             g.newAddExpr(Locals.get("u"),
                                                                                          IntConstant.v(3))),
                                                                 g.newGeExpr(Locals.get("y"), IntConstant.v(3)))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments("(>= w (+ y 3))",
                                                       Set.of(Locals.get("w")),
                                                       Optional.empty())),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= w 2)",
                                                                 "(>= w 2)",
                                                                 "(>= y (- 1))",
                                                                 "(>= w (+ y 3)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("w")),
                                                       Stream.of(g.newLeExpr(Locals.get("w"), IntConstant.v(2)),
                                                                 g.newGeExpr(Locals.get("w"), IntConstant.v(2)))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments(Stream.of("(and (<= w 2)",
                                                                 "(>= w 2)",
                                                                 "(>= y (- 1))",
                                                                 "(<= k (+ y 3))",
                                                                 "(>= w (+ y 3)))").collect(Collectors.joining(" ")),
                                                       Set.of(Locals.get("w"), Locals.get("y")),
                                                       Stream.of(g.newLeExpr(Locals.get("w"), IntConstant.v(2)),
                                                                 g.newGeExpr(Locals.get("w"), IntConstant.v(2)),
                                                                 g.newGeExpr(Locals.get("w"),
                                                                             g.newAddExpr(Locals.get("y"),
                                                                                          IntConstant.v(3))),
                                                                 g.newGeExpr(Locals.get("y"),
                                                                             g.newNegExpr(IntConstant.v(1))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and)))
                         ).flatMap(s -> s.map(v -> v));
    }
}
