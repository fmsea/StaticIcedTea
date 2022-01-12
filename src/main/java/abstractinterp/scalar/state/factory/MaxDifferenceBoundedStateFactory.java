package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.MaxDifferenceBoundedState;

public class MaxDifferenceBoundedStateFactory implements StateFactory<MaxDifferenceBoundedState> {

    public MaxDifferenceBoundedState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public MaxDifferenceBoundedState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public MaxDifferenceBoundedState mkState(Set<Local> locals, boolean top) {
        return new MaxDifferenceBoundedState(locals, top);
    }

    public MaxDifferenceBoundedState copy(MaxDifferenceBoundedState inState) {
        return new MaxDifferenceBoundedState((MaxDifferenceBoundedState) inState);
    }
}
