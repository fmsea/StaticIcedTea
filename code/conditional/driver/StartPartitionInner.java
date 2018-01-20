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

public class StartPartitionInner {
	static public void main(String[] args){
		String className = "test.Example1M";
		int methodId = Integer.parseInt("1");

		Scene.v().setSootClassPath(Scene.v().getSootClassPath()+":"+System.getProperty("java.class.path") 
		+ ":" + System.getProperty("sun.boot.class.path"));

	
		System.out.println("class " + className + " methodId " + methodId);
		SootClass sClass = Scene.v().loadClassAndSupport(className);
		sClass.setApplicationClass();
		Scene.v().loadNecessaryClasses();
		SootMethod m = sClass.getMethods().get(methodId);
		Body b = m.retrieveActiveBody();
		System.out.println(b);
		PartitionGenerator pg = new PartitionGenerator(b);
		AbstractedCFG acfg = pg.getAbstractedCFG(3, 60);
		List<String> l = new ArrayList<String>();
		l.addAll(acfg.getPaths());
		Collections.sort(l);
		System.out.println("Paths " + l.size());
						for(String s : l){
							System.out.println(s);
						}
		//write the graph to the file
//		String fileName = b.getMethod().getDeclaringClass().getName()+"_"+methodId+loops+ ".txt";
//		aCFG.writePaths(fileName);
//		aCFG.writeToFile(fileName);
	}

}
