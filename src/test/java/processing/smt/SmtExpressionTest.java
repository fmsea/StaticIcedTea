package processing.smt;

import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import soot.Local;
import soot.Value;

import common.Locals;
import processing.providers.SmtExpressionProvider;
import processing.providers.SmtExpressionIdentityProvider;
import processing.providers.SmtExpressionReachableIdentityProvider;
import processing.providers.SmtExpressionToSmt2Provider;
import processing.providers.SmtExpressionToSmt2SelectionProvider;
import processing.providers.SmtExpressionToStringProvider;

public class SmtExpressionTest {

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionIdentityProvider.class)
    void testGetConnectedVariables(String smtExpression,
                                   Map<Local, Set<Local>> expected) {
        SmtExpression expr = new SmtExpressionReader(smtExpression).getSmtExpression();
        Map<Local, Set<Local>> connectedVariables = expr.getConnectedVariables();
        assertAll(Stream.concat(connectedVariables.keySet().stream()
                                .map(k -> () -> assertTrue(expected.containsKey(k))),
                                connectedVariables.entrySet().stream()
                                .map(kv -> {
                                        return () -> {
                                            assertEquals(expected.get(kv.getKey()),
                                                         kv.getValue());
                                        };
                                    })));
    }

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionReachableIdentityProvider.class)
    void testGetReachableVariables(String smtExpression,
                                   Map<Local, Set<Local>> expected) {
        SmtExpression expr = SmtExpressionReader.parse(smtExpression);
        Map<Local, Set<Local>> reachableVariables = expr.getReachableVariables();
        Stream<Executable> assertions0 = reachableVariables.keySet().stream().map(k -> () -> assertTrue(expected.containsKey(k)));
        Stream<Executable> assertions1 = expected.keySet().stream().map(k -> () -> assertTrue(reachableVariables.containsKey(k)));
        Stream<Executable> assertions2 = reachableVariables.entrySet().stream().map(kv -> () -> assertEquals(expected.getOrDefault(kv.getKey(), Set.of()), kv.getValue()));
        Stream<Executable> assertions3 = expected.entrySet().stream().map(kv -> () -> assertEquals(kv.getValue(), reachableVariables.getOrDefault(kv.getKey(), Set.of())));
        Stream<Executable> assertions = Stream.of(assertions0,
                                                  assertions1,
                                                  assertions2,
                                                  assertions3).flatMap(s -> s.map(v -> v));
        assertAll(assertions);
    }

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionToSmt2Provider.class)
    void testToSmt2(String smtExpression) {
        try {
            SmtExpression expr = SmtExpressionReader.parse(smtExpression);
            assertEquals(smtExpression, expr.toSmt2());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionToSmt2SelectionProvider.class)
    void testToSmt2Variables(String smtExpression, Set<Local> variables, Optional<String> expected) {
        try {
            SmtExpression expr = SmtExpressionReader.parse(smtExpression);
            assertEquals(expected, expr.toSmt2(variables));
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionToStringProvider.class)
    void testToString(String smtExpression) {
        try {
            SmtExpression expr = SmtExpressionReader.parse(smtExpression);
            assertEquals(smtExpression, expr.toString());
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            assertTrue(false);
        }
    }
}
