package driver;

import java.nio.file.Path;
import java.util.Set;

import abstractinterp.scalar.state.OctagonState;
import abstractinterp.scalar.state.factory.IncrementalOctagonStateFactory;
import driver.util.OrdererFactory;
import driver.util.OrdererType;

public class IncrementalOctagonAnalysisRunner extends AnalysisRunner<OctagonState> {

    public IncrementalOctagonAnalysisRunner(String className,
        int methodId,
        Path outputResultsPath,
        boolean outputStateReports,
        int widenIterations,
        Set<Integer> widenSteps,
        OrdererType orderer) {

        super(className,
            methodId,
            outputResultsPath,
            new IncrementalOctagonStateFactory(),
            outputStateReports,
            widenIterations,
            widenSteps,
            OrdererFactory.get(orderer));
    }
}
