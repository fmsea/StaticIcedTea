package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;

import dev.fmsea.absint.scalar.state.DeferredOctagonState;
import dev.fmsea.absint.scalar.state.OctagonState;
import soot.Local;

public class DeferredOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new DeferredOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new DeferredOctagonState(inState);
    }
}
