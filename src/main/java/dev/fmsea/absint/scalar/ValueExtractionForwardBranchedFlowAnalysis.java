package dev.fmsea.absint.scalar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import dev.fmsea.absint.scalar.state.BinaryOperatorType;
import dev.fmsea.absint.scalar.state.PredicateType;
import dev.fmsea.absint.scalar.state.PredicateValuePair;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JNegExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;

public class ValueExtractionForwardBranchedFlowAnalysis
    extends ForwardBranchedFlowAnalysis<Unit, Set<PredicateValuePair>> {

    public ValueExtractionForwardBranchedFlowAnalysis(DirectedGraph<Unit> graph) {
        super(graph);
        this.unitToAfterFallFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        this.unitToAfterBranchFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        this.unitToBeforeFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        this.order = new PseudoTopologicalOrderer<Unit>().newList(graph, false);
    }

    @Override
    protected void copy(Set<PredicateValuePair> src,
                        Set<PredicateValuePair> dst) {
        dst.addAll(src);
    }

    @Override
    protected void merge(Set<PredicateValuePair> in1,
                         Set<PredicateValuePair> in2,
                         Set<PredicateValuePair> out) {
        out.addAll(Stream.concat(in1.stream(), in2.stream()).collect(Collectors.toSet()));
    }

    @Override
    protected void flowThrough(Set<PredicateValuePair> in,
                               Unit s,
                               List<Set<PredicateValuePair>> fallOut,
                               List<Set<PredicateValuePair>> branchOut) {
        LOGGER.debug("{} flow through: {}", s, in);
        Set<PredicateValuePair> ifFall = new HashSet<>();
        ifFall.addAll(in);
        Set<PredicateValuePair> ifBranch = new HashSet<>();
        ifBranch.addAll(in);
        if (s instanceof AssignStmt) {
            AssignStmt stmt = (AssignStmt)s;
            Value lhs = stmt.getLeftOp();
            if (lhs instanceof Local && isIntType(lhs)) {
                Value rhs = stmt.getRightOp();
                if (rhs instanceof BinopExpr) {
                    BinaryOperatorType op = BinaryOperatorType.fromJimple((BinopExpr) rhs);
                    Value left = ((BinopExpr) rhs).getOp1();
                    Value right = ((BinopExpr) rhs).getOp2();
                    interpretValue(left).ifPresent(v -> {
                            // ifFall.add(PredicateValuePair.of(PredicateType.Lt, v));
                            ifFall.add(PredicateValuePair.of(v));
                            // ifFall.add(PredicateValuePair.of(PredicateType.Gt, v));
                        });
                    interpretValue(right).ifPresent(v -> ifFall.add(PredicateValuePair.of(v)));
                } else if (rhs instanceof NumericConstant ||
                           rhs instanceof JNegExpr) {
                    interpretValue(rhs).ifPresent(v -> {
                            // ifFall.add(PredicateValuePair.of(PredicateType.Lt, v));
                            ifFall.add(PredicateValuePair.of(v));
                            // ifFall.add(PredicateValuePair.of(PredicateType.Gt, v));
                        });
                } else {
                    // LOGGER.warn("Unhandled assignment expression [lhs={}, rhs={}]", lhs, rhs);
                }
            }
        } else if (s instanceof IfStmt) {
            IfStmt stmt = (IfStmt)s;
            ConditionExpr condExpr = (ConditionExpr) stmt.getCondition();
            Value lhs = condExpr.getOp1();
            Value rhs = condExpr.getOp2();
            PredicateType type = PredicateType.fromJimple(condExpr);
            PredicateType negated = type.rotate();
            interpretValue(lhs).ifPresent(v -> {
                    ifFall.add(PredicateValuePair.of(type, v));
                    ifFall.add(PredicateValuePair.of(negated, v));
                });
            interpretValue(rhs).ifPresent(v -> {
                    ifFall.add(PredicateValuePair.of(type, v));
                    ifFall.add(PredicateValuePair.of(negated, v));
                });
        }

        LOGGER.debug("interpreted {} [ifFall = {}, ifBranch = {}]", s, ifFall, ifBranch);

        for (Set<PredicateValuePair> pair : fallOut) {
            this.copy(ifFall, pair);
        }
        for (Set<PredicateValuePair> pair : branchOut) {
            this.copy(ifBranch, pair);
        }
    }

    protected Optional<Long> interpretValue(Value v) {
        if (v instanceof JNegExpr) {
            v = ((JNegExpr)v).getOp();
            if (v instanceof IntConstant) {
                IntConstant c = (IntConstant)v;
                return Optional.of(Long.valueOf(c.value * -1));
            } else if (v instanceof LongConstant) {
                LongConstant c = (LongConstant)v;
                return Optional.of(c.value * -1);
            } else {
                return Optional.empty();
            }
        } else if (v instanceof IntConstant) {
            IntConstant c = (IntConstant)v;
            return Optional.of(Long.valueOf(c.value));
        } else if (v instanceof LongConstant) {
            LongConstant c = (LongConstant)v;
            return Optional.of(c.value);
        } else {
            return Optional.empty();
        }
    }


    protected static boolean isIntType(Value val) {
        return NumericalAnalysisUtil.isIntType(val);
    }

    @Override
    protected Set<PredicateValuePair> newInitialFlow() {
        return new HashSet<>();
    }

    @Override
    protected Set<PredicateValuePair> entryInitialFlow() {
        return new HashSet<>();
    }

    public boolean treatTrapHandlersAsEntries() {
        return false;
    }
}
