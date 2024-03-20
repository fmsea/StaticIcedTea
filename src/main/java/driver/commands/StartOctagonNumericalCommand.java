package driver.commands;

import abstractinterp.scalar.state.DefaultOctagonState;
import driver.AnalysisOptions;
import driver.AnalysisOptionsBuilder;
import driver.OctagonAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;

@Command(name = "octagon-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Octagon Analysis")
public class StartOctagonNumericalCommand extends NumericalAnalysisCommand {

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        AnalysisOptions options = new AnalysisOptionsBuilder()
            .withClassName(className)
            .withMethodId(methodId)
            .withOutputResultsPath(outputResultsPath)
            .withOutputStateReports(outputReport)
            .withWidenIterations(widenIterations)
            .withWidenSteps(widenSteps)
            .withStateType(DefaultOctagonState.class)
            .withOrderer(orderer)
            .withReduceOutput(reduceOutput)
            .build();
        Runnable runner = new OctagonAnalysisRunner(options);
        runner.run();
        return 0;
    }
}
