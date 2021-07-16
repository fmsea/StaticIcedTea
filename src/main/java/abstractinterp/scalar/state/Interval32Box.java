package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.IntConstant;
import soot.jimple.BinopExpr;

import solver.SolverWrapper;

public class Interval32Box {

    private boolean bottom = false;
    private Integer lowerBound = null;
    private Integer upperBound = null;

    public Integer lowerBound() {
        return this.lowerBound;
    }

    public Integer upperBound() {
        return this.upperBound;
    }

    public Interval32Box() {
        super();
    }

    public Interval32Box(Interval32Box box) {
        this(box.lowerBound, box.upperBound, box.bottom);
    }

    public Interval32Box(int bound) {
        this(bound, bound);
    }

    public Interval32Box(int lowerBound, int upperBound) {
        this(Integer.valueOf(lowerBound), Integer.valueOf(upperBound));
    }

    public Interval32Box(Integer lowerBound, Integer upperBound) {
        this(lowerBound, upperBound, !areValidBounds(lowerBound, upperBound));
    }

    private Interval32Box(Integer lowerBound, Integer upperBound, boolean bottom) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.bottom = bottom;
    }

    public static Interval32Box TOP() {
        return new Interval32Box();
    }

    public static Interval32Box MAX() {
        return new Interval32Box(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static Interval32Box BOT() {
        return new Interval32Box(null, null, true);
    }

    public boolean isValid() {
        return (!this.bottom && (!this.isBounded() || this.lowerBound <= this.upperBound));
    }

    private static boolean areValidBounds(Integer lower, Integer upper) {
        return (lower == null || upper == null || lower <= upper);
    }

    private boolean checkAndSetBottom() {
        boolean valid = isValid();
        this.bottom = !valid;
        return valid;
    }

    public boolean isBottom() {
        return this.bottom;
    }

    public boolean isTop() {
        return (!this.isBottom() && !this.isLowerBounded() && !this.isUpperBounded());
    }

    public boolean isMax() {
        return (!this.isBottom() &&
                this.isBounded() &&
                this.lowerBound.equals(Integer.MIN_VALUE) &&
                this.upperBound.equals(Integer.MAX_VALUE));
    }

    public boolean isLowerBounded() {
        return (this.bottom == false && this.lowerBound != null);
    }

    public boolean isUpperBounded() {
        return (this.bottom == false && this.upperBound != null);
    }

    public boolean isBounded() {
        return isLowerBounded() && isUpperBounded();
    }

    public boolean containsIntegerPoint() {
        // very rough translation from ppl/Interval_defs.hh
        return (this.isValid() && (this.isTop() || this.lowerBound <= this.upperBound));
    }

    private void minAssign(Interval32Box box) {
        if (!this.isLowerBounded()) {
            this.lowerBound = box.lowerBound;
        } else if (box.isLowerBounded() && this.lowerBound > box.lowerBound) {
            this.lowerBound = box.lowerBound;
        }
    }

    private void maxAssign(Interval32Box box) {
        if (!this.isUpperBounded()) {
            this.upperBound = box.upperBound;
        } else if (box.isUpperBounded() && this.upperBound < box.upperBound) {
            this.upperBound = box.upperBound;
        }
    }

    private void minWidenAssign(Interval32Box box) {
        if ((!this.isLowerBounded() && !box.isLowerBounded()) ||
            (this.isLowerBounded() && !box.isLowerBounded())) {
            // skip?
        } else if (!this.isLowerBounded() && box.isLowerBounded()) {
            this.lowerBound = box.lowerBound;
        } else if (box.lowerBound < this.lowerBound) {
            this.lowerBound = Integer.MIN_VALUE;
        }
    }

    private void maxWidenAssign(Interval32Box box) {
        if ((!this.isUpperBounded() && !box.isUpperBounded()) ||
            (this.isUpperBounded() && !box.isUpperBounded())) {
            // skip?
        } else if (!this.isUpperBounded() && box.isUpperBounded()) {
            this.upperBound = box.upperBound;
        } else if (box.upperBound > this.upperBound) {
            this.upperBound = Integer.MAX_VALUE;
        }
    }

    public void upperBoundAssign(Interval32Box box) {
        if (this.isTop() || box.isTop()) {
            this.bottom = false;
            this.lowerBound = null;
            this.upperBound = null;
        } else {
            this.bottom = this.bottom && box.bottom;
            this.minAssign(box);
            this.maxAssign(box);
        }
    }

    public void wideningAssign(Interval32Box box) {
        if (this.isTop() || box.isTop()) {
            this.lowerBound = null;
            this.upperBound = null;
        } else {
            minWidenAssign(box);
            maxWidenAssign(box);
        }
        this.bottom = this.bottom && box.bottom;
    }

    public byte intersectionPosition(Interval32Box box) {
        byte position = -1;
        if (box == null ||
            this.bottom ||
            box.bottom ||
            !(this.isBounded() || box.isBounded())) {
            position = -1;
        } else if (this.isTop()) {
            position = 2;
        } else if (box.isTop()) {
            position = 5;
        } else if (this.upperBound.compareTo(box.lowerBound) < 0) {
            // [x1,x2] ... [y1,y2]
            position = 0;
        } else if (this.lowerBound.compareTo(box.lowerBound) < 0 &&
                   box.lowerBound.compareTo(this.upperBound) <= 0 &&
                   this.upperBound.compareTo(box.upperBound) < 0) {
            // [x1, y1, x2, y2]
            position = 1;
        } else if (this.lowerBound.compareTo(box.lowerBound) <= 0 &&
                   box.upperBound.compareTo(this.upperBound) <= 0) {
            // [x1,y1,y2,x2]
            position = 2;
        } else if (box.lowerBound.compareTo(this.lowerBound) < 0 &&
                   this.lowerBound.compareTo(box.upperBound) <= 0 &&
                   box.upperBound.compareTo(this.upperBound) < 0) {
            // [y1,x1,y2,x2]
            position = 3;
        } else if (box.upperBound.compareTo(this.lowerBound) < 0) {
            // [y1,y2] .. [x1,x2]
            position = 4;
        } else if (box.lowerBound.compareTo(this.lowerBound) <= 0 &&
                   this.upperBound.compareTo(box.upperBound) <= 0) {
            // [y1,x1,x2,y2]
            position = 5;
        }
        return position;
    }

    public static List<Interval32Box> transferCondition(Interval32Box lhs,
                                                        Interval32Box rhs,
                                                        PredicateType type) {
        List<Interval32Box> ret = new ArrayList<>(2);
        if (lhs.isBottom() || rhs.isBottom()) {
            ret.add(BOT());
            ret.add(BOT());
        } else if (lhs.isBounded() && rhs.isBounded()) {
            switch (type) {
            case Eq:
                ret.addAll(transferConditionEq(lhs, rhs));
                break;
            case Ne:
                ret.addAll(transferConditionNe(lhs, rhs));
                break;
            case Le:
                ret.addAll(transferConditionLe(lhs, rhs));
                break;
            case Lt:
                ret.addAll(transferConditionLt(lhs, rhs));
                break;
            case Ge:
                ret.addAll(transferConditionGe(lhs, rhs));
                break;
            case Gt:
                ret.addAll(transferConditionGt(lhs, rhs));
                break;
            case Invalid:
                ret.add(BOT());
                ret.add(BOT());
                break;
            }
        } else {
            ret.add(TOP());
            ret.add(TOP());
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionEq(Interval32Box lhs,
                                                           Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        byte position = lhs.intersectionPosition(rhs);
        switch (position) {
        case 0:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 1: // common elements [rhs.l, lhs.u]
            ret.add(new Interval32Box(rhs.lowerBound, lhs.upperBound));
            ret.add(new Interval32Box(rhs.lowerBound, lhs.upperBound));
            break;
        case 2: // inner rhs interval
            ret.add(new Interval32Box(rhs));
            ret.add(new Interval32Box(rhs));
            break;
        case 3: // common elements [lhs.l, rhs.u]
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
            break;
        case 4:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 5: // inner lhs interval
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(lhs));
            break;
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionNe(Interval32Box lhs,
                                                           Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        // check if all values are equal, otherwise, leave
        if (lhs.equals(rhs)) {
            ret.add(BOT());
            ret.add(BOT());
        } else {
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(rhs));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionLe(Interval32Box lhs,
                                            Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        byte position = lhs.intersectionPosition(rhs);
        switch (position) {
        case 0: // lhs <= rhs
        case 1:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(rhs));
            break;
        case 2:
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
            ret.add(new Interval32Box(rhs));
            break;
        case 3:
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
            break;
        case 4:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 5:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(lhs.lowerBound, rhs.upperBound));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionLt(Interval32Box lhs,
                                            Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        byte position = lhs.intersectionPosition(rhs);
        switch (position) {
        case 0:
        case 1:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(rhs));
            break;
        case 2:
            if (lhs.equals(rhs)) {
                ret.add(BOT());
                ret.add(BOT());
            } else {
                ret.add(new Interval32Box(lhs.lowerBound,
                                          Integer.valueOf(rhs.upperBound - 1)));
                if (lhs.lowerBound.equals(rhs.lowerBound)) {
                    ret.add(new Interval32Box(Integer.valueOf(rhs.lowerBound + 1),
                                              rhs.upperBound));
                } else {
                    ret.add(new Interval32Box(rhs));
                }
            }
            break;
        case 3:
            ret.add(new Interval32Box(lhs.lowerBound,
                                      Integer.valueOf(rhs.upperBound - 1)));
            ret.add(new Interval32Box(Integer.valueOf(lhs.lowerBound + 1),
                                      rhs.upperBound));
            break;
        case 4:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 5:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(Integer.valueOf(lhs.lowerBound + 1),
                                      rhs.upperBound));
            break;
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionGe(Interval32Box lhs,
                                                           Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        byte position = lhs.intersectionPosition(rhs);
        switch (position) {
        case 0:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 1:
            ret.add(new Interval32Box(rhs.lowerBound, lhs.upperBound));
            ret.add(new Interval32Box(rhs.lowerBound, lhs.upperBound));
            break;
        case 2:
            ret.add(new Interval32Box(rhs.lowerBound, lhs.upperBound));
            ret.add(new Interval32Box(rhs));
            break;
        case 3:
        case 4:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(rhs));
            break;
        case 5:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(lhs));
            break;
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionGt(Interval32Box lhs,
                                                           Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        byte position = lhs.intersectionPosition(rhs);
        switch (position) {
        case 0:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 1:
            ret.add(new Interval32Box(Integer.valueOf(rhs.lowerBound + 1),
                                      Integer.valueOf(lhs.upperBound)));
            ret.add(new Interval32Box(Integer.valueOf(rhs.lowerBound),
                                      Integer.valueOf(lhs.upperBound - 1)));
            break;
        case 2:
            if (lhs.equals(rhs)) {
                ret.add(BOT());
                ret.add(BOT());
            } else {
                ret.add(new Interval32Box(Integer.valueOf(rhs.lowerBound + 1),
                                          Integer.valueOf(lhs.upperBound)));
                ret.add(new Interval32Box(rhs));
            }
            break;
        case 3:
        case 4:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(rhs));
            break;
        case 5:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(Integer.valueOf(lhs.lowerBound + 1),
                                      Integer.valueOf(lhs.upperBound - 1)));
            break;
        }
        return ret;
    }

    public boolean intersects(Interval32Box box) {
        boolean ret;
        byte position = this.intersectionPosition(box);
        if (position == -1 || position == 0 || position == 4) {
            ret = false;
        } else if (position == 1 ||
                   position == 2 ||
                   position == 3 ||
                   position == 5) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    public void negate() {
        Integer lower = this.upperBound != null ? this.upperBound.intValue() * -1 : null;
        Integer upper = this.lowerBound != null ? this.lowerBound.intValue() * -1 : null;
        this.lowerBound = lower;
        this.upperBound = upper;
        this.checkAndSetBottom();
    }

    public boolean isSingleton() {
        return (!this.isBottom() &&
                this.isBounded() &&
                this.lowerBound.equals(this.upperBound));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Interval32Box) {
            return this.equals((Interval32Box) o);
        } else {
            return false;
        }
    }

    public boolean equals(Interval32Box box) {
        boolean ret;
        if (box == null) {
            ret = false;
        } else if (this.bottom != box.bottom) {
            ret = false;
        } else if (!(this.isBounded() || box.isBounded())) {
            ret = true;
        } else if (!(this.isLowerBounded() ||
                     box.isLowerBounded() ||
                     this.isUpperBounded() ||
                     box.isUpperBounded())) {
            ret = true;
        } else if (this.isLowerBounded() &&
                   this.lowerBound.equals(box.lowerBound) &&
                   this.isUpperBounded() &&
                   this.upperBound.equals(box.upperBound)) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    @Override
    public String toString() {
        if (this.isBottom()) {
            return "⟘";
        } else if (this.isTop()) {
            return "⟙";
        } else if (this.isBounded() && this.lowerBound == this.upperBound) {
            return String.format("%d", this.lowerBound);
        } else {
            return String.format("[%d, %d]", this.lowerBound, this.upperBound);
        }
    }

    public String toSMT(Local local, SolverWrapper solver) {
        return solver.smt2(this.toGrimpExpr(local));
    }

    public BinopExpr toGrimpExpr(Local local) {
        BinopExpr r = null;
        if (this.isBottom()) {
            r = Grimp.v().newAndExpr(Grimp.v().newGeExpr(local, IntConstant.v(0)),
                                     Grimp.v().newLtExpr(local, IntConstant.v(0)));
        } else if (this.isTop()) {
            r = Grimp.v().newOrExpr(Grimp.v().newGeExpr(local, IntConstant.v(0)),
                                    Grimp.v().newLtExpr(local, IntConstant.v(0)));
        } else if (this.isSingleton()) {
            r = Grimp.v().newEqExpr(local, IntConstant.v(this.lowerBound));
        } else if (this.isBounded()) {
            r = Grimp.v().newAndExpr(Grimp.v().newGeExpr(local, IntConstant.v(this.lowerBound)),
                                     Grimp.v().newLeExpr(local, IntConstant.v(this.upperBound)));
        } else if (this.isLowerBounded()) {
            r = Grimp.v().newGeExpr(local, IntConstant.v(this.lowerBound));
        } else if (this.isUpperBounded()) {
            r = Grimp.v().newLeExpr(local, IntConstant.v(this.upperBound));
        } else {
            r = Grimp.v().newAndExpr(Grimp.v().newGeExpr(local, IntConstant.v(Integer.MIN_VALUE)),
                                     Grimp.v().newLeExpr(local, IntConstant.v(Integer.MAX_VALUE)));
        }
        return r;
    }
}
