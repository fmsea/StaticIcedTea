package conditional.driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import conditional.partition.AbstractedCFG;
import conditional.partition.PartitionGenerator;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
/**
 * Main method for the partition generator
 * Takes five parameters:
 * 1. The path to the output
 * 2. className
 * 3. method ID
 * 4. percOfCode that should be in at least one branch
 * @author elenasherman
 *
 */
public class StartPartitionInner {
	static public void main(String[] args){

		String outPath = "./ConditionalTACAS/conditions/";
		String className = "test.BallonFactory";
		int methodId = Integer.parseInt("1");
		int percOfCode = 2;
		int diff = 60;

		if(args.length == 5 ) {
			//parse the input
			outPath = args[0];
			className = args[1];
			methodId = Integer.parseInt(args[2]);
			percOfCode = Integer.parseInt(args[3]);
			diff = Integer.parseInt(args[4]);
		}


		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));

		System.out.println(Scene.v().getSootClassPath());

		System.out.println("class " + className + " methodId " + methodId);
		SootClass sClass = Scene.v().loadClassAndSupport(className);
		sClass.setApplicationClass();
		Scene.v().loadNecessaryClasses();
		SootMethod m = sClass.getMethods().get(methodId);
		Body b = m.retrieveActiveBody();
		System.out.println(m.getName());
		System.out.println(b);
		PartitionGenerator pg = new PartitionGenerator(b);
		AbstractedCFG acfg = pg.getAbstractedCFG(percOfCode, diff);
		if(acfg != null){
			List<String> l = new ArrayList<String>();
			l.addAll(acfg.getPaths());
			Collections.sort(l);
			System.out.println("Paths " + l.size());
//			for(String s : l){
//				System.out.println(s);
//			}
			//write the graph to the file
			String fileName = outPath+b.getMethod().getDeclaringClass().getName()+"_"+methodId+ ".txt";
			acfg.writeToFile(fileName);
			fileName = outPath+"paths/" + b.getMethod().getDeclaringClass().getName()+"_"+methodId+ ".txt";
			acfg.writePaths(fileName);
		} else {
			System.out.println("No partitions");
		}
	}

}
