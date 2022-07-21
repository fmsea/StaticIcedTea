package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.IncZoneState;

public class IncZoneStateFactory implements StateFactory<IncZoneState> {

    public IncZoneState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public IncZoneState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public IncZoneState mkState(Set<Local> locals, boolean top) {
        return new IncZoneState(locals, top);
    }

    public IncZoneState copy(IncZoneState inState) {
        return new IncZoneState((IncZoneState) inState);
    }
}
