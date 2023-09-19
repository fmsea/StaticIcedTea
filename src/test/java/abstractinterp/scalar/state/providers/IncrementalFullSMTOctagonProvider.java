package abstractinterp.scalar.state.providers;

import abstractinterp.scalar.state.factory.IncrementalOctagonStateFactory;

public class IncrementalFullSMTOctagonProvider extends FullSMTOctagonProvider {

    public IncrementalFullSMTOctagonProvider() {
        super(new IncrementalOctagonStateFactory());
    }
}
