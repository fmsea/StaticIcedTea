package abstractinterp.scalar;

import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
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

import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.factory.StateFactory;
import abstractinterp.scalar.state.BinaryOperator;
import abstractinterp.scalar.state.PredicateType;

public class ForwardBranchedFlowNumerical<S extends State>
    extends ForwardBranchedFlowWidening<Unit, S> {

    protected Set<Local> variables;
    private StateFactory<S> stateFactory;

    public ForwardBranchedFlowNumerical(DirectedGraph<Unit> graph,
                                        List<Unit> order,
                                        Map<Unit, S> unitToBeforeFlow,
                                        Map<Unit, List<S>> unitToAfterBranchFlow,
                                        Map<Unit, List<S>> unitToAfterFallFlow,
                                        Set<Unit> wideningNodes,
                                        int iters,
                                        Set<Local> locals,
                                        StateFactory<S> stateFactory) {
        super(graph,
              order,
              unitToBeforeFlow,
              unitToAfterBranchFlow,
              unitToAfterFallFlow,
              wideningNodes,
              iters);
        this.variables = locals;
        this.stateFactory = stateFactory;
    }

    @Override
    protected void widen(S beforeFlow, S prevBeforeFlow) {
        LOGGER.trace("widening {} with {}", beforeFlow, prevBeforeFlow);
        beforeFlow.widenWith(prevBeforeFlow);
    }

    @Override
    protected void copy(S source, S dest) {
        source.copyTo(dest);
    }

    @Override
    protected void merge(S in1, S in2, S out) {
        LOGGER.trace("merging {} with {}", in1, in2);
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
        LOGGER.trace("merge result: {}", out);
    }

    @Override
    protected void flowThrough(S in, Unit s, List<S> fallOut, List<S> branchOut) {
        LOGGER.debug("{} flow through: {}", s, in);
        S ifStmtFall = this.stateFactory.copy(in);
        S ifStmtBranch = this.stateFactory.copy(in);
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

                    LOGGER.debug("assigning {} to {} ({}) {}, using {}",
                                 lVar, left, op, right, in);
                    ifStmtFall.updateState(lVar, in, left, right, op);
                } else if (rhs instanceof JimpleLocal ||
                           rhs instanceof NumericConstant ||
                           rhs instanceof JNegExpr) {
                    ifStmtFall.updateState(lVar, in, rhs);
                } else {
                    ifStmtFall.forget(lVar);
                }
            }
        } else if (s instanceof IfStmt) {
            IfStmt stmt = (IfStmt)s;
            ConditionExpr condExpr = (ConditionExpr) stmt.getCondition();
            Value left = condExpr.getOp1();
            Value right = condExpr.getOp2();
            this.outputStmt.add(s);
            Set<Value> track = new HashSet<>();
            this.changedVariables.put(s, track);
            PredicateType type = PredicateType.fromJimple(condExpr);

            ifStmtBranch.updateCond(in, left, right, type);

            type = type.rotate();

            ifStmtFall.updateCond(in, left, right, type);

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

        for (S state : fallOut) {
            this.copy(ifStmtFall, state);
            LOGGER.debug("fall out: {}", state);
        }

        for (S state : branchOut) {
            this.copy(ifStmtBranch, state);
            LOGGER.debug("branch out: {}", state);
        }
    }

    public static boolean isIntType(Value val) {
        Type t = val.getType();
        return !(val instanceof ArrayRef)
            && !(val instanceof InstanceFieldRef)
            && (t instanceof IntType ||
                t instanceof ByteType ||
                t instanceof ShortType ||
                t instanceof BooleanType);
    }

    @Override
    protected S newInitialFlow() {
        return this.stateFactory.initialFlow(this.variables);
    }

    protected S entryInitialFlow() {
        return this.stateFactory.entryFlow(this.variables);
    }

    public boolean treatTrapHandlersAsEntries() {
        return false;
    }
}
