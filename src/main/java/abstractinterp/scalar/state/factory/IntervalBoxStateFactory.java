package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.IntervalBoxState;

public class IntervalBoxStateFactory implements StateFactory<IntervalBoxState> {

    public IntervalBoxState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public IntervalBoxState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public IntervalBoxState mkState(Set<Local> locals, boolean top) {
        return new IntervalBoxState(locals, top);
    }

    public IntervalBoxState copy(IntervalBoxState inState) {
        return new IntervalBoxState((IntervalBoxState) inState);
    }
}
