package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.factory.IncrementalOctagonStateFactory;

public class IncrementalFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public IncrementalFullSMTOctagonProvider() {
        super(new IncrementalOctagonStateFactory());
    }
}
