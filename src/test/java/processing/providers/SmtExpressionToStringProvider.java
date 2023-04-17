package processing.providers;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class SmtExpressionToStringProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments("(= i52 0)"),
                         Arguments.arguments("(<= i64 2)"),
                         Arguments.arguments("(and (<= i0 0) (> i0 1))"),
                         Arguments.arguments("(and (<= i0 (+ i1 0)) (or (< i1 0) (> i1 0)))"),
                         Arguments.arguments("(and (<= i4 (+ i0 (- 1))) (>= i2 0) (>= i3 1) (>= i4 2))"),
                         Arguments.arguments("(and (<= i64 2) (= i52 0))"),
                         Arguments.arguments("(not (= i52 0))"),
                         Arguments.arguments("(and (<= i0 0) (not (= i52 0)))"),
                         Arguments.arguments("(or (>= i1 0) (not (= i52 0)))"),
                         Arguments.arguments("(and (not (= i52 (+ i7 (* i8 (- 3))))) true)"),
                         Arguments.arguments("(or (not (= i52 (- 1))) false)"));
    }
}
