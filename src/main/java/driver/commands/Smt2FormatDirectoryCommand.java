package driver.commands;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import processing.Smt2Format;


@Command(name = "smt-format-dir",
         mixinStandardHelpOptions = true,
         description = "Run smt2format on a directory of files")
public class Smt2FormatDirectoryCommand implements Callable<Integer> {

    @Parameters(description = "Arguments to be passed to Smt2Format")
    private List<String> arguments = new ArrayList<>();

    @Override
    public Integer call() throws Exception {
        Smt2Format.main(this.arguments.toArray(new String[0]));
        return 0;
    }
}
