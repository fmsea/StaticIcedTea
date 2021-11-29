package abstractinterp.scalar.state;

import java.util.Optional;
import soot.jimple.IntConstant;

class PADO01Constraint implements Comparable<PADO01Constraint> {
    private Optional<Integer> bound;
    private boolean bottom = false;

    public PADO01Constraint(Optional<Integer> bound, boolean bottom) {
        if (bound == null) {
            this.bound = Optional.empty();
        } else {
            this.bound = bound;
        }
        this.bottom = bottom;
    }

    public static PADO01Constraint TOP() {
        return new PADO01TopConstraint();
    }

    public static PADO01Constraint BOT() {
        return new PADO01BotConstraint();
    }

    public static PADO01Constraint of(int bound) {
        return new PADO01Constraint(Optional.of(bound), false);
    }

    public static PADO01Constraint of(int bound, boolean bottom) {
        return new PADO01Constraint(Optional.of(bound), bottom);
    }

    public static PADO01Constraint of(Integer bound) {
        return new PADO01Constraint(Optional.ofNullable(bound), false);
    }

    public static PADO01Constraint of(Optional<Integer> bound) {
        return new PADO01Constraint(bound, false);
    }

    public static PADO01Constraint of(Optional<Integer> bound, boolean bottom) {
        return new PADO01Constraint(bound, bottom);
    }

    public Optional<Integer> bound() {
        return this.bound;
    }

    public boolean isBottom() {
        return (this.bottom == true && this.bound.isEmpty());
    }

    public boolean isTop() {
        return (this.bottom == false && this.bound.isEmpty());
    }

    public PADO01Constraint copy() {
        PADO01Constraint c = new PADO01Constraint(this.bound, this.bottom);
        return c;
    }

    public void makeTop() {
        this.bottom = false;
        this.bound = Optional.empty();
    }

    public void makeBottom() {
        this.bottom = true;
        this.bound = Optional.empty();
    }

    public static PADO01Constraint add(PADO01Constraint x,
                                       PADO01Constraint y) {
        PADO01Constraint z = x.copy();
        z.add(y);
        return z;
    }

    public static PADO01Constraint subtract(PADO01Constraint x,
                                            PADO01Constraint y) {
        PADO01Constraint z = x.copy();
        z.subtract(y);
        return z;
    }

    public static PADO01Constraint multiply(PADO01Constraint x,
                                            PADO01Constraint y) {
        PADO01Constraint z = x.copy();
        z.multiply(y);
        return z;
    }

    public static PADO01Constraint divide(PADO01Constraint x,
                                          PADO01Constraint y) {
        PADO01Constraint z = x.copy();
        z.divide(y);
        return z;
    }

    public PADO01Constraint add(PADO01Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.addExact(t, b)));
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public PADO01Constraint subtract(PADO01Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.subtractExact(t, b)));
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public PADO01Constraint multiply(PADO01Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.multiplyExact(t, b)));
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public PADO01Constraint divide(PADO01Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else if (c.bound.map(b -> b == 0).orElse(false)) {
            this.makeTop();
        } else {
            this.bound = this.bound.flatMap(t -> c.bound.map(b -> t / b));
        }
        return this;
    }

    public static int compare(PADO01Constraint A, PADO01Constraint B) {
        int r;
        if (A.equals(B)) {
            r = 0;
        } else if (A.isBottom() || B.isTop()) {
            r = -1;
        } else if (B.isBottom() || A.isTop()) {
            r = +1;
        } else {
            r = A.bound.map(a -> B.bound.map(b -> Integer.compare(a, b)).orElse(-1)).orElse(+1);
        }
        return r;
    }

    public static PADO01Constraint max(PADO01Constraint a, PADO01Constraint b) {
        PADO01Constraint r = PADO01Constraint.BOT();
        int c = compare(a, b);
        if (c <= 0) {
            r = b.copy();
        } else if (c > 0) {
            r = a.copy();
        }
        return r;
    }

    public static PADO01Constraint min(PADO01Constraint a, PADO01Constraint b) {
        PADO01Constraint r = PADO01Constraint.BOT();
        int c = compare(a, b);
        if (c <= 0) {
            r = a.copy();
        } else if (c > 0) {
            r = b.copy();
        }
        return r;
    }

    @Override
    public int compareTo(PADO01Constraint other) {
        return PADO01Constraint.compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof PADO01Constraint) {
            equal = this.equals((PADO01Constraint) o);
        }
        return equal;
    }

    public boolean equals(PADO01Constraint c) {
        return (c != null &&
                ((this.isBottom() && c.isBottom()) ||
                 (this.bottom == c.bottom &&
                  this.bound.equals(c.bound))));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.bound.hashCode();
        result = prime * result + (this.bottom ? 0 : 1);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.isBottom()) {
            sb.append("⟘");
        } else if (this.isTop()) {
            sb.append("⟙");
        } else {
            // getting here implies there exists a boundary value
            sb.append(this.bound.map(b -> b.toString()).orElse("+∞"));;
        }
        return sb.toString();
    }
}
