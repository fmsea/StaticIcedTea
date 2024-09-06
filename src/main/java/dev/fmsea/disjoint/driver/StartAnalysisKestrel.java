package dev.fmsea.disjoint.driver;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;

import dev.fmsea.disjoint.analysis.ValueTransformer;
import dev.fmsea.disjoint.domain.Domain;
import dev.fmsea.disjoint.domain.reader.DomainReader;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartAnalysisKestrel {

    public static boolean print = true;

    /**
     * @param args
     */
    public static void main(String[] args) {
        String className = args[0];
        Integer methodId = Integer.parseInt(args[1]);
        String domainName = args[2];
        String symbolicOn = args[3];
        home = args[4];
        domainPath = home + domainPath;
        resultsPath = home + resultsPath;

        print = args[5].equals("yes");

        timeDataFile = new File(resultsPath + "timeData");

        try {
            analysisType = domainName.split("\\.")[0] + "_" + symbolicOn;
            new StartAnalysisKestrel(className, methodId, domainName, symbolicOn);
            G.reset();
            System.gc();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // The class should have static fields for the files to write to
    // className_sY_domainName
    // where className is the name of class being analyzed (with all its methods)
    // domainName is the domain that the analysis uses
    // sY means using symbolic helper state and sN means not using symbolic helper
    // state.

    public static String home;
    public static FileWriter fileToWrite;
    private static String domainPath = "ExperimentData/domains/";
    private static String resultsPath = "ExperimentData/results/";
    public static File timeDataFile;
    public static String analysisType;

    // each instance should open/close that file

    public StartAnalysisKestrel(String className, int methodId, String domainFile, String symbolicHelper)
            throws IOException {
        // instantiate the list of domains from a file
        String domainDescription = domainPath + domainFile;
        DomainReader dr = new DomainReader(domainDescription);
        List<Domain> domain = dr.getReadDomains();
        System.out.println(domain);

        // create the file to write to
        fileToWrite = new FileWriter(
                resultsPath + className + "_" + methodId + "_" + symbolicHelper + "_" + domainFile);
        boolean symbolicOn = symbolicHelper.equals("sY");

        String[] sootArgs = { "-f", "n", className };
        PackManager.v().getPack("jtp")
                .add(new Transform("jtp.disjoint", new ValueTransformer(domain, methodId, symbolicOn)));
        // adding runtime to the path
        //
        // System.out.println(Scene.v().getSootClassPath() + " " +
        // System.getProperty("java.class.path"));
        Scene.v().setSootClassPath(Scene.v().getSootClassPath() + ":" + System.getProperty("java.class.path")
                + ":" + System.getProperty("sun.boot.class.path"));
        // run soot
        soot.Main.main(sootArgs);
        fileToWrite.close();
    }

}
