package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.PADO01DifferenceBoundedState;

public class PADO01DifferenceBoundedStateFactory implements StateFactory<PADO01DifferenceBoundedState> {

    public PADO01DifferenceBoundedState initialFlow(Set<Local> locals) {
        return this.mkState(locals, false);
    }

    public PADO01DifferenceBoundedState entryFlow(Set<Local> locals) {
        return this.mkState(locals, true);
    }

    public PADO01DifferenceBoundedState mkState(Set<Local> locals, boolean top) {
        return new PADO01DifferenceBoundedState(locals, top);
    }

    public PADO01DifferenceBoundedState copy(PADO01DifferenceBoundedState inState) {
        return new PADO01DifferenceBoundedState((PADO01DifferenceBoundedState) inState);
    }
}
