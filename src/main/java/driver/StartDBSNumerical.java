package driver;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import abstractinterp.scalar.DBSNumerical;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver for full version of reaching definitions analysis
 * args[0] is the path where to write the output,
 * Make sure to have two folders in this path: invariants and time
 * args[1] is the class name
 * args[2] is the class path for class
 * args[3] is the method id in this class
 * args[4] is whether to persist computed invariants to a file (only set to no to compute average run)
 *
 */
public class StartDBSNumerical {
    private static String resultsPath = "ScratchData/resultsRD/";
    private static Logger LOGGER;

    public static void main(String[] args) {
        LOGGER = LoggerFactory.getLogger(StartDBSNumerical.class);
        String className = "test.Example1M";
        String classPath = Paths.get("artifacts/").toAbsolutePath().toString();
        int methodId = 6;
        boolean writeOutputToFile = false;
        if (args.length >= 5) {
            resultsPath = args[0];
            className = args[1];
            classPath = Paths.get(args[2]).toAbsolutePath().toString();
            methodId = Integer.parseInt(args[3]);
            writeOutputToFile = args[4].equals("y");
        }

        Path fileName = Paths.get(resultsPath, className + "_" + methodId);
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   System.getProperty("java.class.path") +
                                   File.pathSeparator +
                                   classPath);

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod m = sClass.getMethods().get(methodId);

        Body b = m.retrieveActiveBody();

        LOGGER.info("Analyzing Method -- {}", m.getName());
        LOGGER.trace("Soot Body: {}", b);

        DBSNumerical num = new DBSNumerical(b, 2);
        num.runAnalysis();

        if (writeOutputToFile) {
            try (FileWriter writer = new FileWriter(fileName.toFile())) {
                num.writeSMTReport(writer);
            } catch (IOException ex) {
                LOGGER.error("Unable to write results to file: {}", ex.toString());
                LOGGER.trace(Stream.of(ex.getStackTrace())
                             .map(StackTraceElement::toString)
                             .collect(Collectors.joining("\n")));
            }
        } else {
            num.report();
        }
    }
}
