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

public class SmtExpressionConnectedProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        Grimp g = Grimp.v();

        BinaryOperator<Value> or = (a, b) -> g.newOrExpr(a, b);
        BinaryOperator<Value> and = (a, b) -> g.newAndExpr(a, b);


        return Stream.of(Stream.of(Arguments.arguments("(= i0 0)",
                                                       Locals.get("i0"),
                                                       Optional.of(g.newEqExpr(Locals.get("i0"),
                                                                               IntConstant.v(0))))),
                         Stream.of(Arguments.arguments("(= i0 0)", Locals.get("i1"), Optional.empty())),
                         Locals.values().stream().map(id -> Arguments.arguments("true", id, Optional.empty())),
                         Locals.values().stream().map(id -> Arguments.arguments("false", id, Optional.empty())),
                         Stream.of(Arguments.arguments("i0", Locals.get("i0"), Optional.of(Locals.get("i0")))),
                         Stream.of(Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                                       Locals.get("i0"),
                                                       Optional.of(g.newEqExpr(Locals.get("i0"),
                                                                               IntConstant.v(1))))),
                         Locals.values().stream().map(id -> Arguments.arguments("(- 1)", id, Optional.empty())),
                         Stream.of(Arguments.arguments("(- i0)",
                                                       Locals.get("i0"),
                                                       Optional.of(g.newNegExpr(Locals.get("i0"))))),
                         Stream.of(Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                                       Locals.get("$z0"),
                                                       Optional.of(g.newEqExpr(Locals.get("$z0"),
                                                                               IntConstant.v(0))))),
                         Stream.of(Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                                       Locals.get("i3"),
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
                                                       Locals.get("i4"),
                                                       Stream.of(g.newLeExpr(Locals.get("i3"),
                                                                             g.newAddExpr(Locals.get("i4"),
                                                                                          IntConstant.v(0))),
                                                                 g.newGeExpr(Locals.get("i3"),
                                                                             IntConstant.v(0)),
                                                                 g.newLeExpr(Locals.get("i4"),
                                                                             g.newAddExpr(Locals.get("i0"),
                                                                                          g.newNegExpr(IntConstant.v(1)))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and))),
                         Stream.of(Arguments.arguments("(and (or (<= i0 0) (> i0 1)) (or (<= i1 0) (> i1 2)) (<= i0 (+ i3 4)))",
                                                       Locals.get("i0"),
                                                       Stream.of(g.newOrExpr(g.newLeExpr(Locals.get("i0"),
                                                                                         IntConstant.v(0)),
                                                                             g.newGtExpr(Locals.get("i0"),
                                                                                         IntConstant.v(1))),
                                                                 g.newLeExpr(Locals.get("i0"),
                                                                             g.newAddExpr(Locals.get("i3"),
                                                                                          IntConstant.v(4))))
                                                       .map(e -> (Value)e)
                                                       .reduce(and)))
                         ).flatMap(s -> s.map(v -> v));
    }
}
