package abstractinterp.scalar;

import java.util.List;
import java.util.Map;
import java.util.Set;

import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Degenerate_Element;
import parma_polyhedra_library.Variables_Set;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

public class ForwardBranchedFlowNumerical extends ForwardBranchedFlowWidening<Unit, C_Polyhedron> {

	/**the dimention of the polyherdron **/
	int dim;
	Variables_Set vs;

	public ForwardBranchedFlowNumerical(DirectedGraph<Unit> graph, List<Unit> order,
			Map<Unit, C_Polyhedron> unitToBeforeFlow, Map<Unit, List<C_Polyhedron>> unitToAfterBranchFlow,
			Map<Unit, List<C_Polyhedron>> unitToAfterFallFlow, Set<Unit> wideningNodes, int iters, int dim) {
		super(graph, order, unitToBeforeFlow, unitToAfterBranchFlow, unitToAfterFallFlow, wideningNodes, iters);

		vs = new Variables_Set();
	}

	@Override
	protected void widen(C_Polyhedron beforeFlow, C_Polyhedron prevBeforeFlow) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void copy(C_Polyhedron source, C_Polyhedron dest) {
		//unconstraint dest
		dest.unconstrain_space_dimensions(vs);
		//add constraints of one to another
		dest.add_constraints(source.constraints());
		
	}

	@Override
	protected void merge(C_Polyhedron in1, C_Polyhedron in2, C_Polyhedron out) {
		//out should be an init set = false
		
	}

	@Override
	protected void flowThrough(C_Polyhedron in, Unit s, List<C_Polyhedron> fallOut, List<C_Polyhedron> bracnOut) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected C_Polyhedron newInitialFlow() {
		//should be bot
		return new C_Polyhedron(dim,Degenerate_Element.EMPTY);
	}
	
	protected C_Polyhedron entryInitialFlow(){
		//should be top
		return new C_Polyhedron(dim,Degenerate_Element.UNIVERSE);
	}
	
	protected boolean treatTrapHandlersAsEntries(){
		return false;
	}

}
