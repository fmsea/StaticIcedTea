package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import dev.fmsea.absint.ConstraintType;
import dev.fmsea.common.Locals;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.ZonotopalInferenceParser;
import dev.fmsea.inference.rewrite.DefaultRewriter;
import dev.fmsea.processing.InferDomains;
import soot.Local;

public class SimpleOctagonStateTests {

    @ParameterizedTest
    @MethodSource("constraintQueries")
    public void testConstraintQueries(Set<String> expressions, Local candidate, ConstraintType oracle) {
        DefaultRewriter rewriter = new DefaultRewriter();
        Set<InvariantExpression> exprs = expressions.stream()
            .map(expr -> ZonotopalInferenceParser.parseExpr(expr))
            .flatMap(expr -> rewriter.rewrite(expr))
            .collect(Collectors.toSet());
        SimpleOctagonState state = InferDomains.from(exprs);
        assertTrue(state.queryConstraintTypes().get(oracle).contains(candidate));
    }

    public static Stream<Arguments> constraintQueries() {
        return Stream.of(
            Arguments.of(Set.of("v0 >= 0"), Locals.get("v0"), ConstraintType.INTERVAL)
            // Arguments.of(Set.of("v0 >= 0", "v1 >= 0", "v2 >= 0"), Locals.get("v0"), ConstraintType.INTERVAL),
            // Arguments.of(Set.of("v0 >= 0", "v1 - v2 <= 3"), Locals.get("v0"), ConstraintType.INTERVAL),
            // Arguments.of(Set.of("v1 == 0"), Locals.get("v0"), ConstraintType.INTERVAL),
            // Arguments.of(Set.of("v0 == 4", "v1 + v2 <= 4"), Locals.get("v0"), ConstraintType.INTERVAL),
            // Arguments.of(Set.of("v0 - v1 <= 4", "v1 <= 2", "v0 >= 6"), Locals.get("v0"), ConstraintType.INTERVAL),
            // Arguments.of(Set.of("v0 >= 0", "v0 - v1 <= 2"), Locals.get("v0"), ConstraintType.ZONAL),
            // Arguments.of(Set.of("v0 - v1 <= 4", "v1 >= 2", "v3 >= 6"), Locals.get("v0"), ConstraintType.ZONAL),
            // Arguments.of(Set.of("v0 - v1 <= 3", "v2 + v3 <= 4"), Locals.get("v0"), ConstraintType.ZONAL),
            // Arguments.of(Set.of("v0 + v1 <= 3"), Locals.get("v0"), ConstraintType.OCTAGONAL),
            // Arguments.of(Set.of("v1 + v0 >= 4"), Locals.get("v0"), ConstraintType.OCTAGONAL)
        );
    }
}
