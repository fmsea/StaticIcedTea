package driver;

import java.util.Set;
import java.nio.file.Path;
import abstractinterp.scalar.state.IncZoneState;
import abstractinterp.scalar.state.factory.IncZoneStateFactory;
import driver.util.OrdererFactory;
import driver.util.OrdererType;

public class IncZoneAnalysisRunner extends AnalysisRunner<IncZoneState> {

    public IncZoneAnalysisRunner(String className,
                                 int methodId,
                                 Path outputResultsPath,
                                 boolean outputStateReports,
                                 int widenIterations,
                                 Set<Integer> widenSteps,
                                 OrdererType orderer) {
        super(className,
              methodId,
              outputResultsPath,
              new IncZoneStateFactory(),
              outputStateReports,
              widenIterations,
              widenSteps,
              OrdererFactory.get(orderer));
    }
}
