package abstractinterp.scalar;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

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

import abstractinterp.scalar.state.Interval32Box;
import abstractinterp.scalar.state.IntervalBoxState;

public class ForwardBranchedFlowIntervalNumericalBox extends ForwardBranchedFlowWidening<Unit, IntervalBoxState> {

    private Set<Unit> outputStmt;
    private Map<Unit, Set<Value>> changedVariables;
    protected Set<Local> localVars;

    public ForwardBranchedFlowIntervalNumericalBox(DirectedGraph<Unit> graph,
                                                   List<Unit> order,
                                                   Map<Unit,  IntervalBoxState> unitToBeforeFlow,
                                                   Map<Unit, List<IntervalBoxState>> unitToAfterBranchFlow,
                                                   Map<Unit, List<IntervalBoxState>> unitToAfterFallFlow,
                                                   Set<Unit> wideningNodes,
                                                   int iters,
                                                   Set<Local> local) {
        super(graph, order, unitToBeforeFlow, unitToAfterBranchFlow, unitToAfterFallFlow, wideningNodes, iters);
        this.outputStmt = new HashSet<>();
        this.changedVariables = new HashMap<>();
        this.localVars = local;
    }

    public Set<Unit> getOutputStatements() {
        return this.outputStmt;
    }

    public Map<Unit, Set<Value>> getChangedVariables() {
        return this.changedVariables;
    }

    @Override
    protected void widen(IntervalBoxState beforeFlow, IntervalBoxState prevBeforeFlow) {
        // beforeFlow what happened after merge of prevBeforeFlow and new computation
        beforeFlow.widenWith(prevBeforeFlow);
    }

    @Override
    protected void copy(IntervalBoxState source, IntervalBoxState dest) {
        //copy
        source.copyTo(dest);
    }

    @Override
    protected void merge(IntervalBoxState in1, IntervalBoxState in2, IntervalBoxState out) {
        in1.copyTo(out);
        out.mergeWith(in2);
    }

    @Override
    protected void flowThrough(IntervalBoxState in, Unit s, List<IntervalBoxState> fallOut, List<IntervalBoxState> branchOut) {
        IntervalBoxState inState = in;
        // System.err.println(s + " in " + in);
        IntervalBoxState ifStmtFall = new IntervalBoxState(localVars, true);
        inState.copyTo(ifStmtFall);
        IntervalBoxState ifStmtBranch = new IntervalBoxState(localVars, true);
        inState.copyTo(ifStmtBranch);
        //make sure it is a feasible incoming state
        if(in.isFeasible()){
            if(s instanceof AssignStmt){
                AssignStmt stmt = (AssignStmt) s;
                Value lhs = stmt.getLeftOp();
                if(lhs instanceof Local && isIntType(lhs)){
                    this.outputStmt.add(s);
                    Set<Value> track = new HashSet<>();
                    track.add(lhs);
                    this.changedVariables.put(s, track);
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
                this.outputStmt.add(s);
                Set<Value> track = new HashSet<>();
                this.changedVariables.put(s, track);
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
                if (left instanceof JimpleLocal) {
                    track.add(left);
                }
                if (right instanceof JimpleLocal) {
                    track.add(right);
                }

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
            // System.err.println(String.format("DEBUG: in:\n\t%s\n", in.toString()));
        for(Iterator<IntervalBoxState> it = fallOut.iterator(); it.hasNext(); ){
            IntervalBoxState boxState = it.next();
            // System.err.println(String.format("DEBUG: IfStamtFall:\n\t%s\n", boxState.toString()));
            copy(ifStmtFall, boxState);
        }

        for(Iterator<IntervalBoxState> it = branchOut.iterator(); it.hasNext();){
            IntervalBoxState boxState = it.next();
            // System.err.println(String.format("DEBUG: IfStamtBranch:\n\t%s\n", boxState.toString()));
            copy(ifStmtBranch, boxState);
        }
    }


    public static boolean isIntType(Value val){
        Type t = val.getType();
        return !(val instanceof ArrayRef) &&
            !(val instanceof InstanceFieldRef) &&
            (t instanceof IntType ||
             t instanceof ByteType ||
             t instanceof ShortType ||
             t instanceof BooleanType);
    }

    @Override
    protected IntervalBoxState newInitialFlow() {
        return new IntervalBoxState(localVars, false);
    }


    protected IntervalBoxState entryInitialFlow(){
        return new IntervalBoxState(localVars, true);
    }

    public boolean treatTrapHandlersAsEntries(){
        return false;
    }
}
