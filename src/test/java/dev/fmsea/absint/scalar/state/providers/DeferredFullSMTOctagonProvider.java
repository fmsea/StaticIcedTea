package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.DeferredIncrementalOctagonStateFactory;

public class DeferredFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public DeferredFullSMTOctagonProvider() {
        super(new DeferredIncrementalOctagonStateFactory());
    }
}
