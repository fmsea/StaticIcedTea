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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import soot.Local;
import soot.Value;

import processing.providers.SmtExpressionIdentityProvider;
import processing.providers.SmtExpressionConnectedProvider;

public class SmtExpressionTest {

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionIdentityProvider.class)
    void testGetConnectedVariables(String smtExpression,
                                   Map<String, Set<String>> expected) {
        SmtExpression expr = new SmtExpressionReader(smtExpression).getSmtExpression();
        Map<Local, Set<Local>> connectedVariables = expr.getConnectedVariables();
        assertAll(Stream.concat(connectedVariables.keySet().stream()
                                .map(k -> () -> assertTrue(expected.containsKey(k.toString()))),
                                connectedVariables.entrySet().stream()
                                .map(kv -> {
                                        return () -> {
                                            assertEquals(expected.get(kv.getKey().toString()),
                                                         kv.getValue()
                                                         .stream().map(v -> v.toString())
                                                         .collect(Collectors.toSet()));
                                        };
                                    })));
    }

    @ParameterizedTest
    @ArgumentsSource(SmtExpressionConnectedProvider.class)
    void testGetValueById(String smtExpression, Local id, Optional<Value> expected) {
        try {
        SmtExpression expr = new SmtExpressionReader(smtExpression).getSmtExpression();
        assertEquals(expected.map(e -> e.toString()),
                     expr.getValue(id).map(e -> e.toString()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
