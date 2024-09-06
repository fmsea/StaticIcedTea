package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;

import dev.fmsea.absint.scalar.state.IncrementalOctagonState;
import dev.fmsea.absint.scalar.state.OctagonState;
import soot.Local;

public class IncrementalOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new IncrementalOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new IncrementalOctagonState((OctagonState) inState);
    }
}
