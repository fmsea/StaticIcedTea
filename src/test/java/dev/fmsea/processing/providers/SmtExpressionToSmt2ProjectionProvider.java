package dev.fmsea.processing.providers;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

import dev.fmsea.common.Locals;

public class SmtExpressionToSmt2ProjectionProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context)
        throws Exception {

        return Stream.of(Arguments.arguments("(= i52 0)",
                                             Set.of(),
                                             Optional.of("(= i52 0)")),
                         Arguments.arguments("(<= i64 2)",
                                             Set.of(Locals.get("i52")),
                                             Optional.of("(<= i64 2)")),
                         Arguments.arguments("(= x 2147483647)",
                                             Set.of(Locals.get("x")),
                                             Optional.of("(= x 2147483647)")),
                         Arguments.arguments("(= x (- 2147483648))",
                                             Set.of(Locals.get("x")),
                                             Optional.of("(= x (- 2147483648))")),
                         Arguments.arguments("(and (<= i0 0) (> i0 1))",
                                             Set.of(),
                                             Optional.of("(and (<= i0 0) (> i0 1))")),
                         Arguments.arguments("(and (<= i0 (+ i1 0)) (or (> i1 0) (< i1 0)))",
                                             Locals.get("i0", "i1"),
                                             Optional.of("(and (<= i0 (+ i1 0)) (or (< i1 0) (> i1 0)))")),
                         Arguments.arguments("(and (>= i2 0) (>= i3 1) (>= i4 2) (<= i4 (+ i0 (- 1))))",
                                             Locals.get("i0", "i2", "i3", "i4"),
                                             Optional.of("(and (<= i4 (+ i0 (- 1))) (>= i2 0) (>= i3 1) (>= i4 2))")),
                         Arguments.arguments("(and (= i52 0) (<= i64 2))",
                                             Locals.get("i52", "i64"),
                                             Optional.of("(and (<= i64 2) (= i52 0))")),
                         Arguments.arguments("(not (= i52 0))",
                                             Set.of(Locals.get("i52")),
                                             Optional.of("(not (= i52 0))")),
                         Arguments.arguments("(and (<= i0 0) (not (= i52 0)))",
                                             Locals.get("i0", "i52"),
                                             Optional.of("(and (<= i0 0) (not (= i52 0)))")),
                         Arguments.arguments("(or (>= i1 0) (not (= i52 0)))",
                                             Locals.get("i1", "i52"),
                                             Optional.of("(or (>= i1 0) (not (= i52 0)))")),
                         Arguments.arguments("(and (<= i0 0) (<= i1 1) (<= x (+ y 3)))",
                                             Set.of(),
                                             Optional.of("(and (<= i0 0) (<= i1 1))")),
                         Arguments.arguments("(<= i3 (+ i2 0))",
                                             Set.of(Locals.get("i3")),
                                             Optional.empty()),
                         Arguments.arguments("(and (<= i3 (+ i2 0)))",
                                             Set.of(Locals.get("i3")),
                                             Optional.empty()),
                         Arguments.arguments("(and (or (= i3 0) (>= i3 5) (= i3 1) (and (< i3 5) (>= i3 2))) (<= i3 (+ i2 0)))",
                                             Locals.get("i3", "i2"),
                                             Optional.of("(and (<= i3 (+ i2 0)) (or (= i3 0) (= i3 1) (>= i3 5) (and (< i3 5) (>= i3 2))))")));
    }
}
