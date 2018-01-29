package driver;


import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pseudo.analysis.PseudoConditionalReachingDefinitions;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import util.Variables;

/**
 * Driver for running conditional analysis implemented
 * in flowthrough method
 * args[0] - the paths where to write the results
 * make sure it has invariants and time folders
 * args[1] - class name
 * args[2] - method id in that class
 * args[3] - the path to follow in the method's CFG
 * args[4] - whether to write invariant to the file (set to no to get time data)
 * @author elenasherman
 *
 */
public class StartPseudoConditionalReachingDefinitions {
	
	private static String resultsPath = "ScratchData/resultsRD/";
	
	public static void main(String[] args){
	
		
		String className = "test.Example1M";
		int methodId = 4;
		List<String> conditions = new ArrayList<String>();
		boolean writeInv = true;
		if(args.length > 0){
			resultsPath = args[0];
			className =args[1];
			methodId = Integer.parseInt(args[2]);
			conditions.add(args[3]);
			writeInv = args[4].equals("y");
		}
		
//		File file = new File("./ExperimentDataConditional/conditions/paths/"+className+"_"+methodId+".txt");
//		if(file.exists()){
//			try {
//				Scanner scan = new Scanner(file);
//				while(scan.hasNextLine()){
//					String ln = scan.nextLine();
//					if(!ln.isEmpty()){
//						conditions.add(ln);
//					}
//				}
//				scan.close();
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
		//conditions.add("1t,2t");
		//conditions.add("1t,2f");
		//conditions.add("1f,2t");
		//conditions.add("1f,2f");

		
		for(String condition : conditions){

			//System.out.println(condition);
			//StartAnalysis.condition = condition.isEmpty()?"":condition.replaceAll(",", "");
			String fileName = resultsPath+"/invariants/"+ className+"_"+methodId +"_"+condition.replaceAll(",", "")+"_c1";
			//new StartAnalysis(className, domainName, symbolicOn, condition, methodId);
			String[] sootArgs = {"-f", "n", className};
			//System.out.println(Scene.v().getSootClassPath() +  " " + System.getProperty("java.class.path"));
			Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
			+ ":" + System.getProperty("sun.boot.class.path"));
			SootClass sClass = Scene.v().loadClassAndSupport(className);		
			sClass.setApplicationClass();
			Scene.v().loadNecessaryClasses();
			//					G.reset();
			//					System.gc();


			//		String sootClp = Scene.v().getSootClassPath();
			//		System.out.println("sclp: " + sootClp);
			//		sootClp = sootClp.replace("/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/../Classes/classes.jar:/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home/jre/../Classes/ui.jar:", "");
			//		sootClp +=":/Users/erickeefe/Documents/workspace/Conditional_DFA/src";
			//		//sootClp += ":/Users/erickeefe/Documents/workspace/Velocity/src";
			//		sootClp += ":/Users/erickeefe/Documents/workspace/Velocity/src/java";
			//		//sootClp +=":/Users/erickeefe/Documents/workspace/Conditional_DFA/artifacts/test";
			//		//sootClp += "/Users/erickeefe/Documents/workspace/Java";
			//		System.out.println("sclp: " + sootClp);
			//		Scene.v().setSootClassPath(sootClp);
			//		SootClass sClass = Scene.v().loadClassAndSupport(args[0]);		
			//		sClass.setApplicationClass();
			//		Scene.v().loadNecessaryClasses();

			//we are analyzing sClass

			//int methodStop = 1;
			//String branchInfo = "2f9t";
			SootMethod m = sClass.getMethods().get(methodId);

			Body b = m.retrieveActiveBody();

			System.out.println("=======================================");			
			System.out.println(m.toString() + " " + methodId + " " + writeInv);

			UnitGraph g = new ExceptionalUnitGraph(b);
			Variables myVariables = new Variables(g);
			myVariables.makeVariables();
			myVariables.renameLocals();
			PseudoConditionalReachingDefinitions rdf = new PseudoConditionalReachingDefinitions(g, condition);
			//get the reaching definitions using our analysis

			//loop through each unit in our graph
			//and print out the list of reaching definitions before that unit
			String timeData = "c1\t" + condition.replaceAll(",", "")+"\t" + rdf.getTime() + "\n";
			System.out.println(timeData); 
			Iterator gIt = g.iterator();
			String timeDataFile = resultsPath+"/time/"+className+"_"+methodId;
			FileWriter writer;
			try{
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
				if(writeInv){
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
				}
			}catch(Exception e){
				System.out.println("Error" + e);
			}
			System.out.println("=======================================");
		}
		
	}
	
}


