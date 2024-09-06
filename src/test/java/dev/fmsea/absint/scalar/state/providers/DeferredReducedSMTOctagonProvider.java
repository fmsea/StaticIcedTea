package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.DeferredIncrementalOctagonStateFactory;

public class DeferredReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public DeferredReducedSMTOctagonProvider() {
        super(new DeferredIncrementalOctagonStateFactory());
    }
}
