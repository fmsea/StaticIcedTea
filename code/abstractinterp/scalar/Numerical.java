package abstractinterp.scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import abstractinterp.scalar.state.BoxState;
import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Parma_Polyhedra_Library;
import parma_polyhedra_library.Variable;

import soot.Body;
import soot.Local;
import soot.Trap;
import soot.Unit;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import util.Variables;



public class Numerical {
	UnitGraph g;
	ForwardBranchedFlowNumericalBox analysis;
	public Numerical(Body b, int iterations){
		this.g = new ExceptionalUnitGraph(b);
//		Variables myVariables = new Variables(g);
//		myVariables.makeVariables();
//		myVariables.renameLocals();
		//do the set up of PPL
		System.load("/Users/elenasherman/Documents/tools/ppl-1.2/interfaces/Java/jni/libppl_java.ls");
		Parma_Polyhedra_Library.initialize_library();
		//init the analysis
		//the order
		List<Unit> order = new PseudoTopologicalOrderer<Unit>().newList(g, false);
		Map<Unit,  BoxState> unitToBeforeFlow = new HashMap<Unit,  BoxState>();
		Map<Unit, List<BoxState>> unitToAfterBranchFlow = new HashMap<Unit, List<BoxState>>();
		Map<Unit, List<BoxState>> unitToAfterFallFlow = new HashMap<Unit, List<BoxState>>();
		Set<Unit> wideningNode = new HashSet<Unit>();
		Set<Local> locals = new HashSet<Local>();
		for(Local l : b.getLocals()){
			locals.add(l);
		}
		//find the head of the loops
		LoopNestTree loopTree = new LoopNestTree(b);
		//can be also used to do the order
		Iterator<Loop> lit = loopTree.descendingIterator();
		while(lit.hasNext()){
			wideningNode.add(lit.next().getHead());
		}
		analysis = new ForwardBranchedFlowNumericalBox(g, order, unitToBeforeFlow, 
				unitToAfterBranchFlow, unitToAfterFallFlow, wideningNode, iterations, locals);
		//set up the flows
		
		for(Unit node : order){
			unitToBeforeFlow.put(node, analysis.newInitialFlow());
			List<BoxState> f = new ArrayList< BoxState>();
			unitToAfterFallFlow.put(node, f);
			if(node.fallsThrough()){
				f.add(analysis.newInitialFlow());
			} 
			f = new ArrayList< BoxState>();
			unitToAfterBranchFlow.put(node, f);
			if(node.branches()){
				for(int i=0; i < node.getUnitBoxes().size(); i++){
					 BoxState v = analysis.newInitialFlow();
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
