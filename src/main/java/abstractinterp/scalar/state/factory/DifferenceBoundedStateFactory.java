package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.DifferenceBoundedState;

public class DifferenceBoundedStateFactory implements StateFactory<DifferenceBoundedState> {

    public DifferenceBoundedState initialFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public DifferenceBoundedState entryFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public DifferenceBoundedState mkState(Set<Local> locals, boolean top) {
        return new DifferenceBoundedState(locals, top);
    }

    public DifferenceBoundedState copy(DifferenceBoundedState inState) {
        return new DifferenceBoundedState((DifferenceBoundedState) inState);
    }
}
