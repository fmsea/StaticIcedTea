package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.IncZoneStateWithChangePriority;

public class IncZoneStateWithChangePriorityFactory implements StateFactory<IncZoneStateWithChangePriority> {

    public IncZoneStateWithChangePriority initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public IncZoneStateWithChangePriority entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public IncZoneStateWithChangePriority mkState(Set<Local> locals, boolean top) {
        return new IncZoneStateWithChangePriority(locals, top);
    }

    public IncZoneStateWithChangePriority copy(IncZoneStateWithChangePriority inState) {
        return new IncZoneStateWithChangePriority((IncZoneStateWithChangePriority) inState);
    }
}
