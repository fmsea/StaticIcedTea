package driver;


import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Iterator;

import abstractinterp.scalar.Numerical;
import original.analysis.ReachingDefinitions;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import util.Variables;

/**
 * Driver for full versions of reaching definitions analysis
 * args[0] is the path where to write the ouptput,  
 * which are time and the computed invariants.
 * Make sure to have two folder on this paths: invariants and time.
 * args[1] is the class name
 * args[2] is the method id in this class
 * args[3] is whether to write computed invariants (only set to no to compute average run)
 * @author elenasherman
 *
 */
public class StartNumerical {
	private static String resultsPath = "ScratchData/resultsRD/";

	public static void main(String[] args){
		String className = "test.Example1M";
		int methodId = 6;
		boolean writeInv = true;
		if(args.length > 0){
			resultsPath = args[0];
			className =args[1];
			methodId = Integer.parseInt(args[2]);
			writeInv = args[3].equals("y");
		} 



		String fileName = resultsPath+"/invariants/"+className+"_"+methodId;
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));
		SootClass sClass = Scene.v().loadClassAndSupport(className);		
		sClass.setApplicationClass();
		Scene.v().loadNecessaryClasses();


		//we are analyzing sClass
		SootMethod m = sClass.getMethods().get(methodId);

		Body b = m.retrieveActiveBody();

		System.out.println("=======================================");			
		System.out.println(m.toString() + " " + writeInv);;
	
		Numerical num = new Numerical(b, 2);
		num.runAnalysis();
		num.report();

	}

}


