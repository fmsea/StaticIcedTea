package driver.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import disjoint.driver.StartAnalysis;
import util.Configuration;

@Command(name = "disjoint",
         mixinStandardHelpOptions = true,
         description = "Run Disjoint Analysis")
public class StartDisjointAnalysisCommand implements Callable<Integer> {

    @Parameters(description = "Arguments to be passed to IntervalNumerical")
    private List<String> arguments = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        Configuration.LoadArtifactsIntoSootPath();
        StartAnalysis.main(this.arguments.toArray(new String[0]));
        return 0;
    }
}
