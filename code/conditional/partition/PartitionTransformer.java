package conditional.partition;

import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PartitionTransformer extends BodyTransformer {

	@Override
	protected void internalTransform(Body b, String arg1, Map<String, String> arg2) {
		String methodName = b.getMethod().getName();

		if(methodName.equals("main2")){
			UnitGraph gr = new ExceptionalUnitGraph(b);
			//get a dominator tree
			SimpleDominatorsFinder<Unit> dom = new SimpleDominatorsFinder<Unit>(gr);
			
			//now we need to find all conditional statements
			//and decide which true/false branch has
			//the same number of dominators.
			int countOfCond = 1;
			for(Unit u : b.getUnits()){
				//check if u is a conditional statement
				if(u instanceof IfStmt){
					List<Unit> succ = gr.getSuccsOf(u);
					System.out.println(countOfCond + " has " + succ);
					//get the number of items
					//they dominate
					for(Unit dm : succ){
						int bsize = 0;
					for(Unit v : b.getUnits()){
							if(dom.isDominatedBy(v, dm)){
								bsize++;
							}
						}
					System.out.println(dm + " -> " + bsize);
					}
					countOfCond++;
				}
			}
		
		//CFGToDotGraph cfgToDot = new CFGToDotGraph();
		//DotGraph dotGraph = cfgToDot.drawCFG(gr, b);
		//dotGraph.plot("main22.dot");

	}
	}

}
