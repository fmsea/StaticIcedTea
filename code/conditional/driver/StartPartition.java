package conditional.driver;

import conditional.analysis.PartialTransformer;
import conditional.partition.PartitionTransformer;
import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class StartPartition {
	
	static public void main(String[] args){
		String className = "test.LargePC";
		String[] sootArgs = {"-f", "n", className};
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));
		PackManager.v().getPack("jtp").
		add(new Transform("jtp.disjoint", new PartitionTransformer()));
		//run soot to resovle all methods/bodies
		soot.Main.main(sootArgs);
		//load and support the classes
//		SootClass cl = Scene.v().getSootClass(className);
//		SootMethod md = cl.getMethodByName("main2");
//		Body b = md.getActiveBody();
//		
	}

}
