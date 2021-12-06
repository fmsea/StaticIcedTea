package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.IncDifferenceBoundedState;

public class IncDifferenceBoundedStateFactory implements StateFactory<IncDifferenceBoundedState> {

    public IncDifferenceBoundedState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public IncDifferenceBoundedState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public IncDifferenceBoundedState mkState(Set<Local> locals, boolean top) {
        return new IncDifferenceBoundedState(locals, top);
    }

    public IncDifferenceBoundedState copy(IncDifferenceBoundedState inState) {
        return new IncDifferenceBoundedState((IncDifferenceBoundedState) inState);
    }
}
