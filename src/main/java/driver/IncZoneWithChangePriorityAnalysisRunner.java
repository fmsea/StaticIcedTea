package driver;

import java.nio.file.Path;
import abstractinterp.scalar.state.IncZoneStateWithChangePriority;
import abstractinterp.scalar.state.factory.IncZoneStateWithChangePriorityFactory;

public class IncZoneWithChangePriorityAnalysisRunner extends AnalysisRunner<IncZoneStateWithChangePriority> {

    public IncZoneWithChangePriorityAnalysisRunner(String className,
                                                   int methodId,
                                                   Path outputResultsPath,
                                                   boolean outputStateReports,
                                                   int widenIterations) {
        super(className,
              methodId,
              outputResultsPath,
              new IncZoneStateWithChangePriorityFactory(),
              outputStateReports,
              widenIterations);
    }
}
