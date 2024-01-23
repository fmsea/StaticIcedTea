package driver.commands;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import driver.GenerateOctagonsRunner;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "generate-octagons",
         mixinStandardHelpOptions = true,
         description = "Generate Feasible Octagons for Testing Purposes")
public class GenerateOctagonsCommand implements Callable<Integer> {

    @Parameters(index = "0",
        description = "Output Path for generate octagons.")
    private Path outputPath;

    @Parameters(index = "1",
        description = "Number of Octagons to Generate")
    private int numOctagons;

    @Override
    public Integer call() throws Exception {
        Runnable runner = new GenerateOctagonsRunner(this.outputPath, this.numOctagons);
        runner.run();
        return 0;
    }
}
