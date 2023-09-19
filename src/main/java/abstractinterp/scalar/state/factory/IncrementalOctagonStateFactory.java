package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.IncrementalOctagonState;
import abstractinterp.scalar.state.OctagonState;
import soot.Local;

public class IncrementalOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new IncrementalOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new IncrementalOctagonState((OctagonState) inState);
    }
}
