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

public class SmtExpressionIdentityProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments("(= i0 0)",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0")))),
                         Arguments.arguments("true",
                                             Map.of()),
                         Arguments.arguments("false",
                                             Map.of()),
                         Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                             Map.of(Locals.get("i0"), Set.of(Locals.get("i0")),
                                                    Locals.get("i1"), Set.of(Locals.get("i1")))),
                         Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                             Map.of(Locals.get("$z0"), Set.of(Locals.get("$z0")),
                                                    Locals.get("i3"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i3"),
                                                                             Locals.get("i4")),
                                                    Locals.get("i4"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i3"),
                                                                             Locals.get("i4")),
                                                    Locals.get("i0"), Set.of(Locals.get("i0"),
                                                                             Locals.get("i3"),
                                                                             Locals.get("i4")))),
                         Arguments.arguments("(and (<= k (+ y 0)) (<= x (+ y 3)))",
                                             Map.of(Locals.get("k"), Locals.get("k", "x", "y"),
                                                    Locals.get("x"), Locals.get("k", "x", "y"),
                                                    Locals.get("y"), Locals.get("k", "x", "y"))));
    }
}
