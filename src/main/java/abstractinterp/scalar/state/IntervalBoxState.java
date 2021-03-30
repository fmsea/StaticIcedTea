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

public class IntervalBoxState {
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

    public void copyTo(IntervalBoxState dest) {
        for (Entry<Local, Interval32Box> entry : state.entrySet()) {
            Local l = entry.getKey();
            Interval32Box val = entry.getValue();
            // create a new entry
            Interval32Box newVal = new Interval32Box(val);
            dest.update(l, newVal);
        }
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

    public void mergeWith(IntervalBoxState in) {
        // merge in1 and in2 and assign the result to this
        for (Local l : state.keySet()) {
            getValue(l).upperBoundAssign(in.getValue(l)); // smallet box containing the union of two
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
    public static Interval32Box transferBinary(Interval32Box lhs, Interval32Box rhs, byte type) {
        Interval32Box ret;
        // find low of lhs
        if (lhs.isBounded() && rhs.isBounded()) {
            // isBounded suggests none of these Integers are null
            int x1 = lhs.lowerBound().intValue();
            int x2 = lhs.upperBound().intValue();
            int y1 = rhs.lowerBound().intValue();
            int y2 = rhs.upperBound().intValue();
            // adding them up
            int new_high = Integer.MAX_VALUE;
            int new_low = Integer.MIN_VALUE;
            switch (type) {
            case 0:
                new_low = x1 == Integer.MIN_VALUE || y1 == Integer.MIN_VALUE ? Integer.MIN_VALUE : x1 + y1;
                new_high = x2 == Integer.MAX_VALUE || y2 == Integer.MAX_VALUE ? Integer.MAX_VALUE : x2 + y2;
                break;
            case 1:
                new_low = x1 - y2; // do more checks here too
                new_high = x2 - y1;
                break;
            case 2:
                new_high = Math.max(x1 * y1, Math.max(x1 * y2, Math.max(x2 * y1, x2 * y2)));
                new_low = Math.min(x1 * y1, Math.min(x1 * y2, Math.min(x2 * y1, x2 * y2)));
                break;
            case 3:
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

    public void updateState(Local lVar, IntervalBoxState inState, Value left, Value right, byte type) {
        Interval32Box leftBox = eval(inState, left);
        Interval32Box rightBox = eval(inState, right);
        state.put(lVar, transferBinary(leftBox, rightBox, type));
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

    public void updateCond(IntervalBoxState inState, Value left, Value right, byte type) {
        // update to new values so that the condition holds with that type
        Interval32Box leftBox = eval(inState, left);
        Interval32Box rightBox = eval(inState, right);
        List<Interval32Box> result = transferCond(leftBox, rightBox, type);
        if (left instanceof Local) {
            state.put((Local) left, result.get(0));
        }
        if (right instanceof Local) {
            state.put((Local) right, result.get(1));
        }
    }

    public static List<Interval32Box> transferCond(Interval32Box lhs, Interval32Box rhs, byte type) {
        List<Interval32Box> ret = new ArrayList<Interval32Box>(2);
        if (lhs.isBounded() && rhs.isBounded()) {
            int x1 = lhs.lowerBound().intValue();
            int x2 = lhs.upperBound().intValue();
            int y1 = rhs.lowerBound().intValue();
            int y2 = rhs.upperBound().intValue();
            // lhs [x1,x2], rhs [y1,y2] returns the true evals
            byte position = -1;
            // [x1,x2] ... [y1,y2]
            if (x2 < y1) {
                position = 0;
            } else if (x1 < y1 && y1 <= x2 && x2 < y2) {
                // [x1, y1, x2, y2]
                position = 1;
            } else if (x1 <= y1 && y2 <= x2) {
                // [x1,y1,y2,x2]
                position = 2;
            } else if (y1 < x1 && x1 <= y2 && y2 < x2) {
                // [y1,x1,y2,x2]
                position = 3;
            } else if (y2 < x1) {
                // [y1,y2] .. [x1,x2]
                position = 4;
            } else if (y1 <= x1 && x2 <= y2) {
                // [y1,x1,x2,y2]
                position = 5;
            } else {
                System.err.println("Not considered position cases");
            }
            int x2_new = x2;
            int x1_new = x1;
            int y2_new = y2;
            int y1_new = y1;
            switch (type) {
            case 0: // equal
                switch (position) {
                case 0:// infeasible if do not intersect
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 1:// common elements [y1,x2]
                    x1_new = y1;
                    y2_new = x2;
                    break;
                case 2:// inner interval [y1,y2]
                    x1_new = y1;
                    x2_new = y2;
                    break;
                case 3: // common elements [x1,y2]
                    x2_new = y2;
                    y1_new = x1;
                    break;
                case 4:// infeasible if do not intersect
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 5:// inner interval [x1,x2]
                    y2_new = x2;
                    y1_new = x1;
                    break;
                }
                break;
            case 1: // not equal
                // check if there is only one point at it is is the same point
                if (x1 == x2 && x2 == y2 && y2 == y1) {
                    // infeasible
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                }
                // otherwise the previous values
                break;
            case 2: // <=
                switch (position) {
                case 0:// all x's are <= than y's so leave the same
                    break;
                case 1:
                    // leave the same
                    break;
                case 2:
                    // remove some x's that are grater than y2
                    x2_new = y2;
                    break;
                case 3:
                    // remove some y's that are less than x1 and some x's that are grater than y2
                    y1_new = x1;
                    x2_new = y2;
                    break;
                case 4:// infeasible
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 5:
                    // remove some y's that are less than x1
                    y1_new = x1;
                    break;
                }
                break;
            case 3: // < - should be similar to case 3, just need to add +1 ?
                switch (position) {
                case 0:// the same
                    break;
                case 1: // leave the same
                    break;
                case 2: // remove some x's that are strictly greater than y2
                    if (x1 == x2 && x2 == y1 && y1 == y2) {
                        // infeasible
                        x2_new = Integer.MIN_VALUE;
                        x1_new = Integer.MAX_VALUE;
                        y2_new = Integer.MIN_VALUE;
                        y1_new = Integer.MAX_VALUE;
                    } else {
                        x2_new = y2 - 1;// it's ok to leave x1 since there are some elements in y that are greater than
                                        // x1
                        if (x1 == y1) {
                            // increase y1 by one
                            y1_new = y1 + 1;
                        }
                    }
                    break;
                case 3:// shift x by one up and y by one down
                    y1_new = x1 + 1;
                    x2_new = y2 - 1;
                    break;
                case 4:// infeasible
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 5: // remove some y's that are less or equal to x1
                    y1_new = x1 + 1;
                    break;
                }
                break;
            case 4: // >=
                switch (position) {
                case 0:
                    // infeasible
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 1: // remove some x and some y
                    x1_new = y1;
                    y2_new = x2;
                    break;
                case 2:
                    x1_new = y1;
                    break;
                case 3:
                    // stay the same
                    break;
                case 4:
                    // stay the same
                    break;
                case 5:
                    y1_new = x1;
                    y2_new = x2;
                    break;
                }
                break;
            case 5: // > similar to case 4 only +/- 1
                switch (position) {
                case 0:
                    // infeasible
                    x2_new = Integer.MIN_VALUE;
                    x1_new = Integer.MAX_VALUE;
                    y2_new = Integer.MIN_VALUE;
                    y1_new = Integer.MAX_VALUE;
                    break;
                case 1:// remove som x1 and some y and offset by one
                    x1_new = y1 + 1;
                    y2_new = x2 - 1;
                    break;
                case 2:
                    if (x1 == x2 && y2 == x2 && y2 == y1) {
                        // infeasible
                        x2_new = Integer.MIN_VALUE;
                        x1_new = Integer.MAX_VALUE;
                        y2_new = Integer.MIN_VALUE;
                        y1_new = Integer.MAX_VALUE;
                    } else {
                        x1_new = y1 + 1;
                    }
                    break;
                case 3:// stay the same
                    break;
                case 4:// stay the same
                    break;
                case 5:// offset by one
                    y1_new = x1 + 1;
                    y2_new = x2 - 1;
                    break;
                }
                break;
            }
            Interval32Box ret1 = new Interval32Box(x1_new, x2_new);
            Interval32Box ret2 = new Interval32Box(y1_new, y2_new);
            ret.add(ret1);
            ret.add(ret2);
        } else {
            System.err.println("Not dealing with yet");
            ret.add(Interval32Box.TOP());
            ret.add(Interval32Box.TOP());
        }
        return ret;
    }
}
