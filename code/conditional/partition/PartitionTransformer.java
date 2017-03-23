package conditional.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.internal.JReturnStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.SimpleDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PartitionTransformer extends BodyTransformer {
	private Map<IfStmt, Integer> ifToInt = new HashMap<IfStmt, Integer>();
	MHGDominatorsFinder<Unit> dom; 
	MHGPostDominatorsFinder<Unit> postdom;

	@Override
	protected void internalTransform(Body b, String arg1, Map<String, String> arg2) {
		String methodName = b.getMethod().getName();

		if(methodName.equals("getNextBits")){
			UnitGraph gr = new ExceptionalUnitGraph(b);
			//get a dominator tree
			dom = new MHGDominatorsFinder<Unit>(gr);
			postdom = new MHGPostDominatorsFinder<Unit>(gr);
			
			//now we need to find all conditional statements
			//and decide which true/false branch has
			//the same number of dominators.
			int countOfCond = 1;
			//total number of stmt:
			double total = b.getUnits().size();
			//keep track of potential cond stmt
			List<IfStmt> condToSplit = new ArrayList<IfStmt>();
			
			for(Unit u : b.getUnits()){
				//check if u is a conditional statement
				if(u instanceof IfStmt){
					List<Unit> succ = gr.getSuccsOf(u);
					//if u has only one successor then
					//definitely add
					if(succ.size() == 1 ){
						//add for sure
						System.out.println("single don't include" + u);
//						condToSplit.add((IfStmt)u);
//						ifToInt.put((IfStmt)u, countOfCond);
					} else {
						//do regular check
					//if() s2 else s1
					Unit s1 = succ.get(0);
					Unit s2 = succ.get(1);
					System.out.println(u + " s2 " + s2 + " s1 " + s1);
					//we need to make sure that s1 is not
					//a post dominator of s2 and vice versa
					//that would ensure "if" with "else" part
					//that is the place where we want to slip
					//for now we will leave cond splitting inside 
					//the loop to see if it causes any imprecisions.
					if(!postdom.isDominatedBy(s2, s1) && !postdom.isDominatedBy(s1, s2)){
						System.out.println("in "+countOfCond + " " + u);
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
							//System.out.println(dm + " -> " + bsize + " " + " " + btf[i]);
						}// end iteration for each successor
						//compare by how much they are different
						int diff = Math.abs(btf[0] - btf[1]);
						int max = Math.max(btf[0], btf[1]);
						System.out.println("diff " + diff + " max " + max);
						if(diff < 150 && max >1){
							System.out.println(countOfCond+"t" + countOfCond + "f");
							//we need to add this cond to the map
							condToSplit.add((IfStmt)u);
							ifToInt.put((IfStmt)u, countOfCond);
						}
					
					} else {
						System.out.println("not "+countOfCond + " " + u);
					} //end checking the loop
					}
					countOfCond++;
				}//end of if cond
			}//end for units
		CFGToDotGraph cfgToDot = new CFGToDotGraph(); 
		DotGraph dotGraph = cfgToDot.drawCFG(gr, b);
		dotGraph.plot("qrcode.dot");
			System.out.println(condToSplit);
			for(Entry<IfStmt, Integer> entry : ifToInt.entrySet()){
				System.out.println(entry.getValue() + "\t" + entry.getKey());
			}
			System.out.println(ifToInt.values());
//			Set<List<Unit>> ret = new HashSet<List<Unit>>();
//			for(Unit succ: gr.getSuccsOf(condToSplit.get(0))){
//				List<Unit> b0 = new ArrayList<Unit>();
//				b0.add(succ);
//				ret.add(b0);
//			}
//			
//			buildPath(condToSplit, dom, 1, ret, gr);
			if(!condToSplit.isEmpty()){
				AbstractedCFG aCFG = new AbstractedCFG();
				//creae the start node;
				//get the first condstimt
				IfStmt first = condToSplit.get(0);
				//Node str = aCFG.addStart(ifToInt.get(first).toString());
				//Node str = aCFG.addStart("start");
				//List<Unit> firstSucc = gr.getSuccsOf(first);
				List<Unit> firstSucc = gr.getHeads();
				Set<Unit> seen = new HashSet<Unit>();//for loops
				boolean branch = true;
				for(Unit u : firstSucc){
					buildACFG(aCFG, gr, condToSplit, u, null, branch, seen);
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
			}
	}//end if correct method name
	}
	
	private void buildACFG(AbstractedCFG aCFG, UnitGraph gr, List<IfStmt> condList, 
			Unit current, Node from, boolean on, Set<Unit> seen){
		if(from != null){
			System.out.println(from.getName() + " on " + on + " curr " + current);
		}
		System.out.println("Seen " + seen);
		//end on the return statement
		if(current instanceof JReturnStmt){
			//it should be the end node
			Node end = null;
			if(aCFG.contains("end")){
				end = aCFG.findNode("end");
			} else {
		
				end = aCFG.addEnd("end");
			}
			if(from != null){
				System.out.println("return added to " + from.getName());
				aCFG.add(from, end, on);
			}
		} else if (seen.contains(current)){
			//when we see cond of the loop again
			//we need to explore its false branch
			for(Unit s : gr.getSuccsOf(current)){
				if(!postdom.isDominatedBy(s, current)){
					//explore the false branch now
					buildACFG(aCFG, gr, condList, s, from, on, seen);
					break;
				}
			}
		} else {
			//continue the recursion
			if(condList.contains(current)){
				String id = ifToInt.get(current).toString();
				//check if this cond statement has been explored
				Node to = null;
				//already explored and not the first one
				if(aCFG.contains(id)){
					to = aCFG.findNode(id);
					System.out.println("to1 " + to.getName());
				} else {
					//create a node for it
					if(from == null){
						//means that will be the start one
						to = aCFG.addStart(id);
					} else {
						//otherwise a regular node
						to = aCFG.addNode(id);
					}
					System.out.println("to2 " + to.getName());
					boolean branch = false; // the first is falls through and the second is branchout
					System.out.println("succ " + gr.getSuccsOf(current).size());
					for(Unit u : gr.getSuccsOf(current)){
						System.out.println(branch + " " + u);
						buildACFG(aCFG, gr, condList, u, to, branch,  seen);
						branch = !branch;
					}
				} 
				//create a transition
				if(from != null){
				System.out.println("from " + from.getName() + " to " + to.getName() + " on " + on);
				aCFG.add(from, to, on);
				}
			} else {
			    //instead of cond stmtm in seen we need
				//to add the branched statments
				//do the same without creating a node and a transition
				///List<Unit> newseen = new <Unit>();
				Unit loop = null;
				if(current instanceof IfStmt){
					//check first postdom relation
					//of its 
					//get its children
					for(Unit s : gr.getSuccsOf(current)){
						if(postdom.isDominatedBy(s, current)){
							loop = s;
							break;
						}
					}
					
					
				}
				/*if(current instanceof IfStmt){
						seen.add(u);
						//explore its children
						for(Unit uu : gr.getSuccsOf(u)){
							buildACFG(aCFG, gr, ifToInt, condList, uu, from, on, seen);
						}
						*/
				if(loop != null){
					//found the loop explore its true branch first which is loop
					seen.add(current);
					System.out.println("loop " + loop);
					buildACFG(aCFG, gr, condList, loop, from, on, seen);
					//clear seen
					seen.remove(current);
				} else {
				for(Unit u : gr.getSuccsOf(current)){
					
						buildACFG(aCFG, gr,condList, u, from, on, seen);
				}
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
