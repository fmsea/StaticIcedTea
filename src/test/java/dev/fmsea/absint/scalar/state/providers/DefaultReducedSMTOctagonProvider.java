package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.DefaultOctagonStateFactory;

public class DefaultReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public DefaultReducedSMTOctagonProvider() {
        super(new DefaultOctagonStateFactory());
    }
}
