package abstractinterp.scalar.state;

import java.util.Set;

import abstractinterp.scalar.state.factory.OctagonStateFactory;
import soot.Local;
import tadr.TADR;

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
