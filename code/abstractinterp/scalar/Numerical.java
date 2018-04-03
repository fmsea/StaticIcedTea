package abstractinterp.scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Parma_Polyhedra_Library;
import soot.Body;
import soot.Trap;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import util.Variables;

public class Numerical {
	UnitGraph g;
	ForwardBranchedFlowNumerical analysis;
	public Numerical(Body b, int iterations){
		this.g = new ExceptionalUnitGraph(b);
//		Variables myVariables = new Variables(g);
//		myVariables.makeVariables();
//		myVariables.renameLocals();
		//do the set up of PPL
		System.load("/Users/elenasherman/Documents/research/jpf-symbc/lib/libppl_java.jnilib");
		Parma_Polyhedra_Library.initialize_library();
		//init the analysis
		//the order
		List<Unit> order = new PseudoTopologicalOrderer<Unit>().newList(g, false);
		Map<Unit, C_Polyhedron> unitToBeforeFlow = new HashMap<Unit, C_Polyhedron>();
		Map<Unit, List<C_Polyhedron>> unitToAfterBranchFlow = new HashMap<Unit, List<C_Polyhedron>>();
		Map<Unit, List<C_Polyhedron>> unitToAfterFallFlow = new HashMap<Unit, List<C_Polyhedron>>();
		Set<Unit> wideningNode = new HashSet<Unit>();
		analysis = new ForwardBranchedFlowNumerical(g, order, unitToBeforeFlow, 
				unitToAfterBranchFlow, unitToAfterFallFlow, wideningNode, iterations, b.getLocalCount());
		//set up the flows
		
		for(Unit node : order){
			unitToBeforeFlow.put(node, analysis.newInitialFlow());
			List<C_Polyhedron> f = new ArrayList<C_Polyhedron>();
			unitToAfterFallFlow.put(node, f);
			if(node.fallsThrough()){
				f.add(analysis.newInitialFlow());
			} 
			f = new ArrayList<C_Polyhedron>();
			unitToAfterBranchFlow.put(node, f);
			if(node.branches()){
				for(int i=0; i < node.getUnitBoxes().size(); i++){
					C_Polyhedron v = analysis.newInitialFlow();
					f.add(v);
				}
			}
			//entry points
			for(Unit head : g.getHeads()){
				unitToBeforeFlow.put(head, analysis.entryInitialFlow());
			}

			//traps are treated as entry points
			if(analysis.treatTrapHandlersAsEntries()){
				for(Trap trap : ((UnitGraph) g).getBody().getTraps()){
					Unit hanlder = trap.getHandlerUnit();
					unitToBeforeFlow.put(hanlder, analysis.entryInitialFlow());
				}
			}

		}
	}

}
