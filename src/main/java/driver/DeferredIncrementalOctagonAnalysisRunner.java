package driver;

import java.nio.file.Path;
import java.util.Set;

import abstractinterp.scalar.state.OctagonState;
import abstractinterp.scalar.state.factory.DeferredIncrementalOctagonStateFactory;
import driver.util.OrdererFactory;
import driver.util.OrdererType;

public class DeferredIncrementalOctagonAnalysisRunner extends AnalysisRunner<OctagonState> {

    public DeferredIncrementalOctagonAnalysisRunner(String className,
        int methodId,
        Path outputResultsPath,
        boolean outputStateReports,
        int widenIterations,
        Set<Integer> widenSteps,
        OrdererType orderer) {

        super(className,
            methodId,
            outputResultsPath,
            new DeferredIncrementalOctagonStateFactory(),
            outputStateReports,
            widenIterations,
            widenSteps,
            OrdererFactory.get(orderer));
    }
}
