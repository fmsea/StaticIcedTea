package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.DeferredIncrementalOctagonState;
import abstractinterp.scalar.state.OctagonState;
import soot.Local;

public class DeferredIncrementalOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new DeferredIncrementalOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new DeferredIncrementalOctagonState(inState);
    }
}
