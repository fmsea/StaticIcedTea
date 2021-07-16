package abstractinterp.scalar.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;

import solver.SolverWrapper;

public class IntervalBoxState implements State {
    // map of variables to its interval abstract state

    private Map<Local, Interval32Box> state;

    public IntervalBoxState(Set<Local> keys, boolean top) {

        state = new HashMap<Local, Interval32Box>();
        if (top) {
            // maximum integer intervals
            for (Local l : keys) {
                Interval32Box rb = Interval32Box.MAX();
                state.put(l, rb);
            }
        } else {
            // by default create the empty intervals
            for (Local l : keys) {
                Interval32Box rb = Interval32Box.TOP();
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
    public static Interval32Box transferBinary(Interval32Box lhs, Interval32Box rhs, BinaryOperator operator) {
        Interval32Box ret;
        // find low of lhs
        if (lhs.isBottom() || rhs.isBottom()) {
            ret = Interval32Box.BOT();
        } else if (lhs.isBounded() && rhs.isBounded()) {
            // isBounded suggests none of these Integers are null
            int x1 = lhs.lowerBound().intValue();
            int x2 = lhs.upperBound().intValue();
            int y1 = rhs.lowerBound().intValue();
            int y2 = rhs.upperBound().intValue();
            // adding them up
            int new_high = Integer.MAX_VALUE;
            int new_low = Integer.MIN_VALUE;
            switch (operator) {
            case ADDITION:
                new_low = x1 == Integer.MIN_VALUE || y1 == Integer.MIN_VALUE ? Integer.MIN_VALUE : x1 + y1;
                new_high = x2 == Integer.MAX_VALUE || y2 == Integer.MAX_VALUE ? Integer.MAX_VALUE : x2 + y2;
                break;
            case SUBTRACTION:
                new_low = x1 - y2; // do more checks here too
                new_high = x2 - y1;
                break;
            case MULTIPLICATION:
                new_high = Math.max(x1 * y1, Math.max(x1 * y2, Math.max(x2 * y1, x2 * y2)));
                new_low = Math.min(x1 * y1, Math.min(x1 * y2, Math.min(x2 * y1, x2 * y2)));
                break;
            case DIVISION:
                if (y1 > 0 || y2 < 0) {
                    // if 0 not in [y1,y2] range
                    new_high = Math.max(x1 / y2, Math.max(x1 / y1, Math.max(x2 / y2, x2 / y1)));
                    new_low = Math.min(x1 / y2, Math.min(x1 / y1, Math.min(x2 / y2, x2 / y1)));
                } else if (y1 == 0 && y2 != 0) {
                    // y1 is zero but y2 is not
                    new_low = Math.min(x1 / y2, x2 / y2);
                } else if (y2 == 0 && y1 != 0) {
                    // y2 is zero but y1 is not
                    new_high = Math.max(x1 / y1, x2 / y1);
                }
                // 0 in between y1 and y2 - use the top values as set above

                break;// just use the default for now
            case MODULUS:
            case INVALID:
            default:
                // use default max values
                break;
            }
            // create a new constraint
            ret = new Interval32Box(new_low, new_high);
        } else {
            System.err.println("Not dealing yet - we assume there is always an interval");
            ret = Interval32Box.TOP();
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

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperator operator) {
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
                            BinaryOperator operator) {
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
        return state.toString();
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
