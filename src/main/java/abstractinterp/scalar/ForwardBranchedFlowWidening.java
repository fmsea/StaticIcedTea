package abstractinterp.scalar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
/**
 * Widening also asks about after how many
 * iterations apply widening and also a list
 * of widening nodes
 * @author elenasherman
 *
 * @param <N>
 * @param <A>
 */
public abstract class ForwardBranchedFlowWidening<N extends Unit, A> extends ForwardBranchedFlowBasic<N, A> {

	
	Set<N> wideningNodes;
	/**
	 * Count of merging performed by a widening node
	 */
	Map<N,Integer> itersCount;
	

	public ForwardBranchedFlowWidening(DirectedGraph<N> graph, List<N> order,
			Map<N, A> unitToBeforeFlow, Map<N, List<A>> unitToAfterBranchFlow, 
			Map<N, List<A>> unitToAfterFallFlow,
			Set<N> wideningNodes, int iters) {
		super(graph);
		this.order = order;
		this.unitToAfterBranchFlow = unitToAfterBranchFlow;
		this.unitToAfterFallFlow = unitToAfterFallFlow;
		this.unitToBeforeFlow = unitToBeforeFlow;
		this.wideningNodes = wideningNodes;
		itersCount = new HashMap<N,Integer>();
		for(N n : wideningNodes){
			itersCount.put(n, iters);
		}
	}
	
	@Override
	protected void mergeFlows(N node, A beforeFlow, List<A> preds){
		
		if(wideningNodes.contains(node)){
			//copy beforeFlow
			A prevBeforeFlow = newInitialFlow(); //initFlows
			copy(beforeFlow, prevBeforeFlow);
			//do your own merge done in a particular order
			// if you want or just call super
			//x_in is the first element of preds list
			//x_back are the rest one in the list
			basicMergeFlows(node, beforeFlow, preds);
			//if different 
			if(!prevBeforeFlow.equals(beforeFlow)){
				//check the count
				int mergeCounts = itersCount.get(node);
				if(mergeCounts == 0){
				widen(beforeFlow, prevBeforeFlow);
				} else if (mergeCounts < 0){
					System.err.println("Wideining is not working for " + node);
				}
				mergeCounts--;
				itersCount.put(node, mergeCounts);
			}
			
		} else {
			//if node is not a widening node call regular merge
			basicMergeFlows(node, beforeFlow, preds);
		}
	}
	
	/**
	 * widens beforeFlow with prevBeforeFlow and writes
	 * the results back to beforeFlow
	 * @param beforeFlow
	 * @param prevBefore
	 */
	protected abstract void widen(A beforeFlow, A prevBeforeFlow);

}
