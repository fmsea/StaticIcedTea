package dev.fmsea.absint.scalar.state;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;

public class IncZoneStateWithChangePriority extends IncZoneState {

    public IncZoneStateWithChangePriority(Set<Local> locals, boolean top) {
        super(locals, top);
    }

    @Override
    protected void initializeMatrix(Set<Local> locals, boolean top) {
        Set<Local> localsWithZero = Stream.concat(locals.stream(), Stream.of(ZERO))
            .collect(Collectors.toSet());
        this.matrix = new ZoneDifferenceBoundedMatrixWithChangePriority(localsWithZero, top);
    }

    public IncZoneStateWithChangePriority(IncZoneState state) {
        super(state);
    }

    public IncZoneStateWithChangePriority(ZoneDifferenceBoundedMatrixWithChangePriority matrix) {
        super(matrix);
    }
}
