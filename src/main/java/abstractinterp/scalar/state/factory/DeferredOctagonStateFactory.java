package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.DeferredOctagonState;
import abstractinterp.scalar.state.OctagonState;
import soot.Local;

public class DeferredOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new DeferredOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new DeferredOctagonState(inState);
    }
}
