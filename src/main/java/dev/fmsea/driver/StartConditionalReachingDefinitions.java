package dev.fmsea.driver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import dev.fmsea.conditional.analysis.ConditionalReachingDefinitions;
//import old.ReachingDefinitionsAnalysis;
//import test.ReachingDefinitions;
import soot.Body;
//import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Timers;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import dev.fmsea.util.Variables;

/**
 * RD analysis that uses the modification
 * to the topological ordering algorithm
 * args[0] - the paths where to write the results
 * make sure it has invariants and time folders
 * args[1] - class name
 * args[2] - method id in that class
 * args[3] - the path to follow in the method's CFG
 * args[4] - whether to write invariant to the file (set to no to get time data)
 * 
 * @author elenasherman
 *
 */

public class StartConditionalReachingDefinitions {

    private static String resultsPath = "ConditionalTACAS/resultsRD/";

    public static void main(String[] args) {

        String className = "test.Example1M";
        int methodId = 4;
        List<String> conditions = new ArrayList<String>();
        boolean writeInv = true;
        if (args.length > 0) {
            resultsPath = args[0];
            className = args[1];
            methodId = Integer.parseInt(args[2]);
            conditions.add(args[3]);
            writeInv = args[4].equals("y");
        }

        // conditions.clear();
        // conditions.add("1t,2t");
        // conditions.add("1t,2f");
        // conditions.add("1f,2t");
        // conditions.add("1f,2f");

        for (String condition : conditions) {

            System.out.println(condition);
            // StartAnalysis.condition = condition.isEmpty()?"":condition.replaceAll(",",
            // "");
            String fileName = resultsPath + "/invariants/" + className + "_" + methodId + "_"
                    + condition.replaceAll(",", "") + "_c2";
            Scene.v().setSootClassPath(Scene.v().getSootClassPath() + ":" + System.getProperty("java.class.path")
                    + ":" + System.getProperty("sun.boot.class.path"));
            SootClass sClass = Scene.v().loadClassAndSupport(className);
            sClass.setApplicationClass();
            Scene.v().loadNecessaryClasses();

            // we are analyzing sClass

            // int methodStop = 1;
            // String branchInfo = "2f9t";
            SootMethod m = sClass.getMethods().get(methodId);

            Body b = m.retrieveActiveBody();

            System.out.println("=======================================");
            System.out.println(m.toString() + " " + methodId + " " + writeInv);

            UnitGraph g = new ExceptionalUnitGraph(b);
            Variables myVariables = new Variables(g);
            myVariables.makeVariables();
            myVariables.renameLocals();
            ConditionalReachingDefinitions rdf = new ConditionalReachingDefinitions(g, condition);
            // get the reaching definitions using our analysis

            // loop through each unit in our graph
            // and print out the list of reaching definitions before that unit
            // String timeData = "c2\t" + condition.replaceAll(",", "")+"\t" + rdf.getTime()
            // + "\t" + Timers.v().totalFlowNodes + "\t"+ Timers.v().totalFlowComputations +
            // "\n";
            String timeData = "c2\t" + condition.replaceAll(",", "") + "\t" + rdf.getTime() + "\n";

            System.out.println(timeData);
            Iterator gIt = g.iterator();
            String timeDataFile = resultsPath + "/time/" + className + "_" + methodId;
            FileWriter writer;
            try {

                RandomAccessFile rf = new RandomAccessFile(timeDataFile, "rwd");
                FileChannel fileChannel = rf.getChannel();
                FileLock lock = fileChannel.lock();
                fileChannel.position(fileChannel.size());
                fileChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(timeData)));
                fileChannel.force(false);
                lock.release();
                fileChannel.close();
                rf.close();
                if (writeInv) {
                    writer = new FileWriter(new File(fileName));
                    while (gIt.hasNext()) {
                        Unit u = (Unit) gIt.next();
                        String output = rdf.getReachableExpressions(u, "before") + "\n";
                        writer.write(output);
                    }
                    writer.close();
                    System.out.println("=======================================");
                }
            } catch (Exception e) {
                System.out.println("Error" + e);
            }
            System.out.println("=======================================");
        }

    }

}
