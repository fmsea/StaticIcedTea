package driver;

import java.util.Set;
import java.nio.file.Path;
import abstractinterp.scalar.state.IncZoneState;
import abstractinterp.scalar.state.factory.IncZoneStateFactory;

public class IncZoneAnalysisRunner extends AnalysisRunner<IncZoneState> {

    public IncZoneAnalysisRunner(String className,
                                 int methodId,
                                 Path outputResultsPath,
                                 boolean outputStateReports,
                                 int widenIterations) {
        this(className,
             methodId,
             outputResultsPath,
             outputStateReports,
             widenIterations,
             Set.of());
    }

    public IncZoneAnalysisRunner(String className,
                                 int methodId,
                                 Path outputResultsPath,
                                 boolean outputStateReports,
                                 int widenIterations,
                                 Set<Integer> widenSteps) {
        super(className,
              methodId,
              outputResultsPath,
              new IncZoneStateFactory(),
              outputStateReports,
              widenIterations,
              widenSteps);
    }
}
