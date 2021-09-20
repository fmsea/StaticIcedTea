package driver.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import driver.StartPADO01DBSNumerical;

@Command(name = "pado-numerical",
         mixinStandardHelpOptions = true,
         description = "Run PADO01 Difference Bounded Numerical Analysis")
public class StartPADO01DBSNumericalCommand implements Callable<Integer> {

    @Parameters(description = "Arguments to be passed to DBSNumerical")
    private List<String> arguments = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        StartPADO01DBSNumerical.main(this.arguments.toArray(new String[0]));
        return 0;
    }
}
