package processing.providers;

import java.util.Set;
import java.util.Map;
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

public class SmtExpressionReachableIdentityProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments("(= i0 0)",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0")))),
                         Arguments.arguments("(= i0 (+ i1 0))",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i1")),
                                                    Locals.get("i1"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i1")))),
                         Arguments.arguments("true",
                                             Map.of()),
                         Arguments.arguments("false",
                                             Map.of()),
                         Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0")),
                                                    Locals.get("i1"), Set.of(Locals.get("i1")))),
                         Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0")),
                                                    Locals.get("$z0"), Set.of(Locals.get("$z0")),
                                                    Locals.get("i3"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i3"),
                                                                             Locals.get("i4")),
                                                    Locals.get("i4"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i4")))),
                         Arguments.arguments("(and (= w y) (<= w (+ x 0)) (>= z (+ w 2)))",
                                             Map.of(Locals.get("w"), Set.of(Locals.get("w"),
                                                                            Locals.get("x"),
                                                                            Locals.get("y"),
                                                                            Locals.get("z")),
                                                    Locals.get("x"), Set.of(Locals.get("x")),
                                                    Locals.get("y"), Set.of(Locals.get("w"),
                                                                            Locals.get("x"),
                                                                            Locals.get("y"),
                                                                            Locals.get("z")),
                                                    Locals.get("z"), Set.of(Locals.get("z")))),
                         Arguments.arguments("(and (<= w (+ x 2)) (<= x (+ y 1)) (<= y (+ z 0)) (<= z (+ k (- 1))))",
                                             Map.of(Locals.get("w"), Set.of(Locals.get("k"),
                                                                            Locals.get("w"),
                                                                            Locals.get("x"),
                                                                            Locals.get("y"),
                                                                            Locals.get("z")),
                                                    Locals.get("x"), Set.of(Locals.get("k"),
                                                                            Locals.get("x"),
                                                                            Locals.get("y"),
                                                                            Locals.get("z")),
                                                    Locals.get("y"), Set.of(Locals.get("k"),
                                                                            Locals.get("y"),
                                                                            Locals.get("z")),
                                                    Locals.get("z"), Set.of(Locals.get("k"),
                                                                            Locals.get("z")),
                                                    Locals.get("k"), Set.of(Locals.get("k")))),
                         Arguments.arguments("(and (or (= i3 1) (and (>= i3 2) (<= i3 5))) (<= i3 (+ i2 i0)))",
                                             Map.of(Locals.get("i3"), Locals.get("i0", "i2", "i3"),
                                                    Locals.get("i0"), Set.of(Locals.get("i0")),
                                                    Locals.get("i2"), Set.of(Locals.get("i2")))),
                         Arguments.arguments("(and (or (= i3 1) (and (>= i3 2) (<= i3 5))) (<= i3 (+ i2 i0)) (<= i0 (+ i4 4)))",
                                             Map.of(Locals.get("i3"), Locals.get("i0", "i2", "i3", "i4"),
                                                    Locals.get("i0"), Locals.get("i0", "i4"),
                                                    Locals.get("i2"), Set.of(Locals.get("i2")),
                                                    Locals.get("i4"), Set.of(Locals.get("i4")))));
    }
}
