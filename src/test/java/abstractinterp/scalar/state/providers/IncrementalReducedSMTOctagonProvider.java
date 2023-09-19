package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.IncrementalOctagonStateFactory;

public class IncrementalReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public IncrementalReducedSMTOctagonProvider() {
        super(new IncrementalOctagonStateFactory());
    }
}
