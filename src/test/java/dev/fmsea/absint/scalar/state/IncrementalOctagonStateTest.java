package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.absint.scalar.state.factory.IncrementalOctagonStateFactory;
import dev.fmsea.absint.scalar.state.providers.IncrementalFullSMTOctagonProvider;
import dev.fmsea.absint.scalar.state.providers.IncrementalReducedSMTOctagonProvider;

public class IncrementalOctagonStateTest extends OctagonStateTest {

    public IncrementalOctagonStateTest() {
        super(new IncrementalOctagonStateFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(IncrementalFullSMTOctagonProvider.class)
    void testToSmt(OctagonState state, String oracle) {
        assertEquals(oracle, state.toSmt());
    }


    @ParameterizedTest
    @ArgumentsSource(IncrementalReducedSMTOctagonProvider.class)
    void testReducedSmt(OctagonState state, String oracle) {
        state.reduce();
        assertEquals(oracle, state.toSmt());
    }
}
