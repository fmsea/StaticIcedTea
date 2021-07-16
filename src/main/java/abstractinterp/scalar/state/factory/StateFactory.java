package abstractinterp.scalar.state.factory;

import java.util.Set;

import soot.Local;

import abstractinterp.scalar.state.State;

public interface StateFactory<S extends State> {
    S initialFlow(Set<Local> locals);
    S entryFlow(Set<Local> locals);
    S mkState(Set<Local> locals, boolean top);
    S copy(S inState);
}
