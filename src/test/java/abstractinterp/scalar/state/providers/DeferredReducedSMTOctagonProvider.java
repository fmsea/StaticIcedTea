package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.DeferredIncrementalOctagonStateFactory;

public class DeferredReducedSMTOctagonProvider extends ReducedSMTOctagonProvider {

    public DeferredReducedSMTOctagonProvider() {
        super(new DeferredIncrementalOctagonStateFactory());
    }
}
