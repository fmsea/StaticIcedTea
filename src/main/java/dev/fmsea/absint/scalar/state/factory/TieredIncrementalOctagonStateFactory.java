package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;

import dev.fmsea.absint.scalar.state.OctagonState;
import dev.fmsea.absint.scalar.state.TieredIncrementalOctagonState;
import soot.Local;

public class TieredIncrementalOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new TieredIncrementalOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new TieredIncrementalOctagonState((OctagonState) inState);
    }
}
