package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;

import soot.Local;

import dev.fmsea.absint.scalar.state.MinZoneState;

public class MinZoneStateFactory implements StateFactory<MinZoneState> {

    public MinZoneState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public MinZoneState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public MinZoneState mkState(Set<Local> locals, boolean top) {
        return new MinZoneState(locals, top);
    }

    public MinZoneState copy(MinZoneState inState) {
        return new MinZoneState((MinZoneState) inState);
    }
}
