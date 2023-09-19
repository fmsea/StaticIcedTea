package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.DeferredIncrementalOctagonStateFactory;

public class DeferredFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public DeferredFullSMTOctagonProvider() {
        super(new DeferredIncrementalOctagonStateFactory());
    }
}
