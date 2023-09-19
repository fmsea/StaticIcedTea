package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.DefaultOctagonStateFactory;

public class DefaultFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public DefaultFullSMTOctagonProvider() {
        super(new DefaultOctagonStateFactory());
    }
}
