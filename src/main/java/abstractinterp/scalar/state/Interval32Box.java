package abstractinterp.scalar.state;

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
        return (!this.isBottom() && !this.isBounded());
    }

    public boolean isMax() {
        return (!this.isBottom() &&
                this.isBounded() &&
                this.lowerBound.equals(Integer.MIN_VALUE) &&
                this.upperBound.equals(Integer.MAX_VALUE));
    }

    public boolean isLowerBounded() {
        return this.lowerBound != null;
    }

    public boolean isUpperBounded() {
        return this.upperBound != null;
    }

    public boolean isBounded() {
        return isLowerBounded() && isUpperBounded();
    }

    public boolean containsIntegerPoint() {
        // very rough translation from ppl/Interval_defs.hh
        return (this.isValid() && (this.isTop() || this.lowerBound <= this.upperBound));
    }

    public void minAssign(Interval32Box box) {
        if (!this.isLowerBounded()) {
            this.lowerBound = box.lowerBound;
        } else if (box.isLowerBounded() && this.lowerBound > box.lowerBound) {
            this.lowerBound = box.lowerBound;
        }
    }

    public void maxAssign(Interval32Box box) {
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
        if (!box.isBottom() && this.isBottom()) {
            // assign box bounds
            this.bottom = false;
            this.lowerBound = box.lowerBound;
            this.upperBound = box.upperBound;
        } else {
            this.minAssign(box);
            this.maxAssign(box);
        }
    }

    public void wideningAssign(Interval32Box box) {
        if (this.isBottom() && !box.isBottom()) {
            this.bottom = false;
        }
        minWidenAssign(box);
        maxWidenAssign(box);
    }

    public void negate() {
        if (this.isBounded()) {
            int lower = this.upperBound.intValue() * -1;
            int upper = this.lowerBound.intValue() * -1;
            this.lowerBound = Integer.valueOf(lower);
            this.upperBound = Integer.valueOf(upper);
        } else if (this.isLowerBounded()) {
            int upper = this.lowerBound.intValue() * -1;
            this.lowerBound = null;
            this.upperBound = Integer.valueOf(upper);
        } else if (this.isUpperBounded()) {
            int lower = this.upperBound.intValue() * -1;
            this.upperBound = null;
            this.lowerBound = Integer.valueOf(lower);
        } else {
            // Bottom, leave it alone
        }
        this.checkAndSetBottom();
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
}
