package conditional.driver;

import conditional.partition.PartitionTransformer;
import soot.PackManager;
import soot.Scene;
import soot.Transform;

public class StartPartition {
	static public void main(String[] args){
		String className = "test.TIFFFaxDecoder";
		String[] sootArgs = {"-f", "n", className};
		String methodId = "11";
		boolean skipLoops = true;
		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));
		PackManager.v().getPack("jtp").
		add(new Transform("jtp.disjoint", new PartitionTransformer(methodId, skipLoops )));
		//run soot to resovle all methods/bodies
		soot.Main.main(sootArgs);
		//load and support the classes
//		SootClass cl = Scene.v().getSootClass(className);
//		SootMethod md = cl.getMethodByName("main2");
//		Body b = md.getActiveBody();
//		
	}

}
