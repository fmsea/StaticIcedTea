package conditional.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import disjoint.domain.Domain;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PartialTransformer extends BodyTransformer {
	List<Domain> domains;
	boolean symbolicOn;
	Map<IfStmt, Boolean> exclude;
	String[] condition;
	
	
/**
 * Initializes the analysis with the parameters.
 * @param domain - abstract domain
 * @param symbolicOn - will be always yes for now
 * @param conditions - the set of branches to be excluded.
 */
	public PartialTransformer(List<Domain> domains, boolean symbolicOn, String conditions) {
		super();
		this.domains = domains;
		this.symbolicOn = symbolicOn;
		this.condition = conditions.split(",");
		this.exclude = new HashMap<IfStmt, Boolean>();
	}

	@Override
	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
		//get the method body
		String methodName = b.getMethod().getName();
		//simple filtering of a relevant method for tcas.
		if(methodName.equals("main2")){
			//construct dot
//			CFGToDotGraph cfgToDot = new CFGToDotGraph();
//			DotGraph dotGraph = cfgToDot.drawCFG(new ExceptionalUnitGraph(b),b);
//			dotGraph.plot("main2.dot");
//			//get BFS nodes order?
			int countOfCond = 0;
			for(Unit u : b.getUnits()){
				//check if u is a conditional statement
				if(u instanceof IfStmt){
					countOfCond++;
					//System.out.println(countOfCond +"\t" + u);
					//check if this condition should be excluded
					//split on t or f
					for(String c : condition){
						if(c.endsWith("f")){
							String id = c.split("f")[0];
							if(id.equals(Integer.toString(countOfCond))){
								//add to the map if ids match
								exclude.put((IfStmt)u, false);
							}
						} else if (c.endsWith("t")){
							String id = c.split("t")[0];
							if(id.equals(Integer.toString(countOfCond))){
								//add to the map if ids match
								exclude.put((IfStmt)u, true);
							}
						} else {
							if(!c.isEmpty()){
								System.out.println("unknown codition " + c);
							}
						}
					}
					
				}
				
			}
			System.out.println("Exclude map " + exclude);
			//start the analysis
			PartialAnalysis pa = new PartialAnalysis(new ExceptionalUnitGraph(b), domains, exclude);
			pa.start();
			pa.report();
		}

	}

}
