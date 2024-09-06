package dev.fmsea.absint.scalar.state;

import java.util.Set;

import dev.fmsea.absint.scalar.state.factory.OctagonStateFactory;
import dev.fmsea.tadr.TADR;
import soot.Local;

public class OctagonStateBuilder {
    private OctagonState state;

    public OctagonStateBuilder(OctagonStateFactory factory, Set<Local> locals, boolean top) {
        this.state = factory.mkState(locals, top);
    }

    public OctagonStateBuilder addConstraint(TADR expr) {
        this.state.refine(expr, this.state);
        return this;
    }

    public OctagonStateBuilder close() {
        this.state.close();
        return this;
    }

    public OctagonStateBuilder peek() {
        System.err.println(this.state.toString());
        return this;
    }

    public OctagonState build() {
        return this.state;
    }
}
