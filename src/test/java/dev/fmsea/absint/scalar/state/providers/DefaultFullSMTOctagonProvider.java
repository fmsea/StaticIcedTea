package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.DefaultOctagonStateFactory;

public class DefaultFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public DefaultFullSMTOctagonProvider() {
        super(new DefaultOctagonStateFactory());
    }
}
