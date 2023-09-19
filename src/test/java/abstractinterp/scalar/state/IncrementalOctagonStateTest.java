package abstractinterp.scalar.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import abstractinterp.scalar.state.factory.IncrementalOctagonStateFactory;
import abstractinterp.scalar.state.providers.IncrementalFullSMTOctagonProvider;
import abstractinterp.scalar.state.providers.IncrementalReducedSMTOctagonProvider;

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
