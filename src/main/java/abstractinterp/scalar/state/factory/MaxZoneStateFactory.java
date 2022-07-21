package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.MaxZoneState;

public class MaxZoneStateFactory implements StateFactory<MaxZoneState> {

    public MaxZoneState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public MaxZoneState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public MaxZoneState mkState(Set<Local> locals, boolean top) {
        return new MaxZoneState(locals, top);
    }

    public MaxZoneState copy(MaxZoneState inState) {
        return new MaxZoneState((MaxZoneState) inState);
    }
}
