package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.MinDifferenceBoundedState;

public class MinDifferenceBoundedStateFactory implements StateFactory<MinDifferenceBoundedState> {

    public MinDifferenceBoundedState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public MinDifferenceBoundedState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public MinDifferenceBoundedState mkState(Set<Local> locals, boolean top) {
        return new MinDifferenceBoundedState(locals, top);
    }

    public MinDifferenceBoundedState copy(MinDifferenceBoundedState inState) {
        return new MinDifferenceBoundedState((MinDifferenceBoundedState) inState);
    }
}
