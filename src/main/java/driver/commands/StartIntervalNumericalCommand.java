package driver.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import driver.StartIntervalNumerical;

@Command(name = "interval-numerical",
         mixinStandardHelpOptions = true,
         description = "Run IntervalNumerical Analysis")
public class StartIntervalNumericalCommand implements Callable<Integer> {

    @Parameters(description = "Arguments to be passed to IntervalNumerical")
    private List<String> arguments = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        StartIntervalNumerical.main(this.arguments.toArray(new String[0]));
        return 0;
    }
}
