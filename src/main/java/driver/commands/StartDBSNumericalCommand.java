package driver.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import driver.StartDBSNumerical;

@Command(name = "dbs-numerical",
         mixinStandardHelpOptions = true,
         description = "Run Difference Bounded Numerical Analysis")
public class StartDBSNumericalCommand implements Callable<Integer> {

    @Parameters(description = "Arguments to be passed to DBSNumerical")
    private List<String> arguments = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        StartDBSNumerical.main(this.arguments.toArray(new String[0]));
        return 0;
    }
}
