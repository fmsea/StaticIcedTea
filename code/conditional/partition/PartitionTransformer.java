package conditional.partition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import soot.jimple.internal.JThrowStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
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
	Set<IfStmt> condLoops;
	private int methodId = 0;
	boolean skipLoops;
	int percOfCode = 3;
	int branchDiff = 60;

	public PartitionTransformer(String methodId, boolean loops) {
		this.methodId = Integer.parseInt(methodId);
		this.skipLoops = loops;
	}

	@Override
	protected void internalTransform(Body b, String arg1, Map<String, String> arg2) {
		String methodName = b.getMethod().getName();

		//if(methodName.equals("getNextBits")){
		if(b.getMethod().getDeclaringClass().getMethods().get(methodId).equals(b.getMethod())){
			System.out.println("M " + b.getMethod().getName());
			UnitGraph gr = new ExceptionalUnitGraph(b);
			//get a dominator tree
			dom = new MHGDominatorsFinder<Unit>(gr);
			postdom = new MHGPostDominatorsFinder<Unit>(gr);
			LoopNestTree loopTree = new LoopNestTree(b);
			condLoops = new HashSet<IfStmt>();
			Iterator<Loop> lIt = loopTree.iterator();
			while(lIt.hasNext()){
				Loop l = lIt.next();
				System.out.println("l " + l.getHead() + " " + l.getLoopExits());
				Unit u = l.getHead();
				//need to find its if stmt
				while(!(u instanceof IfStmt)){
					u = gr.getSuccsOf(u).get(0);
				}
				condLoops.add((IfStmt)u);
			}
			System.out.println("loops " + condLoops);
			//System.exit(2);
			//now we need to find all conditional statements
			//and decide which true/false branch has
			//the same number of dominators.
			int countOfCond = 1;
			//total number of stmt:
			double total = b.getUnits().size();
			//keep track of potential cond stmt
			List<IfStmt> condToSplit = new ArrayList<IfStmt>();
			Set<IfStmt> allIfStmt = new HashSet<IfStmt>();

			for(Unit u : b.getUnits()){
				//check if u is a conditional statement
				if(u instanceof IfStmt){
					allIfStmt.add((IfStmt)u);
					List<Unit> succ = gr.getSuccsOf(u);
					//if u has only one successor then
					//definitely add
					if(succ.size() == 1 ){
						//add for sure
						System.out.println("single don't include" + u);
						//						condToSplit.add((IfStmt)u);
						//						ifToInt.put((IfStmt)u, countOfCond);
					} else if(!condLoops.contains(u)){
						//do regular check
						//if() s2 else s1
						Unit s1 = succ.get(0);
						Unit s2 = succ.get(1);
						System.out.println(u + " s2 " + s2 + " s1 " + s1);
						//we need to make sure that s1 is not
						//a post dominator of s2 and vice versa
						//that would ensure "if" with "else" part
						//that is the place where we want to split
						//for now we will leave cond splitting inside 
						//the loop to see if it causes any imprecisions.
						if(!postdom.isDominatedBy(s2, s1) && !postdom.isDominatedBy(s1, s2)){
							System.out.println("in "+countOfCond + " " + u);
							System.out.println(countOfCond + " has " + succ);
							int[] btf = {0,0,0};
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
								//btf[i] = (int) Math.ceil(bsize/total*100);
								btf[i] = bsize;
								System.out.println(dm + " -> " + bsize + " " + " " + btf[i]);
							}// end iteration for each successor
							//compare by how much they are different
							double ratio = 100.0/total;
							int diff = (int) (Math.abs(btf[0] - btf[1]) * ratio);
							int max = (int) (Math.max(btf[0], btf[1]) * ratio);
							System.out.println("diff " + diff + " max " + max);
							if(diff <= branchDiff && max >= percOfCode){
								System.out.println(countOfCond+"t" + countOfCond + "f");
								//we need to add this cond to the map
								condToSplit.add((IfStmt)u);
							}

							//						} else if (postdom.isDominatedBy(s1, u) || postdom.isDominatedBy(s2, u)) {
							//							System.out.println("loop if " + u);
							//							//add it the list
							//							condLoops.add((IfStmt)u);
						} else {
							System.out.println("not "+countOfCond + " " + u);
						} //end checking the loop and single branches
					}//end if u instanceof IfStmt
					ifToInt.put((IfStmt)u, countOfCond);
					countOfCond++;

				}//end of if cond
			}//end for units

			System.out.println("Orig cond " + condToSplit);
			
			if(this.skipLoops){
				System.out.println("removing loops " + condLoops);
				//remove those for which the loops is dominator
				Set<IfStmt> removeIf = new HashSet<IfStmt>();
				for(IfStmt cond : condToSplit){
					List<Unit> ch = gr.getSuccsOf(cond);
					for(IfStmt loop : condLoops){
						//it is inside the loop that if the loop's if both
						//dominate the cond and postdominate at least
						//one of the children of the cond, because
						//another child can just "break" from the loop
						//or return a value
						if(dom.isDominatedBy(cond, loop)&& (postdom.isDominatedBy(ch.get(0), loop)
								|| postdom.isDominatedBy(ch.get(1),loop))){
							removeIf.add(cond);
						}
					}
				}
				condToSplit.removeAll(removeIf);
				}
			//remove all ifstmt associated with loops
			allIfStmt.removeAll(condLoops);
			//add to condToSplit all those
			//conditions that lead to condToSplit
			boolean changed = true;
			while(changed){
				changed = false;
				Set<IfStmt> add = new HashSet<IfStmt>();
				for(IfStmt cond : condToSplit){
					for(IfStmt dep : allIfStmt){
						//dep must not be already in the set and dominate cond
						if(!condToSplit.contains(dep) && dom.isDominatedBy(cond, dep)){
							List<Unit> succOfDep = gr.getSuccsOf(dep);
							if(succOfDep.size() >1){
								//do more checks
								Unit succ1 = succOfDep.get(0);
								Unit succ2 = succOfDep.get(1);
								if(dom.isDominatedBy(cond, succ1) || dom.isDominatedBy(cond, succ2)){
									//cannot both dominate, if both dominate then
									//a) either one branch cannot be empty
									if(!postdom.isDominatedBy(succ1, succ2) && 
											!postdom.isDominatedBy(succ2, succ1)){
										//there are two paths.
										changed = true;
										add.add(dep);
									}
									
								}
							} //do not include if only one outcome
						}//end checking of dep in condToSplit already
					}
				}// end for condToSplit
				//now add to condToSplit and start over again.
				condToSplit.addAll(add);
			}
			
			CFGToDotGraph cfgToDot = new CFGToDotGraph(); 
			DotGraph dotGraph = cfgToDot.drawCFG(gr, b);
			dotGraph.plot("bf1.dot");
			//			System.out.println(condToSplit);
			//			for(Entry<IfStmt, Integer> entry : ifToInt.entrySet()){
			//				System.out.println(entry.getValue() + "\t" + entry.getKey());
			//			}
			System.out.println(ifToInt.values());
			System.out.println("Cond " + condToSplit);
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
				boolean branch = false;// start with true or with false? I think the first is falls through, so false
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
				System.out.println("Paths " + l.size() + " " + condLoops.size());
				//				for(String s : l){
				//					System.out.println(s);
				//				}
				//write the graph to the file
				String loops = skipLoops?"":"_L";
				String fileName = b.getMethod().getDeclaringClass().getName()+"_"+methodId+loops+ ".txt";
				aCFG.writePaths(fileName);
				aCFG.writeToFile(fileName);
			}//if condtoSplit != empty
			else {
				System.out.println("Paths " + 0 + " " + condLoops.size());
			}
		}//end if correct method name
	}

	private void buildACFG(AbstractedCFG aCFG, UnitGraph gr, List<IfStmt> condList, 
			Unit current, Node from, boolean on, Set<Unit> seen){
//		if(from != null){
//			System.out.println(from.getName() + " on " + on + " curr " + current + " " + current.getClass());
//		} else {
//			System.out.println(from + " on " + on + " curr " + current + " " + current.getClass());
//		}
		//System.out.println("Seen " + seen);
		//end on the return statement
		if(gr.getTails().contains(current)){
		//if(current instanceof JReturnStmt || current instanceof JThrowStmt || current instanceof ....){
			//it should be the end node
			Node end = null;
			if(aCFG.contains("end")){
				end = aCFG.findNode("end");
			} else {

				end = aCFG.addEnd("end");
			}
			if(from != null){
				//System.out.println("return added to " + from.getName());
				aCFG.add(from, end, on);
			}
		} else if (seen.contains(current)){
			//when we see cond of the loop again
			//we need to explore its false branch
			Unit next = gr.getSuccsOf(current).get(0);//fall through
			//System.out.println("seen " + current + " next " + next);
//			for(Unit s : gr.getSuccsOf(current)){
//				if(!postdom.isDominatedBy(s, current)){
					//explore the false branch now
					buildACFG(aCFG, gr, condList, next, from, on, seen);
					//break;
//				}
//			}
		} else {
			//continue the recursion
			if(condList.contains(current)){
				String id = ifToInt.get(current).toString();
				//check if this cond statement has been explored
				Node to = null;
				//already explored and not the first one
				if(aCFG.contains(id)){
					to = aCFG.findNode(id);
					//System.out.println("to1 " + to.getName());
				} else {
					//create a node for it
					if(from == null){
						//means that will be the start one
						to = aCFG.addStart(id);
					} else {
						//otherwise a regular node
						to = aCFG.addNode(id);
					}
					//System.out.println("to2 " + to.getName());
					boolean branch = false; // the first is falls through and the second is branchout
					//System.out.println("succ " + gr.getSuccsOf(current).size());
					for(Unit u : gr.getSuccsOf(current)){
						//System.out.println("b " + branch + " " + u);
						buildACFG(aCFG, gr, condList, u, to, branch,  seen);
						branch = !branch;
					}
				} 
				//create a transition
				if(from != null){
					//System.out.println("from " + from.getName() + " to " + to.getName() + " on " + on);
					aCFG.add(from, to, on);
				}
			} else {
				//instead of cond stmtm in seen we need
				//to add the branched statments
				//do the same without creating a node and a transition
				///List<Unit> newseen = new <Unit>();
				//				Unit loop = null;
				//				if(current instanceof IfStmt){
				//					//check first postdom relation
				//					//of its 
				//					//get its children
				//					for(Unit s : gr.getSuccsOf(current)){
				//						if(postdom.isDominatedBy(s, current)){
				//							loop = s;
				//							break;
				//						}
				//					}
				//
				//
				//				}
				/*if(current instanceof IfStmt){
						seen.add(u);
						//explore its children
						for(Unit uu : gr.getSuccsOf(u)){
							buildACFG(aCFG, gr, ifToInt, condList, uu, from, on, seen);
						}
				 */
				if(condLoops.contains(current)){
					//found the loop explore its true branch first which is loop
					seen.add(current);
					//current should be the "true" branch of the cond stmt, i.e.,
					//that allows getting inside the loop.
					Unit next = gr.getSuccsOf(current).get(1);//branch out
					//System.out.println("loop " + current + " next " + next);
					//System.out.println(next);
					buildACFG(aCFG, gr, condList, next, from, on, seen);
					//clear seen
					seen.remove(current);
				} else {
					for(Unit u : gr.getSuccsOf(current)){
						//System.out.println("u " + u + " curr " + current);
						try{
							buildACFG(aCFG, gr,condList, u, from, on, seen);
							//System.out.println("done with " + u + " curr " + current + " on " + on);
						} catch(StackOverflowError e) {
							System.err.println("stack overflow!");
							System.exit(2);
						}
					}
				}
			}
		}
	}
	//recursively build the conditions
	//	private void buildPath(List<IfStmt> condList,SimpleDominatorsFinder<Unit> dom, int index,  Set<List<Unit>> paths, UnitGraph gr){
	//		if(index >= condList.size()){
	//			return;
	//		} else {
	//			//get the next ifStmt to process
	//			IfStmt ifS = condList.get(index);
	//			//if iFs
	//		index++;
	//		buildPath(condList, dom, index, paths, gr);
	//		}
	//	}


}
