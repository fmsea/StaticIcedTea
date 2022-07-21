package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.ZoneState;

public class ZoneStateFactory implements StateFactory<ZoneState> {

    public ZoneState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public ZoneState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public ZoneState mkState(Set<Local> locals, boolean top) {
        return new ZoneState(locals, top);
    }

    public ZoneState copy(ZoneState inState) {
        return new ZoneState((ZoneState) inState);
    }
}
