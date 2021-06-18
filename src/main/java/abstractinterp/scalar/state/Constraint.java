package abstractinterp.scalar.state;

import soot.jimple.IntConstant;

public class Constraint implements Comparable<Constraint> {
    private int bound;
    private PredicateType predicate;
    private boolean bottom = false;

    /** Convenience constructor same as (bound, PredicateType.Eq)
     *
     */
    public Constraint(int bound) {
        this(bound, PredicateType.Eq, false);
    }

    /** Construct non-bottom Constraint
     *
     */
    public Constraint(int bound, PredicateType predicate) {
        this(bound, predicate, false);
    }

    /** Copy Constructor
     *
     */
    public Constraint(Constraint src) {
        this(src.bound, src.predicate, src.bottom);
    }

    public Constraint(int bound, PredicateType predicate, boolean bottom) {
        this.bound = bound;
        this.predicate = predicate;
        this.bottom = bottom;
    }

    public int bound() {
        return this.bound;
    }

    public PredicateType predicate() {
        return this.predicate;
    }

    public boolean isBottom() {
        return this.bottom;
    }

    public boolean isTop() {
        return (this.bottom == false &&
                Integer.MAX_VALUE == this.bound &&
                this.predicate == PredicateType.Le);
    }

    public static Constraint TOP() {
        return new TopConstraint();
    }

    public static Constraint BOT() {
        return new BotConstraint();
    }

    public static Constraint ZERO() {
        return new Constraint(0, PredicateType.Eq);
    }

    public Constraint copy() {
        Constraint c = new Constraint(this);
        return c;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof Constraint) {
            equal = this.equals((Constraint) o);
        }
        return equal;
    }

    public boolean equals(Constraint c) {
        return (c != null &&
                this.bottom == c.bottom &&
                this.bound == c.bound &&
                this.predicate == c.predicate);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.bound;
        result = prime * result + this.predicate.hashCode();
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
            sb.append("(");
            sb.append(this.bound);
            sb.append(", ");
            sb.append(this.predicate.toString());
            sb.append(")");
        }
        return sb.toString();
    }

    public static Constraint negate(Constraint a) {
        Constraint c = a.copy();
        c.negate();
        return c;
    }

    public static Constraint add(Constraint a, Constraint b) {
        Constraint c = a.copy();
        c.add(b);
        return c;
    }

    public static Constraint subtract(Constraint a, Constraint b) {
        Constraint c = a.copy();
        c.subtract(b);
        return c;
    }

    public static Constraint multiply(Constraint a, Constraint b) {
        Constraint c = a.copy();
        c.multiply(b);
        return c;
    }

    public static Constraint divide(Constraint a, Constraint b) {
        Constraint c = a.copy();
        c.divide(b);
        return c;
    }

    public Constraint negate() {
        if (!this.isBottom()) {
            this.bound = this.bound * -1;
            this.predicate = this.predicate.negate();
        }
        return this;
    }

    public Constraint add(Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = Math.addExact(this.bound, c.bound);
                this.predicate = PredicateType.superior(this.predicate, c.predicate);
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public Constraint subtract(Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = Math.subtractExact(this.bound, c.bound);
                this.predicate = PredicateType.superior(this.predicate, c.predicate);
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public Constraint multiply(Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = Math.multiplyExact(this.bound, c.bound);
                this.predicate = PredicateType.superior(this.predicate, c.predicate);
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public Constraint divide(Constraint c) {
        if (c.bound == 0 || this.isBottom() || c.isBottom()) {
            // ⟘
            this.makeBottom();
        } else if ((this.isTop() || c.isTop()) ||
                   (this.bound == Integer.MIN_VALUE && c.bound == -1)) {
            this.makeTop();
        } else {
            this.bound = this.bound / c.bound;
            this.predicate = PredicateType.superior(this.predicate, c.predicate);
        }
        return this;
    }

    public Constraint modulus(Constraint c) {
        if (c.bound == 0 || this.isBottom() || c.isBottom()) {
            // ⟘
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            this.bound = this.bound % c.bound;
            this.predicate = PredicateType.superior(this.predicate, c.predicate);
        }
        return this;
    }

    /** Return the smaller constraint
     *
     * @param a Constraint
     * @param b Constraint
     * @return minimum constraint
     */
    public static Constraint min(Constraint a, Constraint b) {
        Constraint t = TOP();
        Constraint c;
        PredicateType predicate;
        if (a.equals(b)) {
            c = a.copy();
        } else if (t.equals(a)) {
            c = b.copy();
        } else if (t.equals(b)) {
            c = a.copy();
        } else if (a.isBottom()) {
            c = b.copy();
        } else if (b.isBottom()) {
            c = a.copy();
        } else if ((predicate = PredicateType.minimum(a.predicate, b.predicate)) == PredicateType.Invalid) {
            c = BOT();
        } else {
            c = new Constraint(Math.min(a.bound, b.bound), predicate);
        }
        return c;
    }

    public static Constraint max(Constraint a, Constraint b) {
        Constraint r;
        int order = a.compareTo(b);
        if (order == 0 || order == 1) {
            r = a.copy();
        } else if (order == -1) {
            r = b.copy();
        } else {
            r = BOT();
        }
        return r;
    }

    public int compareTo(Constraint c) {
        int predicateOrder = PredicateType.compare(this.predicate, c.predicate);
        int order;
        if (this.equals(c)) {
            order = 0;
        } else if (this.equals(TOP())) {
            order = 1;
        } else if (c.equals(TOP())) {
            order = -1;
        } else if (this.equals(BOT())) {
            order = -1;
        } else if (c.equals(BOT())) {
            order = 1;
        } else if (predicateOrder < 0) {
            order = -1;
        } else if (predicateOrder == 0) {
            order = Integer.compare(this.bound, c.bound);
        } else if (predicateOrder == 1) {
            order =  1;
        } else {
            order = 2;
        }
        return order;
    }

    private void makeBottom() {
        this.bound = Integer.MIN_VALUE;
        this.predicate = PredicateType.Lt;
        this.bottom = true;
    }

    private void makeTop() {
        this.bound = Integer.MAX_VALUE;
        this.predicate = PredicateType.Le;
        this.bottom = false;
    }

    public static Constraint transferBinary(IntConstant x,
                                            IntConstant y,
                                            BinaryOperator op) {
        Constraint a = new Constraint(x.value, PredicateType.Eq);
        Constraint b = new Constraint(y.value, PredicateType.Eq);
        Constraint c;
        switch (op) {
        case ADDITION:
            c = a.add(b);
            break;
        case SUBTRACTION:
            c = a.subtract(b);
            break;
        case MULTIPLICATION:
            c = a.multiply(b);
            break;
        case DIVISION:
            c = a.divide(b);
            break;
        case MODULUS:
        case INVALID:
        default:
            c = Constraint.TOP();
            break;
        }
        return c;
    }
}
