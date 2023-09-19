package abstractinterp.scalar.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import abstractinterp.scalar.state.factory.DeferredIncrementalOctagonStateFactory;
import abstractinterp.scalar.state.providers.DeferredFullSMTOctagonProvider;
import abstractinterp.scalar.state.providers.DeferredReducedSMTOctagonProvider;

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
