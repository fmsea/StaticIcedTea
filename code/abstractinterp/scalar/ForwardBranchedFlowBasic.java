package abstractinterp.scalar;

import soot.Unit;
import soot.UnitBox;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;

import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public abstract class ForwardBranchedFlowBasic<N extends Unit, A> {

	/** The results of the analysis */
	protected Map<N, List<A>> unitToAfterFallFlow;
	protected Map<N, List<A>> unitToAfterBranchFlow;
	protected Map<N, A> unitToBeforeFlow;

	/** The order in which nodes should be analyzed */
	protected List<N> order; 

	/** Graph with nodes */
	DirectedGraph<N> graph;

	/**
	 * Constructor - can start with the results of a
	 * previous analysis.
	 */
	public ForwardBranchedFlowBasic(DirectedGraph<N> graph){
		this.graph = graph;
	}
	
	public void setOrder(List<N> order){
		this.order = order;
	}
	
	public void setBeforeFlow(Map<N, A> unitToBeforeFlow ){
		this.unitToBeforeFlow = unitToBeforeFlow;
	}
	
	public void setAfterFallFlow(Map<N, List<A>> unitToAfterFallFlow){
		this.unitToAfterFallFlow = unitToAfterFallFlow;
	}
	
	public void setAfterBranchFlow(Map<N, List<A>> unitToAfterBranchFlow){
		this.unitToAfterBranchFlow = unitToAfterBranchFlow;
	}

	/** abstract methods that needs to  be implemented */

	/** Create a copy of the <code>source</code? flow object in <code>dest</code>. */
	protected abstract void copy(A source, A dest);

	/** Merges the IN flows <code>in1</code>, <code>in2</code> 
	 * and assigns the results to <code>out</code>  */
	protected abstract void merge(A in1, A in2, A out);


	/** the transfer function for a unit <code>s</code>*/
	protected abstract void flowThrough(A in, N s, List<A> fallOut, List<A> bracnOut);
	
	 /** 
     * Returns the flow object corresponding to the initial values for
     * each graph node. 
     */
    protected abstract A newInitialFlow();

	/** the core algorithm that computes fixed point */
	protected void doAnalysis(){
		final Map<N, Integer> numbers = new HashMap<N, Integer>();
		int maxBranchSize = 0;
		for(int i = 0; i < order.size(); i++){
			N node = order.get(i);
			if(node.getUnitBoxes().size() > maxBranchSize){
				maxBranchSize = node.getUnitBoxes().size();
			}
			numbers.put(order.get(i), (i+1));
		}

		//create a comparator
		TreeSet<N> worklist = new TreeSet<N>(new Comparator<N>(){
			public int compare(N n1, N n2){
				return numbers.get(n1) - numbers.get(n2);
			}
		});
		//keep track of the predecessors
		//List<A> is the same object as in two after falows maps
		Map<N, List<A>> unitToIncomingFlowSets = new HashMap<N, List<A>>(order.size()*2 + 1, 0.7f);
		for(N node : order){
			worklist.add(node);
			unitToIncomingFlowSets.put(node, new ArrayList<A>());
		}

		/* populate that the predecessor map */
		/* we cannot use succ/pred of the graph because we do not
		 * know which one comes from which branch
		 */
		Chain<N> stmts = (Chain<N>) ((UnitGraph) graph).getBody().getUnits();
		for(N node : order){
			//case 1 not a branching node
			if(node.fallsThrough()){
				N succ = stmts.getSuccOf(node);
				if(order.contains(succ)){//make sure it is in the order
					List<A> predsFlows = unitToIncomingFlowSets.get(succ);
					predsFlows.addAll(unitToAfterFallFlow.get(node));
				}
			}
			//case 2 a branching node
			if(node.branches()){
				List<UnitBox> successors = node.getUnitBoxes();
				for(UnitBox succBox : successors){
					N succ = (N) succBox.getUnit();
					if(order.contains(succ)){//make sure it is in the order
						List<A> predsFlows = unitToIncomingFlowSets.get(succ);
						predsFlows.addAll(unitToAfterBranchFlow.get(node));
					}
				}
			}
		}
		//now the main after maps and unitToIncoingflowSets share the same objects

		/* perform fixed point computation */

		/* setting up datastructures */
		List<A> previousAfterFlows = new ArrayList<A>();
		List<A> afterFlows = new ArrayList<A>();
		/* arrays are used to save on new object inits
		 * instead creating a new object that is a copy of
		 * an after flow, the object stored in the repository
		 * is changed.
		 */
		A[] flowRepositories = (A[]) new Object[maxBranchSize+1];
		A[] previousFlowRepositories = (A[]) new Object[maxBranchSize+1];
		for(int i= 0 ; i <= maxBranchSize; i++){
			flowRepositories[i] = newInitialFlow();//init flow
			previousFlowRepositories[i] = newInitialFlow();//init flow
		}

		/* the main iteration */
		while(!worklist.isEmpty()){
			N node = worklist.first();//get the node
			worklist.remove(node);//remove it from the worklist


			/* remember the old after flows (copies) in preivousAfterFlows*/
			accumulateAfterFlowSets(node, previousFlowRepositories, previousAfterFlows);


			/* compute and store beforeFlow */
			A beforeFlow = unitToBeforeFlow.get(node);
			//iterate over the successor's after flows of a node a merge it
			List<A> preds = unitToIncomingFlowSets.get(node);
			mergeFlows(node, beforeFlow, preds);


			/*call the transfer function to compute and update after flows*/
			flowThrough(beforeFlow, node, unitToAfterFallFlow.get(node), unitToAfterBranchFlow.get(node));


			/*check the changes and update the worklist*/
			/*get the values as a list of the newly computed after flows*/
			accumulateAfterFlowSets(node, flowRepositories, afterFlows);
			if(!afterFlows.equals(previousAfterFlows)){
				//add the successors of that node
				//only put those successors that are in the order
				for(N succ : graph.getSuccsOf(node)){
					if(order.contains(succ)){
						worklist.add(succ);
					}
				}
			}
		}

	}
	
	/**
	 * Determines how to merge flows
	 * for classes implementing widening this method should 
	 * be re-implemented.
	 * @param beforeFlow
	 * @param preds - list of predecessor flows ordered - fall through
	 * is first if there is one (entry to a loop, then back edge)
	 */
	protected void mergeFlows(N node, A beforeFlow, List<A> preds){
		if(preds.size() > 0){
			Iterator<A> it = preds.iterator();
			//if only one predecessor then nothing to merge
			copy(it.next(), beforeFlow);
			//more than one merge and copy
			while(it.hasNext()){
				A otherBranchFlow = it.next();
				A newBeforeFlow = newInitialFlow(); // initialFlows
				merge(beforeFlow, otherBranchFlow, newBeforeFlow);
				copy(newBeforeFlow, beforeFlow);
			}
		}
	}

	/**
	 * This method retrieves all after flows of a node from two maps, 
	 * creates copies of them using temp objects of <code>flowRepositories</cdoe>
	 * and puts them in a list <code>afterFlows</code>
	 * @param node
	 * @param flowRepositories is newly computed afterflows of the node, the first value is 
	 * fall through (next) node, the rest of the list are branched out nodes
	 * @param afterFlows to store newly computed afterflows the node
	 */
	private void accumulateAfterFlowSets(N node, A[] flowRepositories, List<A> afterFlows) {
		int repoCount = 0;
		afterFlows.clear();
		/* node has the next node followed it */
		if(node.fallsThrough()){
			A fallThroughFlow = flowRepositories[repoCount];
			copy(unitToAfterFallFlow.get(node).get(repoCount), fallThroughFlow);
			afterFlows.add(fallThroughFlow);
			repoCount++;
		}
		/* node also has branches */
		if(node.branches()){

			List<A> l = unitToAfterBranchFlow.get(node);
			for(A fs : l){
				A branchOutflow = flowRepositories[repoCount];
				copy(fs, branchOutflow);
				afterFlows.add(branchOutflow);
				repoCount++;
			}
		}
	}

	/** Accessor function returning value of IN set for node */
	public A getFlowBefore(N node){
		return unitToBeforeFlow.get(node);
	}

	/**
	 * Accessor function returning the abstract value on the branched out
	 * @param node
	 * @return
	 */
	public List<A> getBranchFlowAfter(N node){
		return unitToAfterBranchFlow.get(node);
	}

	public A getFallFlowAfter(N node){
		A ret = newInitialFlow();//initFlows
		List<A> fl = unitToAfterFallFlow.get(node);
		if(!fl.isEmpty()){
			ret = fl.get(0);
		}
		return ret;
	}
	
	

	//	public static <N extends Unit> Map<N,List<N>> computePred(Chain<N> stmts, List<N> order){
	//		/* compute the predecessors of a node */
	//		Map<N, List<N>> predsMap = new HashMap<N, List<N>>();
	//		//add to worklist in that order
	//		for(N node : order){
	//			predsMap.put(node, new ArrayList<N>());
	//		}
	//		
	//	   //now populate the node
	//	   for(N node : order){
	//		   //adding this node as a pred to its successors
	//			//case 1 node falls through
	//			if(node.fallsThrough()){
	//				//get its successor
	//				N succ = stmts.getSuccOf(node);
	//				//and the node as its predecessor if it is in order
	//				if(order.contains(succ)){
	//					predsMap.get(succ).add(node);
	//				}
	//			}
	//			
	//			//case 2 node branches - several successors
	//			if(node.branches()){
	//				for(UnitBox ub : node.getUnitBoxes()){
	//					if(order.contains(ub)){
	//						predsMap.get(ub).add(node);
	//					}
	//				}
	//			}
	//	   }
	//	   
	//	   return predsMap;
	//	}
}
