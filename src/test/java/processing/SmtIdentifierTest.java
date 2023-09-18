package processing;

import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import common.Locals;

class SmtIdentifierTest {

    @ParameterizedTest
    @MethodSource("provideEqualArguments")
    void testEquals(SmtIdentifier a, SmtIdentifier b) {
        assertEquals(a, b);
    }

    @ParameterizedTest
    @MethodSource("provideNotEqualArguments")
    void testNotEquals(SmtIdentifier a, SmtIdentifier b) {
        assertNotEquals(a, b);
    }

    @ParameterizedTest
    @MethodSource("provideComparisons")
    void testCompareTo(SmtIdentifier a, SmtIdentifier b, int expected) {
        assertEquals(expected, a.compareTo(b));
    }

    private static Stream<Arguments> provideEqualArguments() {
        return Stream.of(Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i0"), false)),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i1"), true),
                                             new SmtIdentifier(Locals.get("i1"), true)));
    }

    private static Stream<Arguments> provideNotEqualArguments() {
        return Stream.of(Arguments.arguments(new SmtIdentifier(Locals.get("i0"), false),
                                             new SmtIdentifier(Locals.get("i1"), false)),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i1"))),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i0"), true)),
                         Arguments.arguments(new SmtIdentifier(Locals.get("$z0"), true),
                                             new SmtIdentifier(Locals.get("$z0"), false)));
    }

    private static Stream<Arguments> provideComparisons() {
        return Stream.of(Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i0")),
                                             +0),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0"), true),
                                             new SmtIdentifier(Locals.get("i0")),
                                             +1),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i0"), true),
                                             -1),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0")),
                                             new SmtIdentifier(Locals.get("i1")),
                                             -1),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i1")),
                                             new SmtIdentifier(Locals.get("i0")),
                                             +1),
                         Arguments.arguments(new SmtIdentifier(Locals.get("i0"), true),
                                             new SmtIdentifier(Locals.get("i1")),
                                             +1));
    }
}
