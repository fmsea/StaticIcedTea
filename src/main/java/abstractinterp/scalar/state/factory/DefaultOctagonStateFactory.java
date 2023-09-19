package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.DefaultOctagonState;
import abstractinterp.scalar.state.OctagonState;
import soot.Local;

public class DefaultOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new DefaultOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new DefaultOctagonState((OctagonState) inState);
    }
}
