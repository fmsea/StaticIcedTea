package processing.providers;

import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import soot.Local;
import soot.IntType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.grimp.Grimp;

import common.Locals;

public class SmtExpressionProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        Grimp g = Grimp.v();

        return Stream.of(Arguments.arguments("(and (<= i0 0) (> i0 1))",
                                             Set.of(Locals.get("i0")),
                                             Optional.of(g.newAndExpr(g.newLeExpr(Locals.get("i0"),
                                                                                  IntConstant.v(0)),
                                                                      g.newGtExpr(Locals.get("i0"),
                                                                                  IntConstant.v(1))))),
                         Arguments.arguments("(and (<= i0 (+ i1 0)) (or (> i1 0) (< i1 0)))",
                                             Set.of(Locals.get("i0"), Locals.get("i1")),
                                             Optional.of(g.newAndExpr(g.newLeExpr(Locals.get("i0"),
                                                                                  g.newAddExpr(Locals.get("i1"),
                                                                                               IntConstant.v(0))),
                                                                      g.newOrExpr(g.newLtExpr(Locals.get("i1"),
                                                                                              IntConstant.v(0)),
                                                                                  g.newGtExpr(Locals.get("i1"),
                                                                                              IntConstant.v(0)))))),
                         Arguments.arguments("(and (>= i2 0) (>= i3 1) (>= i4 2) (<= i4 (+ i0 (- 1))))",
                                             Set.of(Locals.get("i4"), Locals.get("i3")),
                                             Optional.of(g.newAndExpr(g.newGeExpr(Locals.get("i3"),
                                                                                               IntConstant.v(1)),
                                                                      g.newAndExpr(g.newLeExpr(Locals.get("i4"),
                                                                                               g.newAddExpr(Locals.get("i0"),
                                                                                                            g.newNegExpr(IntConstant.v(1)))),
                                                                                   g.newGeExpr(Locals.get("i4"),
                                                                                               IntConstant.v(2)))))));
    }
}
