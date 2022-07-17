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

public class SmtExpressionIdentityProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments("(= i0 0)",
                                             Map.of("i0", Set.of())),
                         Arguments.arguments("true",
                                             Map.of()),
                         Arguments.arguments("false",
                                             Map.of()),
                         Arguments.arguments("(or (= i0 1) (>= i1 2))",
                                             Map.of("i0", Set.of(),
                                                    "i1", Set.of())),
                         Arguments.arguments("(and (= $z0 0) (>= i3 0) (<= i4 (+ i0 (- 1))) (<= i3 (+ i4 0)))",
                                             Map.of("$z0", Set.of(),
                                                    "i3", Set.of("i4"),
                                                    "i4", Set.of("i0", "i3"),
                                                    "i0", Set.of("i4"))));
    }
}
