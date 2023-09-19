package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.DefaultOctagonStateFactory;

public class DefaultReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public DefaultReducedSMTOctagonProvider() {
        super(new DefaultOctagonStateFactory());
    }
}
