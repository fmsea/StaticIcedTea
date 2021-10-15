package solver;

import com.microsoft.z3.Z3Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverFactory.class);

    // 10,000,000 milliseconds, 2.8 hours
    public static final int SOLVER_TIMEOUT_MS = 10000000;

    public static SolverWrapper getSolver() {
        return getSolver(SOLVER_TIMEOUT_MS);
    }

    public static SolverWrapper getSolver(int solverTimeout) {
        SolverWrapper s = null;
        try {
            s = new SolverWrapperZ3();
            s.setTimeOut(solverTimeout);
        } catch (Z3Exception ex) {
            LOGGER.error("Cannot instantiate the solver", ex);
            System.exit(2); // hmmm...
        }
        return s;
    }
}
