package driver.commands;

import java.io.File;

import driver.PredicateAnalysisRunner;
import driver.util.SootInitialization;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "predicate",
         mixinStandardHelpOptions = true,
         description = "Run Predicate Analysis")
public class StartPredicateNumericalCommand extends AnalysisCommand {

    @Option(names = {"--domain"}, description = "Domain file for analysis", required = true)
    private File domainFile;

    @Option(names = {"--symbolic"},
            description = "Whether to use enable symbolic interpretation",
            required = false)
    private String symbolic = "Y";

    @Option(names = {"--output-symbolic-states"},
            description = "Whether to include symbolic state information in analysis report.  Implies Symbolic",
            required = false,
            defaultValue = "false",
            fallbackValue = "false",
            negatable = true)
    private boolean outputSymbolicStates;

    @Override
    public Integer call() throws Exception {
        SootInitialization.initializeSoot(className, classpath);
        Runnable runner = new PredicateAnalysisRunner(
            className,
            methodId,
            outputResultsPath,
            domainFile,
            symbolic.equals("Y") || outputSymbolicStates,
            outputReport,
            outputSymbolicStates);
        runner.run();
        return 0;
    }
}
