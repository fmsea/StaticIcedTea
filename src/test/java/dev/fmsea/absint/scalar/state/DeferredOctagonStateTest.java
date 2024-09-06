package dev.fmsea.absint.scalar.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import dev.fmsea.absint.scalar.state.factory.DeferredIncrementalOctagonStateFactory;
import dev.fmsea.absint.scalar.state.providers.DeferredFullSMTOctagonProvider;
import dev.fmsea.absint.scalar.state.providers.DeferredReducedSMTOctagonProvider;

public class DeferredOctagonStateTest extends OctagonStateTest {

    public DeferredOctagonStateTest() {
        super(new DeferredIncrementalOctagonStateFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(DeferredFullSMTOctagonProvider.class)
    void testToSmt(OctagonState state, String oracle) {
        assertEquals(oracle, state.toSmt());
    }


    @ParameterizedTest
    @ArgumentsSource(DeferredReducedSMTOctagonProvider.class)
    void testReducedSmt(OctagonState state, String oracle) {
        state.reduce();
        assertEquals(oracle, state.toSmt());
    }

}
