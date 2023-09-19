package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.OctagonState;
import soot.Local;

public abstract class OctagonStateFactory implements StateFactory<OctagonState> {

    public OctagonState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public OctagonState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public abstract OctagonState mkState(Set<Local> locals, boolean top);

    public abstract OctagonState copy(OctagonState inState);
}
