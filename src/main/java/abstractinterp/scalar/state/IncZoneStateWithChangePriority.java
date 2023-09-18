package abstractinterp.scalar.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.internal.JNegExpr;

import abstractinterp.scalar.state.util.GraphProjection;
import solver.SolverWrapper;

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
