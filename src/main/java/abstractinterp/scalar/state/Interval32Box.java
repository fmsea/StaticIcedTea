package abstractinterp.scalar.state;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import soot.Local;
import soot.grimp.Grimp;
import soot.jimple.IntConstant;
import soot.jimple.BinopExpr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import solver.SolverWrapper;

public class Interval32Box implements Comparable<Interval32Box> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Interval32Box.class);

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

    public static Interval32Box of(Interval32Box copy) {
        return new Interval32Box(copy);
    }

    public static Interval32Box of(int singleton) {
        return new Interval32Box(singleton);
    }

    public static Interval32Box of(long singleton) {
        return Interval32Box.of(singleton, singleton);
    }

    public static Interval32Box of(int lowerBound, int upperBound) {
        return new Interval32Box(lowerBound, upperBound);
    }

    public static Interval32Box of(long lowerBound, long upperBound) {
        Optional<Integer> lower = lowerBound < Integer.MIN_VALUE ?
            Optional.empty() :
            Optional.of(Math.toIntExact(lowerBound));
        Optional<Integer> upper = upperBound > Integer.MAX_VALUE ?
            Optional.empty() :
            Optional.of(Math.toIntExact(upperBound));
        return new Interval32Box(lower, upper);
    }

    public static Interval32Box of(Integer lowerBound, Integer upperBound) {
        return new Interval32Box(lowerBound, upperBound);
    }

    public static Interval32Box of(Optional<Integer> lowerBound,
                                   Optional<Integer> upperBound) {
        return new Interval32Box(lowerBound,upperBound);
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
        } else if (this.lowerBound.map(lo -> lo > box.lowerBound.get()).orElse(false)) {
            this.lowerBound = box.lowerBound;
        }
    }

    private void maxAssign(Interval32Box box) {
        if (!this.isUpperBounded() || !box.isUpperBounded()) {
            this.upperBound = Optional.empty();
        } else if (this.upperBound.map(up -> up < box.upperBound.get()).orElse(false)) {
            this.upperBound = box.upperBound;
        }
    }

    private void minWidenAssign(Interval32Box n) {
        boolean shouldWiden = (!this.isLowerBounded() ||
                               !n.isLowerBounded() ||
                               this.lowerBound.flatMap(ml -> n.lowerBound.map(nl -> nl < ml)).orElse(false));
        if (shouldWiden) {
            this.lowerBound = Optional.empty();
        }
    }

    private void maxWidenAssign(Interval32Box n) {
        boolean shouldWiden = (!this.isUpperBounded() ||
                               !n.isUpperBounded() ||
                               this.upperBound.flatMap(mu -> n.upperBound.map(nu -> nu > mu)).orElse(false));
        if (shouldWiden) {
            this.upperBound = Optional.empty();
        }
    }

    public static Interval32Box upperBoundAssign(Interval32Box a, Interval32Box b) {
        Interval32Box c = Interval32Box.of(a);
        c.upperBoundAssign(b);
        return c;
    }

    public void upperBoundAssign(Interval32Box box) {
        if (this.isBottom()) {
            this.lowerBound = box.lowerBound;
            this.upperBound = box.upperBound;
            this.bottom = box.bottom;
        } else if (box.isBottom()) {
            // skip
        } else {
            this.minAssign(box);
            this.maxAssign(box);
        }
        this.checkAndSetBottom();
    }

    public static Interval32Box wideningAssign(Interval32Box m, Interval32Box n) {
        Interval32Box c = Interval32Box.of(m);
        c.wideningAssign(n);
        return c;
    }

    public void wideningAssign(Interval32Box n) {
        // this = m
        if (this.isBottom()) {
            this.lowerBound = n.lowerBound;
            this.upperBound = n.upperBound;
            this.bottom = n.bottom;
        } else if (!n.isBottom()) {
            // ⊃ this ≠ ⟘ ∧ box ≠ ⟘
            minWidenAssign(n);
            maxWidenAssign(n);
            this.checkAndSetBottom();
        }
    }

    public boolean isSubset(Interval32Box box) {
        boolean subset = false;
        if (this.equals(box) || this.isBottom() || box.isTop()) {
            subset = true;
        } else if (box.isBottom()) {
            subset = false;
        } else {
            subset = ((this.lowerBound
                       .map(tl -> box.lowerBound.map(bl -> tl >= bl).orElse(true))
                       .orElse(this.lowerBound.isEmpty() && box.lowerBound.isEmpty())) &&
                      (this.upperBound
                       .map(tu -> box.upperBound.map(bu -> tu <= bu).orElse(true))
                       .orElse(this.upperBound.isEmpty() && box.upperBound.isEmpty())));
        }
        return subset;
    }

    public static Interval32Box add(Interval32Box x, Interval32Box y) {
        Interval32Box r = Interval32Box.TOP();
        Integer l = null;
        Integer u = null;
        if (x.isBottom() || y.isBottom()) {
            r = Interval32Box.BOT();
        } else {
            // lower bound
            if (x.isLowerBounded() && y.isLowerBounded()) {
                try {
                    l = Math.addExact(x.lowerBound.get(), y.lowerBound.get());
                } catch (ArithmeticException ex) {
                }
            }

            // upper bound
            if (x.isUpperBounded() && y.isUpperBounded()) {
                try {
                    u = Math.addExact(x.upperBound.get(), y.upperBound.get());
                } catch (ArithmeticException ex) {
                }
            }

            r.lowerBound = Optional.ofNullable(l);
            r.upperBound = Optional.ofNullable(u);
            r.checkAndSetBottom();
        }
        return r;
    }

    public static Interval32Box subtract(Interval32Box x, Interval32Box y) {
        Interval32Box r = Interval32Box.TOP();
        Integer l = null;
        Integer u = null;

        if (x.isBottom() || y.isBottom()) {
            r = Interval32Box.BOT();
        } else {
            // lower bound
            if (x.isLowerBounded() && y.isUpperBounded()) {
                try {
                    l = Math.subtractExact(x.lowerBound.get(), y.upperBound.get());
                } catch (ArithmeticException ex) {
                }
            }

            // upper bound
            if (x.isUpperBounded() && y.isLowerBounded()) {
                try {
                    u = Math.subtractExact(x.upperBound.get(), y.lowerBound.get());
                } catch (ArithmeticException ex) {
                }
            }

            r.lowerBound = Optional.ofNullable(l);
            r.upperBound = Optional.ofNullable(u);
            r.checkAndSetBottom();
        }
        return r;
    }

    public static Interval32Box multiply(Interval32Box x, Interval32Box y) {
        Interval32Box r = Interval32Box.TOP();
        Integer l = null;
        Integer u = null;

        if (x.isBottom() || y.isBottom()) {
            r = Interval32Box.BOT();
        } else {
            if (x.isBounded() && y.isBounded()) {
                // lower bound
                try {
                    l = Stream.of(new Integer[] {
                            Math.multiplyExact(x.lowerBound.get(), y.lowerBound.get()),
                            Math.multiplyExact(x.lowerBound.get(), y.upperBound.get()),
                            Math.multiplyExact(x.upperBound.get(), y.lowerBound.get()),
                            Math.multiplyExact(x.upperBound.get(), y.upperBound.get()),
                        }).min(Integer::compareTo).orElse(null);
                } catch (ArithmeticException ex) {
                }
                // upper bound
                try {
                    u = Stream.of(new Integer[] {
                            Math.multiplyExact(x.lowerBound.get(), y.lowerBound.get()),
                            Math.multiplyExact(x.lowerBound.get(), y.upperBound.get()),
                            Math.multiplyExact(x.upperBound.get(), y.lowerBound.get()),
                            Math.multiplyExact(x.upperBound.get(), y.upperBound.get()),
                        }).max(Integer::compareTo).orElse(null);
                } catch (ArithmeticException ex) {
                }
            }

            r.lowerBound = Optional.ofNullable(l);
            r.upperBound = Optional.ofNullable(u);
            r.checkAndSetBottom();
        }
        return r;
    }

    public static Interval32Box divide(Interval32Box x, Interval32Box y) {
        Interval32Box r = Interval32Box.TOP();
        Optional<Integer> l = Optional.empty();
        Optional<Integer> u = Optional.empty();

        BiFunction<Integer, Integer, Integer> div = (a, b) -> {
            if (a == Integer.MIN_VALUE && b == -1) {
                // overflow
                return Integer.MAX_VALUE;
            } else {
                return a / b;
            }
        };

        Comparator<Optional<Integer>> optionalMin = new Comparator<>() {
                public int compare(Optional<Integer> a, Optional<Integer> b) {
                    return a.map(av -> b.map(bv -> av.compareTo(bv)).orElse(1)).orElse(-1);
                }
            };

        Comparator<Optional<Integer>> optionalMax = new Comparator<>() {
                public int compare(Optional<Integer> a, Optional<Integer> b) {
                    return a.map(av -> b.map(bv -> av.compareTo(bv)).orElse(-1)).orElse(1);
                }
            };

        if (x.isBottom() || y.isBottom()) {
            r = Interval32Box.BOT();
        } else {
            if (y.isBounded()) {
                if (y.lowerBound().get() > 0 || y.upperBound().get() < 0) {
                    // no zero in y
                    l = Stream.of(x.lowerBound.map(xl -> div.apply(xl, y.lowerBound.get())),
                                  x.lowerBound.map(xl -> div.apply(xl, y.upperBound.get())),
                                  x.upperBound.map(xu -> div.apply(xu, y.lowerBound.get())),
                                  x.upperBound.map(xu -> div.apply(xu, y.upperBound.get()))
                                  ).min(optionalMin).get();
                    u = Stream.of(x.lowerBound.map(xl -> div.apply(xl, y.lowerBound.get())),
                                  x.lowerBound.map(xl -> div.apply(xl, y.upperBound.get())),
                                  x.upperBound.map(xu -> div.apply(xu, y.lowerBound.get())),
                                  x.upperBound.map(xu -> div.apply(xu, y.upperBound.get()))
                                  ).max(optionalMax).get();
                } else if (Integer.valueOf(0).equals(y.lowerBound.get()) &&
                           !Integer.valueOf(0).equals(y.upperBound.get())) {
                    l = Stream.of(x.lowerBound.map(xl -> div.apply(xl, y.upperBound.get())),
                                  x.upperBound.map(xl -> div.apply(xl, y.upperBound.get()))
                                  ).min(optionalMin).get();
                } else if (Integer.valueOf(0).equals(y.upperBound.get()) &&
                           !Integer.valueOf(0).equals(y.lowerBound.get())) {
                    u = Stream.of(x.lowerBound.map(xl -> div.apply(xl, y.lowerBound.get())),
                                  x.upperBound.map(xl -> div.apply(xl, y.lowerBound.get()))
                                  ).max(optionalMax).get();
                }
            }

            r.lowerBound = l;
            r.upperBound = u;
            r.checkAndSetBottom();
        }
        return r;
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
        // lhs = [a, b]
        // rhs = [c, d]
        List<Interval32Box> ret = new ArrayList<>(2);
        if (lhs.lowerBound.flatMap(ll -> rhs.upperBound.map(ru -> ll > ru)).orElse(false) ||
            rhs.lowerBound.flatMap(rl -> lhs.upperBound.map(lu -> rl > lu)).orElse(false)) {
            ret.add(Interval32Box.BOT());
            ret.add(Interval32Box.BOT());
        } else {
            ret.add(Interval32Box.of(negate(minimum(negate(lhs.lowerBound),
                                                    negate(rhs.lowerBound))),
                                     minimum(lhs.upperBound, rhs.upperBound)));
            ret.add(Interval32Box.of(negate(minimum(negate(lhs.lowerBound),
                                                    negate(rhs.lowerBound))),
                                     minimum(lhs.upperBound, rhs.upperBound)));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionNe(Interval32Box lhs,
                                                           Interval32Box rhs) {
        List<Interval32Box> ret = new ArrayList<>(2);
        // check if all values are equal, otherwise, leave
        if (lhs.isSingleton() && rhs.isSingleton() && lhs.equals(rhs)) {
            ret.add(BOT());
            ret.add(BOT());
        } else {
            ret.add(Interval32Box.of(lhs));
            ret.add(Interval32Box.of(rhs));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionLe(Interval32Box lhs,
                                                           Interval32Box rhs) {
        // lhs = [a, b]
        // rhs = [c, d]
        List<Interval32Box> ret = new ArrayList<>(2);
        if (lhs.lowerBound.flatMap(a -> rhs.upperBound.map(d -> a > d)).orElse(false)) {
            ret.add(Interval32Box.BOT());
            ret.add(Interval32Box.BOT());
        } else if (lhs.upperBound.flatMap(b -> rhs.lowerBound.map(c -> b <= c)).orElse(false)) {
            ret.add(Interval32Box.of(lhs));
            ret.add(Interval32Box.of(rhs));
        } else {
            ret.add(Interval32Box.of(negate(minimum(negate(lhs.lowerBound),
                                                    sub(sub(rhs.upperBound, lhs.lowerBound),
                                                        rhs.lowerBound))),
                                     minimum(lhs.upperBound, rhs.upperBound)));
            ret.add(Interval32Box.of(negate(minimum(negate(rhs.lowerBound),
                                                    negate(lhs.lowerBound))),
                                     minimum(rhs.upperBound,
                                             add(sub(rhs.upperBound, lhs.lowerBound),
                                                 lhs.upperBound))));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionLt(Interval32Box lhs,
                                                           Interval32Box rhs) {
        // lhs = [a, b]
        // rhs = [c, d]
        Optional<Integer> a = lhs.lowerBound;
        Optional<Integer> b = lhs.upperBound;
        Optional<Integer> c = rhs.lowerBound;
        Optional<Integer> d = rhs.upperBound;
        List<Interval32Box> ret = new ArrayList<>(2);
        if (a.flatMap(A -> d.map(D -> A >= D)).orElse(false)) {
            ret.add(Interval32Box.BOT());
            ret.add(Interval32Box.BOT());
        } else if (b.flatMap(B -> c.map(C -> B <= C - 1)).orElse(false)) {
            ret.add(Interval32Box.of(lhs));
            ret.add(Interval32Box.of(rhs));
        } else {
            ret.add(Interval32Box.of(a, minimum(b, sub(d, Optional.of(1)))));
            ret.add(Interval32Box.of(negate(minimum(negate(c), add(negate(a), Optional.of(-1)))),
                                     minimum(d, add(d, add(negate(a), b)))));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionGe(Interval32Box lhs,
                                                           Interval32Box rhs) {
        // lhs = [a, b]
        // rhs = [c, d]
        List<Interval32Box> ret = new ArrayList<>(2);
        if (rhs.lowerBound.flatMap(c -> lhs.upperBound.map(b -> c > b)).orElse(false)) {
            ret.add(Interval32Box.BOT());
            ret.add(Interval32Box.BOT());
        } else if (rhs.upperBound.flatMap(d -> lhs.lowerBound.map(a -> d <= a)).orElse(false)) {
            ret.add(Interval32Box.of(lhs));
            ret.add(Interval32Box.of(rhs));
        } else {
            ret.add(Interval32Box.of(negate(minimum(negate(lhs.lowerBound),
                                                    negate(rhs.lowerBound))),
                                     minimum(lhs.upperBound,
                                             add(rhs.upperBound, sub(lhs.upperBound, rhs.lowerBound)))));
            ret.add(Interval32Box.of(negate(minimum(negate(rhs.lowerBound),
                                                    add(lhs.upperBound,
                                                        add(negate(rhs.lowerBound),
                                                            negate(lhs.lowerBound))))),
                                     minimum(rhs.upperBound, lhs.upperBound)));
        }
        return ret;
    }

    private static List<Interval32Box> transferConditionGt(Interval32Box lhs,
                                                           Interval32Box rhs) {
        // lhs = [a, b]
        // rhs = [c, d]
        Optional<Integer> a = lhs.lowerBound;
        Optional<Integer> b = lhs.upperBound;
        Optional<Integer> c = rhs.lowerBound;
        Optional<Integer> d = rhs.upperBound;
        List<Interval32Box> ret = new ArrayList<>(2);
        if (c.flatMap(C -> b.map(B -> C >= B)).orElse(false)) {
            ret.add(Interval32Box.BOT());
            ret.add(Interval32Box.BOT());
        } else if (a.flatMap(A -> d.map(D -> A > D)).orElse(false)) {
            ret.add(Interval32Box.of(lhs));
            ret.add(Interval32Box.of(rhs));
        } else {
            ret.add(Interval32Box.of(negate(minimum(negate(a), sub(negate(c), Optional.of(1)))), b));
            ret.add(Interval32Box.of(negate(minimum(negate(c), add(negate(a), sub(b, c)))),
                                     minimum(d, sub(b, Optional.of(1)))));
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
        return (box != null &&
                ((this.isBottom() && box.isBottom()) ||
                 (this.bottom == box.bottom &&
                  this.lowerBound.equals(box.lowerBound) &&
                  this.upperBound.equals(box.upperBound))));
    }

    public int compareTo(Interval32Box box) {
        return this.upperBound.map(tu -> box.upperBound.map(bu -> Integer.compare(tu, bu)).orElse(-1)).orElse(1);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.lowerBound.hashCode();
        result = prime * result + this.upperBound.hashCode();
        result = prime * result + (this.bottom ? 0 : 1);
        return result;
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
            r = Grimp.v().newEqExpr(IntConstant.v(0), IntConstant.v(1));
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

    private static Optional<Integer> minimum(Optional<Integer> A, Optional<Integer> B) {
        return A.map(a -> B.map(b -> a <= b ? Optional.of(a) : Optional.of(b)).orElse(A)).orElse(B);
    }

    private static Optional<Integer> maximum(Optional<Integer> A, Optional<Integer> B) {
        return A.map(a -> B.map(b -> a <= b ? Optional.of(b) : Optional.of(a)).orElse(A)).orElse(B);
    }

    private static Optional<Integer> add(Optional<Integer> A, Optional<Integer> B) {
        return A.flatMap(a -> B.flatMap(b -> {
                    Optional<Integer> r;
                    try {
                        r = Optional.of(Math.addExact(a, b));
                    } catch (ArithmeticException ex) {
                        r = Optional.empty();
                    }
                    return r;
                }));
    }

    private static Optional<Integer> sub(Optional<Integer> A, Optional<Integer> B) {
        return A.flatMap(a -> B.flatMap(b -> {
                    Optional<Integer> r;
                    try {
                        r = Optional.of(Math.subtractExact(a, b));
                    } catch (ArithmeticException ex) {
                        r = Optional.empty();
                    }
                    return r;
                }));
    }

    private static Optional<Integer> mul(Optional<Integer> A, Optional<Integer> B) {
        return A.flatMap(a -> B.flatMap(b -> {
                    Optional<Integer> r;
                    try {
                        r = Optional.of(Math.multiplyExact(a, b));
                    } catch (ArithmeticException ex) {
                        r = Optional.empty();
                    }
                    return r;
                }));
    }

    private static Optional<Integer> negate(Optional<Integer> A) {
        return mul(A, Optional.of(-1));
    }

    private static int compare(Optional<Integer> A, Optional<Integer> B) {
        int order = -2;
        order = A.map(a -> B.map(b -> Integer.compare(a, b)).orElse(-1)).orElse(+1);
        return order;
    }
}
