package driver;


import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import original.analysis.ReachingDefinitions;
import soot.Body;
import soot.NormalUnitPrinter;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Timers;
import soot.Unit;
import soot.UnitPrinter;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import util.Variables;
import soot.jimple.internal.*;

public class StartReachingDefinitions {
	private static String resultsPath = "ScratchData/resultsRD/";
	
	public static void main(String[] args){
	
		
		String className = "test.Example1M";
		int methodId = 4;
		
		String fileName = resultsPath+"/invariants/"+className+"_"+methodId;
		//new StartAnalysis(className, domainName, symbolicOn, condition, methodId);
		String[] sootArgs = {"-f", "n", className};
		System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));
		SootClass sClass = Scene.v().loadClassAndSupport(className);		
		sClass.setApplicationClass();
		Scene.v().loadNecessaryClasses();
		
		
		//we are analyzing sClass
		SootMethod m = sClass.getMethods().get(methodId);

		Body b = m.retrieveActiveBody();

		System.out.println("=======================================");			
		System.out.println(m.toString());
		System.out.println(methodId);

		UnitGraph g = new ExceptionalUnitGraph(b);
		Variables myVariables = new Variables(g);
		myVariables.makeVariables();
		myVariables.renameLocals();
		ReachingDefinitions rdf = new ReachingDefinitions(g);
		Iterator gIt = g.iterator();
		String timeData = "f\t\t" + rdf.getTime()+"\n";
		System.out.println(timeData);
		String timeDataFile = resultsPath+"/time/"+className+"_"+methodId;
		FileWriter writer;
		try{
			//write the time data first
			RandomAccessFile rf = new RandomAccessFile(timeDataFile, "rwd");
			FileChannel fileChannel = rf.getChannel();
			FileLock lock = fileChannel.lock();
			fileChannel.position(fileChannel.size());
			fileChannel.write(Charset.defaultCharset().encode(CharBuffer.wrap(timeData)));
			fileChannel.force(false);
			lock.release();
			fileChannel.close();
			rf.close();
			//					String path = "/Users/erickeefe/Documents/workspace/Conditional_DFA/src/automatedTesting/";
			//					String fName = aClass + "_" + methodStop + "_" + branchInfo;
			//					String name = path + fName;
			writer = new FileWriter(new File(fileName));
			while (gIt.hasNext()){
				Unit u = (Unit) gIt.next();

				//System.out.println(u);
				//				
				//				String[] flowTypes = {"before", "fall", "branch"};
				//				
				//				for (int i = 0; i < 1; i++){
				//					
				String output = rdf.getReachableExpressions(u, "before") + "\n";
				//					
				//					UnitPrinter up = new NormalUnitPrinter(b);
				//					up.setIndent("");
				//					
				//					System.out.println("---------------------------------------");			
				//					u.toString(up);			
				//					System.out.println(up.output());
				//					System.out.println("Reachable Defitions " + flowTypes[i] + " this unit are:");
				//					String sep = "";
				//					Iterator flowIt = flowList.iterator();
				//					while(flowIt.hasNext()){
				//						String varReachables = (String) flowIt.next();
				//						System.out.println(sep);
				//						System.out.println(varReachables);
				//						sep = ", ";
				//					}
				//					System.out.println("---------------------------------------");
				//System.out.println(output);
				writer.write(output);

				//					
				//			    }
			}	
			writer.close();
			System.out.println("=======================================");
		}catch(Exception e){
			System.out.println("Error" + e);
		}
		System.out.flush();
		
	}
	
}


