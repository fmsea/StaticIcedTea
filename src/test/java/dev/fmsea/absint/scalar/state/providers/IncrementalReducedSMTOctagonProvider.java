package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.IncrementalOctagonStateFactory;

public class IncrementalReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public IncrementalReducedSMTOctagonProvider() {
        super(new IncrementalOctagonStateFactory());
    }
}
