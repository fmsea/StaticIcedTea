package driver;

import java.io.File;
import java.nio.file.Paths;

import abstractinterp.scalar.IntervalNumerical;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

/**
 * Driver for full versions of reaching definitions analysis
 * args[0] is the path where to write the output,
 * which are time and the computed invariants.
 * Make sure to have two folder on this paths: invariants and time.
 * args[1] is the class name
 * args[2] is the class path for class
 * args[3] is the method id in this class
 * args[4] is whether to write computed invariants (only set to no to compute average run)
 *
 */
public class StartIntervalNumerical {
    private static String resultsPath = "ScratchData/resultsRD/";

    public static void main(String[] args) {
        String className = "test.Example1M";
        String classPath = Paths.get("artifacts/").toAbsolutePath().toString();
        int methodId = 6;
        boolean writeInv = true;
        if (args.length >= 5) {
            resultsPath = args[0];
            className = args[1];
            classPath = Paths.get(args[2]).toAbsolutePath().toString();
            methodId = Integer.parseInt(args[3]);
            writeInv = args[4].equals("y");
        }

        String fileName = resultsPath + "/invariants/" + className + "_" + methodId;
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   System.getProperty("java.class.path") +
                                   File.pathSeparator +
                                   classPath);
        SootClass sClass = Scene.v().loadClassAndSupport(className);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        //we are analyzing sClass
        SootMethod m = sClass.getMethods().get(methodId);

        Body b = m.retrieveActiveBody();

        System.out.println("=======================================");
        System.out.println(m.toString() + " " + writeInv);;

        IntervalNumerical num = new IntervalNumerical(b, 2);
        num.runAnalysis();
        num.report();
    }
}
