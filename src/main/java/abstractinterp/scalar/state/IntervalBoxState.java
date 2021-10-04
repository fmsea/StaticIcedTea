package abstractinterp.scalar.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solver.SolverWrapper;

public class IntervalBoxState implements State {
    // map of variables to its interval abstract state

    private static Logger LOGGER = LoggerFactory.getLogger(IntervalBoxState.class);

    private Map<Local, Interval32Box> state;

    public IntervalBoxState(Set<Local> keys, boolean top) {

        state = new HashMap<Local, Interval32Box>();
        if (top) {
            for (Local l : keys) {
                Interval32Box rb = Interval32Box.TOP();
                state.put(l, rb);
            }
        } else {
            for (Local l : keys) {
                Interval32Box rb = Interval32Box.BOT();
                state.put(l, rb);
            }
        }
    }

    public IntervalBoxState(IntervalBoxState inState) {
        this(inState.state.keySet(), false);
        inState.copyTo(this);
    }

    public boolean isFeasible() {
        boolean ret = true;
        for (Interval32Box v : state.values()) {
            if (!v.containsIntegerPoint()) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    public void copyTo(State dest) {
        if (dest instanceof IntervalBoxState) {
            this.copyTo((IntervalBoxState) dest);
        } else {
            throw new RuntimeException("Invalid copy attempt");
        }
    }

    public void copyTo(IntervalBoxState dest) {
        for (Entry<Local, Interval32Box> entry : state.entrySet()) {
            Local l = entry.getKey();
            Interval32Box val = entry.getValue();
            // create a new entry
            Interval32Box newVal = new Interval32Box(val);
            dest.update(l, newVal);
        }
    }

    public State copy() {
        IntervalBoxState copy = new IntervalBoxState(this.state.keySet(), false);
        this.copyTo(copy);
        return copy;
    }

    public Map<Local, Interval32Box> getMap() {
        return state;
    }

    public void update(Local l, Interval32Box b) {
        state.put(l, b);
    }

    public Interval32Box getValue(Local l) {
        return state.get(l);
    }

    public void mergeWith(State in) {
        if (in instanceof IntervalBoxState) {
            mergeWith((IntervalBoxState) in);
        } else {
            throw new RuntimeException("Invalid merge. The types are wrong!");
        }
    }

    public void mergeWith(IntervalBoxState in) {
        // merge in1 and in2 and assign the result to this
        for (Local l : state.keySet()) {
            getValue(l).upperBoundAssign(in.getValue(l)); // smallet box containing the union of two
        }

    }

    public void widenWith(State prevBeforeFlow) {
        if (prevBeforeFlow instanceof IntervalBoxState) {
            widenWith((IntervalBoxState) prevBeforeFlow);
        } else {
            throw new RuntimeException("Invalid widen. The types are wrong!");
        }
    }

    public void widenWith(IntervalBoxState prevBeforeFlow) {
        for(Local l : state.keySet()){
            getValue(l).wideningAssign(prevBeforeFlow.getValue(l));
        }
    }

    public boolean isSubset(State inState) {
        if (inState instanceof IntervalBoxState) {
            return this.isSubset((IntervalBoxState) inState);
        } else {
            throw new RuntimeException("invalid type for subset checks");
        }
    }

    public boolean isSubset(IntervalBoxState inState) {
        Set<Local> keys = new HashSet<>();
        keys.addAll(this.state.keySet());
        keys.addAll(inState.state.keySet());
        boolean subset = keys.stream().map(k -> {
                Optional<Interval32Box> oa = Optional.ofNullable(this.state.get(k));
                Optional<Interval32Box> ob = Optional.ofNullable(inState.state.get(k));
                return oa.map(a -> ob.map(b -> a.isSubset(b)).orElse(false)).orElse(false);
            }).reduce((a, b) -> a && b).orElse(false);
        return subset;
    }

    /**
     * the algorithms is from https://en.wikipedia.org/wiki/Interval_arithmetic
     * [x1,x2] op [y1,y2]
     *
     * @param lhs
     * @param rhs
     * @param type of the operation: 0 - addition, 1 - subtraction, 2 -
     *             multiplication, 3 - division
     * @return
     */
    public static Interval32Box transferBinary(Interval32Box lhs, Interval32Box rhs, BinaryOperatorType operator) {
        Interval32Box ret;
        // find low of lhs
        if (lhs.isBottom() || rhs.isBottom()) {
            ret = Interval32Box.BOT();
        } else {
            switch (operator) {
            case ADDITION:
                ret = Interval32Box.add(lhs, rhs);
                break;
            case SUBTRACTION:
                ret = Interval32Box.subtract(lhs, rhs);
                break;
            case MULTIPLICATION:
                ret = Interval32Box.multiply(lhs, rhs);
                break;
            case DIVISION:
                ret = Interval32Box.divide(lhs, rhs);
                break;
            case MODULUS:
            case BAND:
            case BOR:
            case BSHL:
            case BSHR:
            case BUSHR:
            case XOR:
            case INVALID:
            default:
                // use default max values
                ret = Interval32Box.TOP();
                break;
            }
        }
        return ret;
    }

    /**
     * Returns the negated copy inBox argument [x1,x2]
     *
     * @param inBox
     * @return [-x2,-x1]
     */
    public static Interval32Box negate(Interval32Box inBox) {
        inBox.negate();
        return new Interval32Box(inBox);
    }

    public static Interval32Box constant(int val) {
        Interval32Box ret = new Interval32Box(val, val);
        return ret;
    }

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperatorType operator) {
        if (inState instanceof IntervalBoxState) {
            updateState(lVar, (IntervalBoxState) inState, left, right, operator);
        } else {
            throw new RuntimeException("Invalid types for update state");
        }
    }

    public void updateState(Local lVar,
                            IntervalBoxState inState,
                            Value left,
                            Value right,
                            BinaryOperatorType operator) {
        Interval32Box leftBox = eval(inState, left);
        Interval32Box rightBox = eval(inState, right);
        state.put(lVar, transferBinary(leftBox, rightBox, operator));
    }

    private static Interval32Box eval(IntervalBoxState inState, Value v) {
        Interval32Box ret = null;
        if (v instanceof IntConstant) {
            ret = constant(((IntConstant) v).value);
        } else if (v instanceof Local) {
            ret = inState.getValue((Local) v);
        } else {
            ret = Interval32Box.TOP();
        }

        return ret;
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof IntervalBoxState) {
            updateState(lVar, (IntervalBoxState) inState, v);
        } else {
            throw new RuntimeException("Invalid types for update state");
        }
    }

    public void updateState(Local lVar, IntervalBoxState inState, Value v) {
        if (v instanceof JNegExpr) {
            v = ((JNegExpr) v).getOp();
            state.put(lVar, negate(eval(inState, v)));
        } else {
            state.put(lVar, eval(inState, v));
        }
    }

    @Override
    public String toString() {
        List<Local> locals = this.state.keySet()
            .stream()
            .sorted((a, b) -> (a.toString().compareTo(b.toString())))
            .collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Local local : locals) {
            sb.append(local);
            sb.append("=");
            sb.append(this.state.get(local).toString());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("}");
        return sb.toString();
    }

    public String toSMT(SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        for (Local l : this.state.keySet()) {
            sb.append(l.toString());
            sb.append("->");
            sb.append(this.toSMT(l, solver));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toSMT(Local l, SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        Interval32Box interval = this.state.get(l);
        sb.append(solver.smt2(interval.toGrimpExpr(l)));
        return sb.toString();
    }

    public Graph<Local, DBSConstraint> toGraph() {
        Graph<Local, DBSConstraint> graph = new DefaultDirectedGraph<>(DBSConstraint.class);
        graph.addVertex(Variable.ZERO);
        this.state.forEach((l, i) -> {
                graph.addVertex(l);
                graph.addEdge(l, Variable.ZERO, DBSConstraint.from(i.upperBound(), i.isBottom()));
                graph.addEdge(Variable.ZERO, l, DBSConstraint.from(i.lowerBound().map(b -> b * -1), i.isBottom()));
            });
        return graph;
    }

    @Override
    public boolean equals(Object a) {
        boolean equal = false;
        if (a == null || !(a instanceof IntervalBoxState)) {
            equal = false;
        } else {
            equal = this.state.equals(((IntervalBoxState)a).state);
        }
        return equal;
    }

    public void forget(Local local) {
        this.updateTop(local);
    }

    public void updateTop(Local l) {
        state.put(l, Interval32Box.TOP());
    }

    public void makeInfeasible() {
        for (Local l : this.state.keySet()) {
            this.state.put(l, Interval32Box.BOT());
        }
    }

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof IntervalBoxState) {
            return updateCond((IntervalBoxState) inState, left, right, type);
        } else {
            throw new RuntimeException("the types are wrong!");
        }
    }

    public boolean updateCond(IntervalBoxState inState, Value left, Value right, PredicateType type) {
        // update to new values so that the condition holds with that type
        Interval32Box leftBox = eval(inState, left);
        Interval32Box rightBox = eval(inState, right);
        List<Interval32Box> result = Interval32Box.transferCondition(leftBox, rightBox, type);
        if (left instanceof Local) {
            state.put((Local) left, result.get(0));
        }
        if (right instanceof Local) {
            state.put((Local) right, result.get(1));
        }
        return this.isFeasible();
    }
}
