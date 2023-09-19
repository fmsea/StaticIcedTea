package abstractinterp.scalar.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import abstractinterp.scalar.state.factory.DefaultOctagonStateFactory;
import abstractinterp.scalar.state.providers.DefaultFullSMTOctagonProvider;
import abstractinterp.scalar.state.providers.DefaultReducedSMTOctagonProvider;

public class DefaultOctagonStateTest extends OctagonStateTest {

    public DefaultOctagonStateTest() {
        super(new DefaultOctagonStateFactory());
    }

    @ParameterizedTest
    @ArgumentsSource(DefaultFullSMTOctagonProvider.class)
    void testToSmt(OctagonState state, String oracle) {
        assertEquals(oracle, state.toSmt());
    }


    @ParameterizedTest
    @ArgumentsSource(DefaultReducedSMTOctagonProvider.class)
    void testReducedSmt(OctagonState state, String oracle) {
        state.reduce();
        assertEquals(oracle, state.toSmt());
    }

}
