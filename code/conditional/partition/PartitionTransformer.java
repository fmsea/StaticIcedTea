package conditional.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PartitionTransformer extends BodyTransformer {

	@Override
	protected void internalTransform(Body b, String arg1, Map<String, String> arg2) {
		String methodName = b.getMethod().getName();

		if(methodName.equals("soogood")){
			UnitGraph gr = new ExceptionalUnitGraph(b);
			//get a dominator tree
			SimpleDominatorsFinder<Unit> dom = new SimpleDominatorsFinder<Unit>(gr);
			
			//now we need to find all conditional statements
			//and decide which true/false branch has
			//the same number of dominators.
			int countOfCond = 1;
			//total number of stmt:
			double total = b.getUnits().size();
			//keep track of potential cond stmt
			List<IfStmt> condToSplit = new ArrayList<IfStmt>();
			Map<IfStmt, Integer> ifToInt = new HashMap<IfStmt, Integer>();
			for(Unit u : b.getUnits()){
				//check if u is a conditional statement
				if(u instanceof IfStmt){
					List<Unit> succ = gr.getSuccsOf(u);
					//System.out.println(countOfCond + " has " + succ);
					int[] btf = {0,0};
					//get the number of items
					//they dominate
					for(int i=0; i < succ.size(); i++){
					//for(Unit dm : succ){
						Unit dm = succ.get(i);
						int bsize = 0;
					for(Unit v : b.getUnits()){
							if(dom.isDominatedBy(v, dm)){
								bsize++;
							}
						}
					btf[i] = (int) Math.ceil(bsize/total*100);
					System.out.println(dm + " -> " + bsize + " " + " " + btf[i]);
					}// end iteration for each successor
					//compare by how much they are different
					int diff = Math.abs(btf[0] - btf[1]);
					int max = Math.max(btf[0], btf[1]);
					System.out.println("diff " + diff + " max " + max);
					if(diff < 50 && max >1){
						System.out.println(countOfCond+"t" + countOfCond + "f");
						//we need to add this cond to the map
						condToSplit.add((IfStmt)u);
						ifToInt.put((IfStmt)u, countOfCond);
					}
					countOfCond++;
				}
			}//end for units
		//CFGToDotGraph cfgToDot = new CFGToDotGraph(); 
		//DotGraph dotGraph = cfgToDot.drawCFG(gr, b);
		//dotGraph.plot("main22.dot");
			System.out.println(condToSplit);
//			Set<List<Unit>> ret = new HashSet<List<Unit>>();
//			for(Unit succ: gr.getSuccsOf(condToSplit.get(0))){
//				List<Unit> b0 = new ArrayList<Unit>();
//				b0.add(succ);
//				ret.add(b0);
//			}
//			
//			buildPath(condToSplit, dom, 1, ret, gr);
			AbstractedCFG aCFG = new AbstractedCFG();
			//get the first condstimt
			IfStmt first = condToSplit.get(0);
			Node str = aCFG.addStart(ifToInt.get(first).toString());
			List<Unit> firstSucc = gr.getSuccsOf(first);
			boolean branch = true;
			for(Unit u : firstSucc){
				buildACFG(aCFG, gr, ifToInt, condToSplit, u, str, branch);
				branch = !branch;
			}
			System.out.println(aCFG.toString());
			//print out the paths
			//System.out.println(aCFG.getPaths());
			List<String> l = new ArrayList<String>();
			l.addAll(aCFG.getPaths());
			Collections.sort(l);
			for(String s : l){
				System.out.println(s);
			}
	}//end if correct method name
	}
	
	private void buildACFG(AbstractedCFG aCFG, UnitGraph gr, Map<IfStmt, Integer> ifToInt, List<IfStmt> condList, 
			Unit current, Node from, boolean on){
		//end on the return statement
		if(current instanceof JReturnStmt){
			//it should be the end node
			Node end = null;
			if(aCFG.contains("end")){
				end = aCFG.findNode("end");
			} else {
		
				end = aCFG.addEnd("end");
			}
			aCFG.add(from, end, on);
		} else {
			//continue the recursion
			if(condList.contains(current)){
				String id = ifToInt.get(current).toString();
				//check if this cond statement has been explored
				Node to = null;
				if(aCFG.contains(id)){
					to = aCFG.findNode(id);
				} else {
					//create a node for it
					to = aCFG.addNode(id);
					boolean branch = true;
					for(Unit u : gr.getSuccsOf(current)){
						buildACFG(aCFG, gr, ifToInt, condList, u, to, branch);
						branch = !branch;
					}
				} 
				//create a transition
				aCFG.add(from, to, on);
			} else {
				//do the same without creating a node and a transiton
				for(Unit u : gr.getSuccsOf(current)){
					buildACFG(aCFG, gr, ifToInt, condList, u, from, on);
				}
			}
		}
	}
	//recursively build the conditions
	private void buildPath(List<IfStmt> condList,SimpleDominatorsFinder<Unit> dom, int index,  Set<List<Unit>> paths, UnitGraph gr){
		if(index >= condList.size()){
			return;
		} else {
			//get the next ifStmt to process
			IfStmt ifS = condList.get(index);
			//if iFs
		index++;
		buildPath(condList, dom, index, paths, gr);
		}
	}
	

}
