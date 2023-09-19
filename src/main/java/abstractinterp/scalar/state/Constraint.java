package abstractinterp.scalar.state;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.IntConstant;

public class Constraint implements Comparable<Constraint> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constraint.class);
    private Optional<Integer> bound;
    private boolean bottom = false;

    public Constraint(Optional<Integer> bound, boolean bottom) {
        if (bound == null) {
            this.bound = Optional.empty();
        } else {
            this.bound = bound;
        }
        this.bottom = bottom;
    }

    public static Constraint TOP() {
        return new TopConstraint();
    }

    public static Constraint BOT() {
        return new BotConstraint();
    }

    public static Constraint of(int bound) {
        return new Constraint(Optional.of(bound), false);
    }

    public static Constraint of(long bound) {
        if (bound < Integer.MAX_VALUE && bound > Integer.MIN_VALUE) {
            return new Constraint(Optional.of(Math.toIntExact(bound)), false);
        } else {
            return Constraint.TOP();
        }
    }

    public static Constraint of(int bound, boolean bottom) {
        return new Constraint(Optional.of(bound), bottom);
    }

    public static Constraint of(Integer bound) {
        return new Constraint(Optional.ofNullable(bound), false);
    }

    public static Constraint of(Optional<Integer> bound) {
        return new Constraint(bound, false);
    }

    public static Constraint of(Optional<Integer> bound, boolean bottom) {
        return new Constraint(bound, bottom);
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

    public Constraint copy() {
        Constraint c = new Constraint(this.bound, this.bottom);
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

    public Stream<Constraint> stream() {
        if (this.isTop()) {
            return Stream.of();
        } else {
            return Stream.of(this);
        }
    }

    public <U> Optional<U> map(Function<? super Constraint, U> f) {
        if (this.isTop()) {
            return Optional.empty();
        } else {
            return Optional.of(f.apply(this));
        }
    }

    public static Constraint add(Constraint... cs) {
        return Constraint.add(Stream.of(cs));
    }

    public static Constraint add(Stream<Constraint> cs) {
        return cs.reduce((a, b) -> Constraint.add(a, b)).orElse(Constraint.TOP());
    }

    public static Constraint add(Constraint x,
                                 Constraint y) {
        Constraint z = x.copy();
        z.add(y);
        return z;
    }

    public static Constraint subtract(Constraint x,
                                      Constraint y) {
        Constraint z = x.copy();
        z.subtract(y);
        return z;
    }

    public static Constraint multiply(Constraint... cs) {
        return Constraint.multiply(Stream.of(cs));
    }

    public static Constraint multiply(Stream<Constraint> cs) {
        return cs.reduce((a, b) -> Constraint.multiply(a, b)).orElse(Constraint.TOP());
    }

    public static Constraint multiply(Constraint x,
                                      Constraint y) {
        Constraint z = x.copy();
        z.multiply(y);
        return z;
    }

    public static Constraint divide(Constraint x,
                                    Constraint y) {
        Constraint z = x.copy();
        z.divide(y);
        return z;
    }

    public Constraint add(Constraint c) {
        if (this.isBottom() || c.isBottom()) {
            this.makeBottom();
        } else if (this.isTop() || c.isTop()) {
            this.makeTop();
        } else {
            try {
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.addExact(t, b)));
            } catch (ArithmeticException ex) {
                LOGGER.debug("[this = {}, c = {}]", this, c);
                LOGGER.trace("Overflowed!", ex);
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
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.subtractExact(t, b)));
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
                this.bound = this.bound.flatMap(t -> c.bound.map(b -> Math.multiplyExact(t, b)));
            } catch (ArithmeticException ex) {
                this.makeTop();
            }
        }
        return this;
    }

    public Constraint divide(Constraint c) {
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

    public static int compare(Constraint A, Constraint B) {
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

    public static Constraint max(Constraint a, Constraint b) {
        Constraint r = Constraint.BOT();
        int c = compare(a, b);
        if (c <= 0) {
            r = b.copy();
        } else if (c > 0) {
            r = a.copy();
        }
        return r;
    }

    public static Constraint min(Stream<Constraint> cs) {
        return cs.reduce(Constraint::min).orElse(Constraint.BOT());
    }

    public static Constraint min(Constraint... cs) {
        return min(Stream.of(cs));
    }

    public static Constraint min(Constraint a, Constraint b) {
        Constraint r = Constraint.BOT();
        int c = compare(a, b);
        if (c <= 0) {
            r = a.copy();
        } else if (c > 0) {
            r = b.copy();
        }
        return r;
    }

    @Override
    public int compareTo(Constraint other) {
        return Constraint.compare(this, other);
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

    public String toSmt() {
        if (this.isBottom()) {
            return "false";
        } else if (this.isTop()) {
            return "true";
        } else {
            return this.bound.map(b -> {
                    if (b < 0) {
                        return String.format("(- %d)", b * -1);
                    }else {
                        return String.format("%d", b);
                    }
                }).get();
        }
    }
}
