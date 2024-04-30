package abstractinterp.scalar.state.factory;

import java.util.Set;

import abstractinterp.scalar.state.DefaultOctagonState;
import abstractinterp.scalar.state.DeferredIncrementalOctagonState;
import abstractinterp.scalar.state.DeferredOctagonState;
import abstractinterp.scalar.state.IncZoneState;
import abstractinterp.scalar.state.IncZoneStateWithChangePriority;
import abstractinterp.scalar.state.IncrementalOctagonState;
import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.MaxZoneState;
import abstractinterp.scalar.state.MinZoneState;
import abstractinterp.scalar.state.OctagonState;
import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.ZoneState;
import soot.Local;

public interface StateFactory<S extends State> {

    public static <S extends State> StateFactory getFactory(Class<?> type) {
        if (type == IntervalBoxState.class) {
            return new IntervalBoxStateFactory();
        } else if (type == ZoneState.class) {
            return new ZoneStateFactory();
        } else if (type ==  MaxZoneState.class) {
            return new MaxZoneStateFactory();
        } else if (type ==  MinZoneState.class) {
            return new MinZoneStateFactory();
        } else if (type == IncZoneState.class) {
            return new IncZoneStateFactory();
        } else if (type == IncZoneStateWithChangePriority.class) {
            return new IncZoneStateWithChangePriorityFactory();
        } else if (type == DeferredIncrementalOctagonState.class) {
            return new DeferredIncrementalOctagonStateFactory();
        } else if (type == IncrementalOctagonState.class) {
            return new IncrementalOctagonStateFactory();
        } else if (type == DeferredOctagonState.class) {
            return new DeferredOctagonStateFactory();
        } else if (type == OctagonState.class || type == DefaultOctagonState.class) {
            return new DefaultOctagonStateFactory();
        } else {
            throw new UnsupportedOperationException("No factory for that state type");
        }
    }

    S initialFlow(Set<Local> locals);
    S entryFlow(Set<Local> locals);
    S mkState(Set<Local> locals, boolean top);
    S copy(S inState);
}
