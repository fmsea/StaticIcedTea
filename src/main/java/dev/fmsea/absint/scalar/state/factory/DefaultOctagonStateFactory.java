package dev.fmsea.absint.scalar.state.factory;

import java.util.Set;

import dev.fmsea.absint.scalar.state.DefaultOctagonState;
import dev.fmsea.absint.scalar.state.OctagonState;
import soot.Local;

public class DefaultOctagonStateFactory extends OctagonStateFactory {

    public OctagonState mkState(Set<Local> locals, boolean top) {
        return new DefaultOctagonState(locals, top);
    }

    public OctagonState copy(OctagonState inState) {
        return new DefaultOctagonState((OctagonState) inState);
    }
}
