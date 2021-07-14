package abstractinterp.scalar;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.NegExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.ConditionExpr;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JNegExpr;
import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.ShortType;
import soot.Type;
import soot.Unit;
import soot.Value;

import soot.toolkits.graph.DirectedGraph;

import abstractinterp.scalar.state.DifferenceBoundedState;
import abstractinterp.scalar.state.PredicateType;
import abstractinterp.scalar.state.BinaryOperator;


public class ForwardBranchedFlowDifferenceBoundedNumerical
    extends ForwardBranchedFlowWidening<Unit, DifferenceBoundedState> {

    protected Set<Local> locals;

    public ForwardBranchedFlowDifferenceBoundedNumerical(DirectedGraph<Unit> graph,
                                                         List<Unit> order,
                                                         Map<Unit, DifferenceBoundedState> unitToBeforeFlow,
                                                         Map<Unit, List<DifferenceBoundedState>> unitToAfterBranchFlow,
                                                         Map<Unit, List<DifferenceBoundedState>> unitToAfterFallFlow,
                                                         Set<Unit> wideningNodes,
                                                         int iters,
                                                         Set<Local> locals) {
        super(graph,
              order,
              unitToBeforeFlow,
              unitToAfterBranchFlow,
              unitToAfterFallFlow,
              wideningNodes,
              iters);
        this.locals = locals;
    }

    @Override
    protected void widen(DifferenceBoundedState beforeFlow,
                         DifferenceBoundedState prevBeforeFlow) {
        beforeFlow.widenWith(prevBeforeFlow);
    }

    @Override
    protected void copy(DifferenceBoundedState source,
                        DifferenceBoundedState dest) {
        source.copyTo(dest);
    }

    @Override
    protected void merge(DifferenceBoundedState in1,
                         DifferenceBoundedState in2,
                         DifferenceBoundedState out) {
        boolean in1Feasible = in1.isFeasible();
        boolean in2Feasible = in2.isFeasible();
        if (in1Feasible && in2Feasible) {
            in1.copyTo(out);
            out.mergeWith(in2);
        } else if (in1Feasible) {
            in1.copyTo(out);
        } else if (in2Feasible) {
            in2.copyTo(out);
        } else {
            out.makeInfeasible();
        }
        LOGGER.debug("Merged {} ({}) and {} ({}) ==> {}",
                     in1, in1Feasible, in2, in2Feasible, out);
    }

    @Override
    protected void flowThrough(DifferenceBoundedState in,
                               Unit s,
                               List<DifferenceBoundedState> fallOut,
                               List<DifferenceBoundedState> branchOut) {
        DifferenceBoundedState ifStmtFall = new DifferenceBoundedState(in);
        DifferenceBoundedState ifStmtBranch = new DifferenceBoundedState(in);

        LOGGER.debug("{} flow in: {}", s, in);
        if (in.isFeasible()) {
            if (s instanceof AssignStmt) {
                AssignStmt stmt = (AssignStmt)s;
                Value lhs = stmt.getLeftOp();
                if (lhs instanceof Local && isIntType(lhs)) {
                    this.outputStmt.add(s);
                    Set<Value> track = new HashSet<>();
                    track.add(lhs);
                    this.changedVariables.put(s, track);
                    Local lVar = (Local) lhs;
                    Value rhs = stmt.getRightOp();
                    if (rhs instanceof BinopExpr) {
                        BinaryOperator op = BinaryOperator.fromJimple((BinopExpr) rhs);
                        Value left = ((BinopExpr) rhs).getOp1();
                        Value right = ((BinopExpr) rhs).getOp2();

                        LOGGER.debug("assigning {} to {} ({}) {}, within {}",
                                     lVar, left, op, right, in);
                        ifStmtFall.updateState(lVar, in, left, right, op);
                    } else if (rhs instanceof JimpleLocal ||
                               rhs instanceof NumericConstant ||
                               rhs instanceof JNegExpr) {
                        ifStmtFall.updateState(lVar, in, rhs);
                    }
                }
            } else if (s instanceof IfStmt) {
                IfStmt stmt = (IfStmt) s;
                ConditionExpr condExpr = (ConditionExpr) stmt.getCondition();
                Value left = condExpr.getOp1();
                Value right = condExpr.getOp2();
                this.outputStmt.add(s);
                Set<Value> track = new HashSet<>();
                this.changedVariables.put(s, track);
                PredicateType type = PredicateType.fromJimple(condExpr);

                ifStmtBranch.updateCond(in, left, right, type); // true branch

                //rotate type
                type = type.rotate();
                ifStmtFall.updateCond(in, left, right, type);  // false branch
                if (left instanceof JimpleLocal) {
                    track.add(left);
                }
                if (right instanceof JimpleLocal) {
                    track.add(right);
                }
            }
            if (s instanceof IdentityStmt) {
                IdentityStmt param = (IdentityStmt) s;
                if (isIntType(param.getLeftOp())) {
                    ifStmtFall.updateTop((Local) param.getLeftOp());
                }
            }
        } else {
            // copy infeasible graph since the state may not be marked
            // infeasible until _after_ invoking isFeasible()
            ifStmtFall = new DifferenceBoundedState(in);
            ifStmtBranch = new DifferenceBoundedState(in);
        }

        for (DifferenceBoundedState state : fallOut) {
            copy(ifStmtFall, state);
            LOGGER.debug("fallOut: {}", state);
        }

        for (DifferenceBoundedState state : branchOut) {
            copy(ifStmtBranch, state);
            LOGGER.debug("branchOut: {}", branchOut);
        }
    }

    public static boolean isIntType(Value val) {
        Type t = val.getType();
        return (!(val instanceof ArrayRef) &&
                !(val instanceof InstanceFieldRef) &&
                (t instanceof IntType ||
                 t instanceof ByteType ||
                 t instanceof ShortType ||
                 t instanceof BooleanType));
    }

    @Override
    protected DifferenceBoundedState newInitialFlow() {
        return new DifferenceBoundedState(this.locals, false);
    }

    protected DifferenceBoundedState entryInitialFlow() {
        return new DifferenceBoundedState(this.locals, true);
    }

    public boolean treatTrapHandlersAsEntries() {
        return false;
    }
}
