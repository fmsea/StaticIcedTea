package abstractinterp.scalar;

import java.util.List;
import java.util.Map;
import java.util.Set;

import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Degenerate_Element;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Linear_Expression_Coefficient;
import parma_polyhedra_library.Relation_Symbol;
import parma_polyhedra_library.Variables_Set;
import parma_polyhedra_library.Variable;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.NegExpr;
import soot.toolkits.graph.DirectedGraph;

public class ForwardBranchedFlowNumerical extends ForwardBranchedFlowWidening<Unit, C_Polyhedron> {

	/**the dimention of the polyherdron **/
	protected int dim;
	protected Variables_Set vs;
	protected Map<Local,Variable> localToVar;

	public ForwardBranchedFlowNumerical(DirectedGraph<Unit> graph, List<Unit> order,
			Map<Unit, C_Polyhedron> unitToBeforeFlow, Map<Unit, List<C_Polyhedron>> unitToAfterBranchFlow,
			Map<Unit, List<C_Polyhedron>> unitToAfterFallFlow, Set<Unit> wideningNodes, int iters, Map<Local,Variable> localToVar) {
		super(graph, order, unitToBeforeFlow, unitToAfterBranchFlow, unitToAfterFallFlow, wideningNodes, iters);
		this.localToVar = localToVar;
		dim = localToVar.size();
		vs = new Variables_Set();
		vs.addAll(localToVar.values());
	}

	@Override
	protected void widen(C_Polyhedron beforeFlow, C_Polyhedron prevBeforeFlow) {
		// beforeFlow what happened after merge of prevBforeFlow and new computation
		if(beforeFlow.contains(prevBeforeFlow)){
			beforeFlow.widening_assign(prevBeforeFlow, null);
		} else {
			System.err.println("Cannot widen: prevFlow is not contained in beforeFlow");
		}
		
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
		//copy in1 into out
		copy(in1, out);
		//compute their least upper bound
		out.upper_bound_assign(in2);
	}

	@Override
	protected void flowThrough(C_Polyhedron in, Unit node, List<C_Polyhedron> fallOut, List<C_Polyhedron> bracnOut) {
		//that is the main part here of computing the transfer function
		
		//translate an assignment into PPL's constraint
		//and add it to the polyhedron
		
		//for now just copy one into another
		C_Polyhedron inState = in;
		C_Polyhedron ifStmtFall = new C_Polyhedron(dim, Degenerate_Element.EMPTY);
		ifStmtFall.add_constraints(inState.constraints());
		C_Polyhedron ifStmtBranch = new C_Polyhedron(dim, Degenerate_Element.EMPTY);
		ifStmtBranch.add_constraints(inState.constraints());
		
		//check if in is feasible
		if(in.contains_integer_point()){
			//in is not empty
			if(node instanceof AssignStmt){
				AssignStmt stmt = (AssignStmt) node;
				Value lhs = stmt.getLeftOp();
				if(lhs instanceof Local){
					
					//unconstrain for lhs - remove all constraints on lhs
					//ifStmtFall.unconstrain_space_dimension(localToVar.get(lhs));
					Linear_Expression_Variable lhsVar = new Linear_Expression_Variable(localToVar.get(lhs));
					Value rhs = stmt.getRightOp();
					Linear_Expression rhsExpr = parse(rhs);
					Constraint c = new Constraint(lhsVar, Relation_Symbol.EQUAL, rhsExpr);
					//add the constraint to the fallThrough polyhedron
					ifStmtFall.add_constraint(c);
				}
			} else if(node instanceof IfStmt){
				
			}
		}
		
	}
	
	protected Linear_Expression parse(Value rhs){
		Linear_Expression ret = null;
		if(rhs instanceof BinopExpr){
			
		} else if(rhs instanceof NegExpr){
			
		} else {
			ret = singelton(rhs);
		}
		
		return ret;
	}
	
	protected Linear_Expression singelton(Value rhs){
		Linear_Expression ret = null;
		if (rhs instanceof Local){
			ret = new Linear_Expression_Variable(localToVar.get(rhs));
		} else if(rhs instanceof IntConstant){
			int constVal = ((IntConstant)rhs).value;
			ret = new Linear_Expression_Coefficient(new Coefficient(constVal));
		} else {
			System.err.println("Cannot process type " + rhs.getClass() + " in singelton");
		}
		return ret;
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
