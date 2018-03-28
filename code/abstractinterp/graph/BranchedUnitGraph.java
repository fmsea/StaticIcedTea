package abstractinterp.graph;

import soot.Unit;
import soot.toolkits.graph.UnitGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a wrap around UnitGraph
 * only it can distinguish between different
 * successors and predecessors: fall through or branch out
 * @author elenasherman
 *
 */
public class BranchedUnitGraph<N extends Unit> {
	/*the list of nodes to which node falls through*/
	protected Map<N, List<N>> nodeToFallThroughSuccs;
	/*the list of nodes to which node branches out*/
	protected Map<N, List<N>> nodeToBranchOutSucss;
	/*the list of nodes that fall through to node*/
	protected Map<N, List<N>> nodeToFallThroughPred;
	/*the list of nodes that branch out to node*/
	protected Map<N, List<N>> nodeToBranchOutPred;
	/*the original graph*/
	protected UnitGraph graph;
	
	public BranchedUnitGraph(UnitGraph graph){
		this.graph = graph;
		nodeToFallThroughSuccs = new HashMap<N, List<N>>();
		nodeToBranchOutSucss = new HashMap<N, List<N>>();
		nodeToFallThroughPred = new HashMap<N, List<N>>();
		nodeToBranchOutPred = new HashMap<N, List<N>>();
	}
	
	
	public List<N> getFallThroughSuccs(N node){
		return nodeToFallThroughSuccs.get(node);
	}
	
	public List<N> getBranchOutSucss(N node){
		return nodeToBranchOutSucss.get(node);
	}
	
	public List<N> getFallThroughPred(N node){
		return nodeToFallThroughPred.get(node);
	}
	
	public List<N> getBranchOutPred(N node){
		return nodeToBranchOutPred.get(node);
	}
	
	public UnitGraph getGraph(){
		return graph;
	}


}
