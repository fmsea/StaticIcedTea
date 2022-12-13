package abstractinterp.scalar;

import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.NumericConstant;
import soot.jimple.ParameterRef;
import soot.jimple.internal.JimpleLocal;
import soot.jimple.internal.JNegExpr;
import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.LongType;
import soot.Local;
import soot.ShortType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.toolkits.graph.DirectedGraph;

import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.factory.StateFactory;
import abstractinterp.scalar.state.DeferredCmpMap;
import abstractinterp.scalar.state.BinaryOperatorType;
import abstractinterp.scalar.state.PredicateType;
import abstractinterp.scalar.util.Pair;

public class ForwardBranchedFlowNumerical<S extends State>
    extends ForwardBranchedFlowWidening<Unit, S> {

    protected Set<Local> variables;
    private StateFactory<S> stateFactory;
    private DeferredCmpMap deferredComparisons;

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
        this.deferredComparisons = new DeferredCmpMap();
    }

    /** Widen flows
     *
     * Widen flows following the definition of widening per Miné's PADO 2001 paper.
     *
     * m_ij ▽ n_ij = { m_ij if n_ij ≤ m_ij else +∞ }
     * where `prevBeforeFlow` is `m` and `beforeFlow` is `n`.
     *
     * http://dx.doi.org/10.1007/3-540-44978-7_10
     */
    @Override
    protected void widen(S prevBeforeFlow, S beforeFlow) {
        LOGGER.trace("widening {} with {}", prevBeforeFlow, beforeFlow);
        State widenedFlow = prevBeforeFlow.copy();
        widenedFlow.widenWith(beforeFlow);
        LOGGER.trace("widening result: {}", widenedFlow);
        widenedFlow.copyTo(beforeFlow);
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
                Set<Local> track = new HashSet<>();
                track.add((Local)lhs);
                this.changedVariables.put(s, track);
                Local lVar = (Local) lhs;
                Value rhs = stmt.getRightOp();
                if (rhs instanceof BinopExpr) {
                    BinaryOperatorType op = BinaryOperatorType.fromJimple((BinopExpr) rhs);
                    Value left = ((BinopExpr) rhs).getOp1();
                    Value right = ((BinopExpr) rhs).getOp2();

                    LOGGER.debug("assigning {} to {} ({}) {}, using {}",
                                 lVar, left, op, right, in);
                    if (op == BinaryOperatorType.CMP) {
                        // Add lhs, left, and right to DeferredCmpMap.
                        this.deferredComparisons.put(lVar, left, right);
                    } else {
                        ifStmtFall.updateState(lVar, in, left, right, op);
                    }
                } else if (rhs instanceof JimpleLocal ||
                           rhs instanceof NumericConstant ||
                           rhs instanceof JNegExpr) {
                    ifStmtFall.updateState(lVar, in, rhs);
                } else {
                    LOGGER.debug("Unhandled assignment expression [lhs={}, rhs={}]", lhs, rhs);
                    ifStmtFall.forget(lVar);
                }
                LOGGER.trace("[in state: {}, out state: {}]", in, ifStmtFall);
                Set<Local> fallChanged = ifStmtFall.getChangedVariables(in);
                LOGGER.trace("fall changed: {} [unit = {}]", fallChanged, s);
                this.minChangedVariables.putFall(s, ifStmtFall.getChangedVariables(in));
            }
        } else if (s instanceof IfStmt) {
            IfStmt stmt = (IfStmt)s;
            ConditionExpr condExpr = (ConditionExpr) stmt.getCondition();
            Value lhs = condExpr.getOp1();
            Value rhs = condExpr.getOp2();
            PredicateType type = PredicateType.fromJimple(condExpr);

            // If either of the values are in the map, we should use the map to
            // refine the deferred branch.
            // Otherwise, proceed as usual.
            if (lhs instanceof Local &&
                this.deferredComparisons.contains((Local)lhs)) {
                this.deferredComparisons.get((Local)lhs).ifPresent(deferred -> {
                        interpretCondition(s,
                                           in,
                                           deferred.fst(),
                                           deferred.snd(),
                                           type,
                                           ifStmtBranch,
                                           ifStmtFall);
                    });
            // This version is not likely given how the code tends to be generated.
            // } else if (right instanceof Local &&
            //            this.deferredComparisons.contains((Local) right)) {
            } else {
                interpretCondition(s,
                                   in,
                                   lhs,
                                   rhs,
                                   type,
                                   ifStmtBranch,
                                   ifStmtFall);
            }
        } else if (s instanceof IdentityStmt) {
            // skip
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

    private void interpretCondition(Unit s, S in, Value left, Value right, PredicateType type, S branch, S fall) {
        this.outputStmt.add(s);
        Set<Local> track = new HashSet<>();
        this.changedVariables.put(s, track);

        branch.updateCond(in, left, right, type);

        type = type.rotate();

        fall.updateCond(in, left, right, type);

        if (left instanceof JimpleLocal) {
            track.add((Local)left);
        }
        if (right instanceof JimpleLocal) {
            track.add((Local)right);
        }
        Set<Local> fallChanged = fall.getChangedVariables(in);
        Set<Local> branchChanged = branch.getChangedVariables(in);
        LOGGER.trace("fall changed: {} [unit = {}]", fallChanged, s);
        LOGGER.trace("branch changed: {} [unit = {}]", branchChanged, s);
        this.minChangedVariables.putFall(s, fallChanged);
        this.minChangedVariables.putBranch(s, branchChanged);
    }

    public static boolean isIntType(Value val) {
        return NumericalAnalysisUtil.isIntType(val);
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
