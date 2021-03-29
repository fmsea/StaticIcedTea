package abstractinterp.scalar;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import abstractinterp.scalar.state.BoxState;
import parma_polyhedra_library.C_Polyhedron;
import parma_polyhedra_library.Coefficient;
import parma_polyhedra_library.Constraint;
import parma_polyhedra_library.Degenerate_Element;
import parma_polyhedra_library.Int32_Box;
import parma_polyhedra_library.Linear_Expression;
import parma_polyhedra_library.Linear_Expression_Variable;
import parma_polyhedra_library.Linear_Expression_Coefficient;
import parma_polyhedra_library.Relation_Symbol;
import parma_polyhedra_library.Variables_Set;
import parma_polyhedra_library.Variable;
import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.ShortType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DivExpr;
import soot.jimple.EqExpr;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.LeExpr;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NumericConstant;
import soot.jimple.SubExpr;
import soot.jimple.internal.JEqExpr;
import soot.jimple.internal.JGeExpr;
import soot.jimple.internal.JGtExpr;
import soot.jimple.internal.JLeExpr;
import soot.jimple.internal.JLtExpr;
import soot.jimple.internal.JNeExpr;
import soot.jimple.internal.JNegExpr;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.DirectedGraph;

public class ForwardBranchedFlowNumericalBox extends ForwardBranchedFlowWidening<Unit, BoxState> {

	protected Set<Local> localVars;

	public ForwardBranchedFlowNumericalBox(DirectedGraph<Unit> graph, List<Unit> order,
			Map<Unit,  BoxState> unitToBeforeFlow, Map<Unit, List< BoxState>> unitToAfterBranchFlow,
			Map<Unit, List< BoxState>> unitToAfterFallFlow, Set<Unit> wideningNodes, int iters, Set<Local> local) {
		super(graph, order, unitToBeforeFlow, unitToAfterBranchFlow, unitToAfterFallFlow, wideningNodes, iters);
		this.localVars = local;
	}

	@Override
	protected void widen(BoxState beforeFlow, BoxState prevBeforeFlow) {
		// beforeFlow what happened after merge of prevBeforeFlow and new computation
		beforeFlow.widenWith(prevBeforeFlow);
	}

	@Override
	protected void copy(BoxState source, BoxState dest) {
		//copy
		source.copyTo(dest);
		
	}

	@Override
	protected void merge(BoxState in1, BoxState in2, BoxState out) {
		in1.copyTo(out);
		out.mergeWith(in2);
		
	}

	@Override
	protected void flowThrough(BoxState in, Unit s, List<BoxState> fallOut, List<BoxState> branchOut) {
		BoxState inState = in;
		//System.out.println(s + " in " + in);
		BoxState ifStmtFall = new BoxState(localVars,true);
		inState.copyTo(ifStmtFall);
		BoxState ifStmtBranch = new BoxState(localVars,true);
		inState.copyTo(ifStmtBranch);
		//make sure it is a feasible incoming state
		if(in.isFeasible()){
			if(s instanceof AssignStmt){
				AssignStmt stmt = (AssignStmt) s;
				Value lhs = stmt.getLeftOp();
				if(lhs instanceof Local && isIntType(lhs)){
					Local lVar = (Local) lhs;
					Value rhs = stmt.getRightOp();
					if(rhs instanceof BinopExpr){
						byte type = -1;
						if(rhs instanceof AddExpr){
							type = 0;
						} else if (rhs instanceof SubExpr){
							type = 1;
						} else if (rhs instanceof MulExpr){
							type = 2;
						} else if (rhs instanceof DivExpr){
							type = 3;
						}
						Value left = ((BinopExpr) rhs).getOp1();
						Value right = ((BinopExpr) rhs).getOp2();
						
						ifStmtFall.updateState(lVar, inState, left, right, type);
						
					} else if (rhs instanceof JimpleLocal || rhs instanceof NumericConstant || 
							rhs instanceof JNegExpr){
						ifStmtFall.updateState(lVar, inState, rhs);
						
					}
				}
			} else if(s instanceof IfStmt){
				//process if stmt
				IfStmt stmt = (IfStmt) s;
				ConditionExpr condExpr = (ConditionExpr)stmt.getCondition();
				Value left = condExpr.getOp1();
				Value right = condExpr.getOp2();
				byte type = -1;
				if(condExpr instanceof EqExpr){
					type = 0;
				} else if(condExpr instanceof NeExpr){
					type = 1;
				} else if(condExpr instanceof LeExpr){
					type = 2;
				} else if(condExpr instanceof GtExpr){
					type = 3;
				} else if(condExpr instanceof GeExpr){
					type = 4;
				} else if(condExpr instanceof LtExpr){
					type = 5;
				} 
				ifStmtFall.updateCond(inState, left, right, type);//false branch
				//rotate type;
				if(type == 0){
					type = 1;
				} else if (type == 1){
					type = 0;
				} else if (type == 2){
					type = 3;
				} else if (type == 3){
					type = 2;
				} else if (type == 4){
					type = 5;
				} else if (type == 5){
					type = 4;
				}
				ifStmtBranch.updateCond(inState, left, right, type);//true branch
			}
		} 
			if(s instanceof IdentityStmt){
				IdentityStmt param = (IdentityStmt)s;
				if (isIntType(param.getLeftOp())){
					//will update to top
					ifStmtFall.updateTop((Local) param.getLeftOp());
				}
			}
		
		//System.out.println(s + " out " + ifStmtFall);
		for(Iterator<BoxState> it = fallOut.iterator(); it.hasNext(); ){
			copy(ifStmtFall, it.next());
		}
		
		for(Iterator<BoxState> it = branchOut.iterator(); it.hasNext();){
			copy(ifStmtBranch, it.next());
		}
	}
	

	public static boolean isIntType(Value val){
		Type t = val.getType();
		return !(val instanceof ArrayRef) && !(val instanceof InstanceFieldRef)&&(t instanceof IntType || t instanceof ByteType || t instanceof ShortType
				|| t instanceof BooleanType);
	}

	@Override
	protected BoxState newInitialFlow() {
		return new BoxState(localVars, false);
	}
	

	protected BoxState entryInitialFlow(){
		return new BoxState(localVars, true);
	}
	
	public boolean treatTrapHandlersAsEntries(){
		return false;
	}
}
