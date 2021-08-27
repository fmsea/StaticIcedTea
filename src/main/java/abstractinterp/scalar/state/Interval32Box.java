package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.IntConstant;
import soot.jimple.BinopExpr;

import solver.SolverWrapper;

public class Interval32Box {

    private boolean bottom = false;
    private Optional<Integer> lowerBound = Optional.empty();
    private Optional<Integer> upperBound = Optional.empty();

    public Optional<Integer> lowerBound() {
        return this.lowerBound;
    }

    public Integer lowerBoundOrElse() {
        return this.lowerBound.orElse(Integer.MIN_VALUE);
    }

    public Optional<Integer> upperBound() {
        return this.upperBound;
    }

    public Integer upperBoundOrElse() {
        return this.upperBound.orElse(Integer.MAX_VALUE);
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
        this(Optional.of(Integer.valueOf(lowerBound)),
             Optional.of(Integer.valueOf(upperBound)));
    }

    public Interval32Box(Integer lowerBound, Integer upperBound) {
        this(lowerBound != null ? Optional.of(lowerBound) : Optional.empty(),
             upperBound != null ? Optional.of(upperBound) : Optional.empty());
    }

    public Interval32Box(Optional<Integer> lowerBound, Optional<Integer> upperBound) {
        this(lowerBound, upperBound, !areValidBounds(lowerBound, upperBound));
    }

    private Interval32Box(Optional<Integer> lowerBound, Optional<Integer> upperBound, boolean bottom) {
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
        return new Interval32Box(Optional.empty(), Optional.empty(), true);
    }

    public boolean isValid() {
        return (!this.bottom && (!this.isBounded() || this.lowerBound.get() <= this.upperBound.get()));
    }

    private static boolean areValidBounds(Optional<Integer> lower, Optional<Integer> upper) {
        return lower.flatMap(l -> upper.map(u -> l <= u)).orElse(true);
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
                this.lowerBound.get().equals(Integer.MIN_VALUE) &&
                this.upperBound.get().equals(Integer.MAX_VALUE));
    }

    public boolean isLowerBounded() {
        return (this.bottom == false && this.lowerBound.isPresent());
    }

    public boolean isUpperBounded() {
        return (this.bottom == false && this.upperBound.isPresent());
    }

    public boolean isBounded() {
        return isLowerBounded() && isUpperBounded();
    }

    public boolean containsIntegerPoint() {
        return this.isValid();
    }

    private void minAssign(Interval32Box box) {
        if (!this.isLowerBounded() || !box.isLowerBounded()) {
            this.lowerBound = Optional.empty();
        } else if (box.isLowerBounded() && this.lowerBound.get() > box.lowerBound.get()) {
            this.lowerBound = box.lowerBound;
        }
    }

    private void maxAssign(Interval32Box box) {
        if (!this.isUpperBounded() || !box.isUpperBounded()) {
            this.upperBound = Optional.empty();
        } else if (box.isUpperBounded() && this.upperBound.get() < box.upperBound.get()) {
            this.upperBound = box.upperBound;
        }
    }

    private void minWidenAssign(Interval32Box box) {
        if (!this.isLowerBounded() || !box.isLowerBounded()) {
            this.lowerBound = Optional.empty();
        } else if (box.lowerBound.get() < this.lowerBound.get()) {
            this.lowerBound = Optional.of(Integer.MIN_VALUE);
        }
    }

    private void maxWidenAssign(Interval32Box box) {
        if (!this.isUpperBounded() || !box.isUpperBounded()) {
            this.upperBound = Optional.empty();
        } else if (box.upperBound.get() > this.upperBound.get()) {
            this.upperBound = Optional.of(Integer.MAX_VALUE);
        }
    }

    public void upperBoundAssign(Interval32Box box) {
        this.minAssign(box);
        this.maxAssign(box);
        this.bottom = this.bottom && box.bottom;
    }

    public void wideningAssign(Interval32Box box) {
        minWidenAssign(box);
        maxWidenAssign(box);
        this.bottom = this.bottom && box.bottom;
    }

    public byte intersectionPosition(Interval32Box box) {
        byte position = -1;
        if (box == null ||
            this.bottom ||
            box.bottom) {
            position = -1;
        } else if (this.isTop()) {
            position = 2;
        } else if (box.isTop()) {
            position = 5;
        } else if (this.isUpperBounded() &&
                   box.isLowerBounded() &&
                   this.upperBound.get().compareTo(box.lowerBound.get()) < 0) {
            // [x1,x2] ... [y1,y2]
            position = 0;
        } else if (this.isUpperBounded() &&
                   box.isLowerBounded() &&
                   (!this.isLowerBounded() || this.lowerBound.get().compareTo(box.lowerBound.get()) < 0) &&
                   box.lowerBound.get().compareTo(this.upperBound.get()) <= 0 &&
                   (!box.isUpperBounded() || this.upperBound.get().compareTo(box.upperBound.get()) < 0)) {
            // [x1, y1, x2, y2]
            position = 1;
        } else if (box.isBounded() &&
                   (!this.isLowerBounded() || this.lowerBound.get().compareTo(box.lowerBound.get()) <= 0) &&
                   (!this.isUpperBounded() || box.upperBound.get().compareTo(this.upperBound.get()) <= 0)) {
            // [x1,y1,y2,x2]
            position = 2;
        } else if (this.isLowerBounded() &&
                   box.isUpperBounded() &&
                   (!box.isLowerBounded() || box.lowerBound.get().compareTo(this.lowerBound.get()) < 0) &&
                   this.lowerBound.get().compareTo(box.upperBound.get()) <= 0 &&
                   (!this.isUpperBounded() || box.upperBound.get().compareTo(this.upperBound.get()) < 0)) {
            // [y1,x1,y2,x2]
            position = 3;
        } else if (this.isLowerBounded() &&
                   box.isUpperBounded() &&
                   box.upperBound.get().compareTo(this.lowerBound.get()) < 0) {
            // [y1,y2] .. [x1,x2]
            position = 4;
        } else if (this.isBounded() &&
                   (!box.isLowerBounded() || box.lowerBound.get().compareTo(this.lowerBound.get()) <= 0) &&
                   (!box.isUpperBounded() || this.upperBound.get().compareTo(box.upperBound.get()) <= 0)) {
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
        } else {
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
                                          rhs.upperBound.map(u -> Integer.valueOf(u - 1))));
                if (lhs.isLowerBounded() && lhs.lowerBound.equals(rhs.lowerBound)) {
                    ret.add(new Interval32Box(rhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
                                              rhs.upperBound));
                } else {
                    ret.add(new Interval32Box(rhs));
                }
            }
            break;
        case 3:
            ret.add(new Interval32Box(lhs.lowerBound,
                                      rhs.upperBound.map(u -> Integer.valueOf(u - 1))));
            ret.add(new Interval32Box(lhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
                                      rhs.upperBound));
            break;
        case 4:
            ret.add(BOT());
            ret.add(BOT());
            break;
        case 5:
            ret.add(new Interval32Box(lhs));
            ret.add(new Interval32Box(lhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
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
            ret.add(new Interval32Box(rhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
                                      lhs.upperBound));
            ret.add(new Interval32Box(rhs.lowerBound,
                                      lhs.upperBound.map(u -> Integer.valueOf(u - 1))));
            break;
        case 2:
            if (lhs.equals(rhs)) {
                ret.add(BOT());
                ret.add(BOT());
            } else {
                ret.add(new Interval32Box(rhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
                                          lhs.upperBound));
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
            ret.add(new Interval32Box(lhs.lowerBound.map(l -> Integer.valueOf(l + 1)),
                                      lhs.upperBound.map(u -> Integer.valueOf(u - 1))));
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
        Optional<Integer> lower = this.upperBound.map((u) -> u * -1);
        Optional<Integer> upper = this.lowerBound.map((l) -> l * -1);
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
        } else if (this.isSingleton()) {
            return String.format("%d", this.lowerBound.get());
        } else {
            if (this.isLowerBounded() && !this.isUpperBounded()) {
                return String.format("[%d, ∞)", this.lowerBound.get());
            } else if (this.isUpperBounded() && !this.isLowerBounded()) {
                return String.format("(-∞, %d]", this.upperBound.get());
            } else {
            return String.format("[%d, %d]", this.lowerBound.get(), this.upperBound.get());
            }
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
            r = Grimp.v().newEqExpr(local, IntConstant.v(this.lowerBound.get()));
        } else if (this.isBounded()) {
            r = Grimp.v().newAndExpr(Grimp.v().newGeExpr(local, IntConstant.v(this.lowerBound.get())),
                                     Grimp.v().newLeExpr(local, IntConstant.v(this.upperBound.get())));
        } else if (this.isLowerBounded()) {
            r = Grimp.v().newGeExpr(local, IntConstant.v(this.lowerBound.get()));
        } else if (this.isUpperBounded()) {
            r = Grimp.v().newLeExpr(local, IntConstant.v(this.upperBound.get()));
        } else {
            r = Grimp.v().newAndExpr(Grimp.v().newGeExpr(local, IntConstant.v(Integer.MIN_VALUE)),
                                     Grimp.v().newLeExpr(local, IntConstant.v(Integer.MAX_VALUE)));
        }
        return r;
    }
}
