package conditional.partition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.MHGPostDominatorsFinder;
import soot.toolkits.graph.UnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PartitionGenerator {
	
	private Body b;
	/* the set of cond stmt that we can partition - contains no loop ifs */
	private Set<IfStmt> ifSet;
	/* to use in the calculations */
	private UnitGraph gr;
	private MHGDominatorsFinder<Unit> dom; 
	private MHGPostDominatorsFinder<Unit> postdom;
	/* the branch set can be partitioned: has if/else parts, subset of ifSet */
	private Set<Branch> branchSet;
	/*any if stmt (inlc loops) to its order in the code, ifSet is a subset of map's KeySet */
	private Map<IfStmt, Integer> ifIndex;
	
	
	public PartitionGenerator(Body b){
		this.b = b;
		gr = new ExceptionalUnitGraph(b);
		//find all condition stmt that
		//are part of a loop so we will not
		//include them into the partition
		//that will include the loop stmt
		LoopNestTree loopTree = new LoopNestTree(b);
		Iterator<Loop> lit = loopTree.descendingIterator();
	    Set<Stmt> insideLoops = new HashSet<Stmt>();
		while (lit.hasNext()){
			Loop l = lit.next();
			for(Stmt s : l.getLoopStatements()){
				if(s instanceof IfStmt){
					insideLoops.add(s);
				}
			}
		}
		//System.out.println(insideLoops);
		//now iterate over all units
		//and add to ifSet those that are
		//not in insdieLoops
		//and has at least two successors
		//Populate the index map
		ifSet = new HashSet<IfStmt>();
		ifIndex = new HashMap<IfStmt, Integer>();
		int ifCount = 1;
		for(Unit u : b.getUnits()){
			if(u instanceof IfStmt){
				IfStmt ifS = (IfStmt)u;
				if(!insideLoops.contains(u) && gr.getSuccsOf(u).size() > 1){
					ifSet.add(ifS);
				}
				ifIndex.put(ifS, ifCount);
				ifCount++;
			}
		}
		
		//System.out.println(ifSet);
		//to use later
		postdom = new MHGPostDominatorsFinder<Unit>(gr);
		dom = new MHGDominatorsFinder<Unit>(gr);
		branchSet = new HashSet<Branch>();
		double ratio = 100.0/b.getUnits().size();
		
		//compute information for each stmt in ifSet
		//iterate over all ifStmt outside loops
				for(IfStmt ifS : ifSet){
					//System.out.println("ifS " + ifS);
					List<Unit> succ = gr.getSuccsOf(ifS);
					Unit s1 = succ.get(0);
					Unit s2 = succ.get(1);
					if(succ.size() > 2){
						System.err.print("More than two branch outcomes!");
					}
					/*
					 * 1: s1 is not a post dominator for s2
					 * and vice versa, i.e., they are
					 * in if/else parts of the branch 
					 * 2: in case of || make sure that s2 is not
					 * a successor of s1 and vicsysoue versa
					 */
					if(!postdom.isDominatedBy(s2, s1) && !postdom.isDominatedBy(s1, s2) 
							&& !gr.getSuccsOf(s1).contains(s2) && !gr.getSuccsOf(s2).contains(s1)){
						//create a branch object
						//now do the computation of nodes
						//which each branch dominates
						int[] btf = {0,0};
						for(int i = 0; i < succ.size(); i++){
							Unit curr = succ.get(i);
							int blkSize = 0;
							for(Unit v : b.getUnits()){
								if(dom.isDominatedBy(v, curr)){
									blkSize++;
								}
								btf[i] = blkSize;
							}//end for b.getUnits()
						}//end for each successor
						//System.out.println(succ.get(0) + " -> " +btf[0]);
						//System.out.println(succ.get(1) + " -> " +btf[1]);
						//compute the data
						int diff = (int) Math.round(Math.abs(btf[0] - btf[1]) * ratio);
						int perc = (int) Math.round(Math.max(btf[0], btf[1]) * ratio);
						//System.out.println("diff " + diff + " perc " + perc);
						Branch br = new Branch(ifS, succ, ifIndex.get(ifS));
						br.setDiff(diff);
						br.setPerc(perc);
						branchSet.add(br);
					}// end if neither of them dominates another
				}
				
		
	}
	
	public AbstractedCFG getAbstractedCFG(int percOfCode, int branchDiff){
		Set<Branch> condToSplit = new HashSet<Branch>();
		for(Branch br : branchSet){
			if(br.getDiff() <= branchDiff && br.getPerc() >= percOfCode){
				condToSplit.add(br);
				//System.out.println(br.getIndex() + " " + br.getIfStmt());
			}
		}
		
		//System.out.println("condToSplit1 " + condToSplit);
		
		//add all conditions that lead to ifs in condToSplit
		//need to determine on what other ifs they depend.
		boolean changed = true;
		while(changed){
			changed = false;
			Set<Branch> add = new HashSet<Branch>();
			for(Branch br : condToSplit){
				IfStmt cond = br.getIfStmt();
				for(IfStmt dep : ifSet){
					//dep must not be in condToSplit set and dominate cond
					if(contains(condToSplit, dep)==null && dom.isDominatedBy(cond, dep)){
						//now check that dep and cond are not part of the same composite
						//conditional statement, if they are they will have one common
						//successor
						//System.out.println("found dep " + dep);
						List<Unit> succOfDep = gr.getSuccsOf(dep);
						boolean noCommon = true;
						for(Unit uDep : succOfDep){
							if (br.getSucc().contains(uDep)){
								noCommon = false;
								break;
							}
						}
						
						if(noCommon && succOfDep.size() > 1){
							Unit succ1 = succOfDep.get(0);
							Unit succ2 = succOfDep.get(1);
							//in order to consider dep
							//we need to make sure that only
							//one of its branch outcomes dominates
							//our branch cond
							if(dom.isDominatedBy(cond, succ1) || dom.isDominatedBy(cond, succ2)){
								//one of the successor must dominate the branch
								if(!postdom.isDominatedBy(succ1, succ2) && 
										!postdom.isDominatedBy(succ2, succ1)){
									//one successor cannot dominate other successors, i.e.,
									//there are if/else part and not just if part.
									changed = true;
									Branch newBr = new Branch(dep,succOfDep,ifIndex.get(dep));
									add.add(newBr);
								}
							}
						}
					}
				}
			}//end for br in condToSplit
			condToSplit.addAll(add);
		}//end while(changed)
		
		//for debugging
		//debug();
		//System.out.println("condtoSplit2 " + condToSplit);
	    //System.exit(1);
		AbstractedCFG cfgA = null;
		//creating an abstract graph if there is something to split
		if(!condToSplit.isEmpty()){
			cfgA = new AbstractedCFG();
			//perform DFS of the graph and noting only relevant branches
			for(Unit u : gr.getHeads()){
				//initialize the search perhaps no need for the loop
				buildACFG(cfgA, condToSplit, u, null, false, new HashSet<Unit>());
			}
			
			//print the graph out
			//System.out.println(cfgA.toString());
		}
		
		return cfgA;
	}
	
	/*
	 * Very confusing method!
	 */
	
	private void buildACFG(AbstractedCFG cfgA, Set<Branch> branches, Unit curr, Node from,
			boolean outcome, Set<Unit> seen){
		//System.out.println("curr " + curr + " outcome " + outcome + " from " + from);
		if(gr.getTails().contains(curr)){
			//encountered at least one branch
			//at least one branch encountered
			if(from != null){
			//at the end of cfg, add end node to it.
			Node end = cfgA.findNode("end");
			if(end == null){
				end = cfgA.addEnd("end");
			} 
			 cfgA.add(from, end, outcome);
			}
		} else {
			Branch br = contains(branches, curr);
			//System.out.println("br " + br);
			if(br != null){
				//if the branch is to be explored
				String id = String.valueOf(br.getIndex());
				Node to = null;
				if(cfgA.contains(id)){
					//this condition stmt has been already explored before
					to = cfgA.findNode(id);
					//just make the connection and use the node with its descendants already created
				}else {
					if(from == null){
						//the first branch found
						//create the start node
						to = cfgA.addStart(id);
					} else {
						// will be a new regular node
						to = cfgA.addNode(id);
					}
						//first explore fall through, i.e., false branch
						//and after that branch out, i.e.,g true branch
						//of the newly discovered ifStmt
						boolean branch = false;
						for(Unit u : br.getSucc()){
							buildACFG(cfgA, branches, u, to, branch,seen);
							branch = !branch;//flip the branch outcome for next successor
						}
				} 
				
				//found to node
				//create an edge "from" "to" when "from" is not the start node
				if(from!=null){
					cfgA.add(from, to, outcome);
				}

			} else {
				//curr is not to be explored
				//then just continue the graph exploration
				//will optimize later for loops - need just skip them
				int succCount = 0;
				for(Unit u : gr.getSuccsOf(curr)){
					//in case of cond stmt that are not in the tracking set
					//the alg will always takes false branch (fall through)
					//and never goes onto exploring true branch (branch out)
					//this should take care of loops
					if(!(curr instanceof IfStmt) || succCount == 0 ){
						//System.out.println("cont " + curr + " to " + u + " count " + succCount);
					try{
						buildACFG(cfgA, branches, u, from, outcome, seen);
					} catch(StackOverflowError e){
						System.err.print("stack overflow!");
						System.exit(2);
					}
					}
					succCount++;
				}
				
			}
		}
	}
	
	private void debug(){
		CFGToDotGraph cfgToDot = new CFGToDotGraph(); 
		DotGraph dotGraph = cfgToDot.drawCFG(gr, b);
		dotGraph.plot("bf1.dot");
	}
	
	private static Branch contains(Set<Branch> brSet, Unit ifS){
		Branch ret = null;
		for(Branch br : brSet){
			if(br.getIfStmt().equals(ifS)){
				ret = br;
				break;
			}
		}
		return ret;
	}

}
