package abstractinterp.scalar.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;

import solver.SolverWrapper;

public class DifferenceBoundedState implements State {

    private DifferenceBoundedGraph dbs;
    private static Logger LOGGER = LoggerFactory.getLogger(DifferenceBoundedState.class);
    public static final Local ZERO = Variable.ZERO;

    public DifferenceBoundedState(Set<Local> locals, boolean top) {
        Set<Local> localsWithZero = new HashSet<>();
        localsWithZero.addAll(locals);
        localsWithZero.add(ZERO);
        this.dbs = new DifferenceBoundedGraph(localsWithZero);
    }

    public DifferenceBoundedState(DifferenceBoundedState state) {
        this(state.getLocals(), false);
        state.dbs.copyTo(this.dbs);
    }

    /** Return the set of known locals of the graph.
     */
    public Set<Local> getLocals() {
        return this.dbs.getLocals();
    }

    public State copy() {
        return new DifferenceBoundedState(this);
    }

    public void copyTo(State destination) {
        if (destination instanceof DifferenceBoundedState) {
            this.copyTo((DifferenceBoundedState) destination);
        } else {
            throw new RuntimeException("invalid type for copyTo");
        }
    }

    /** Add a constraint through the ZERO element.
     *
     * We remove any existing edge before adding the new edge/constraint.
     */
    public void add(Local l, Constraint constraint) {
        this.add(l, ZERO, constraint);
    }

    /** Add a constraint through the two provided locals.
     *
     * The ordering implies the relationship between variables:
     * l - r < c
     *
     * Remove the edge before adding the new edge of the constraint.
     */
    public void add(Local l, Local r, Constraint constraint) {
        this.dbs.add(l, r, constraint);
    }

    public void addWithNarrowing(Local l, Local r, Constraint c) {
        Constraint constraint = this.dbs.getValue(l, r)
            .map(k -> Constraint.narrow(c, k))
            .orElse(c);
        this.add(l, r, constraint);
    }

    /** Get the constraint connecting <i>l</i> through <i>ZERO</i>
     *
     * If no edge directly connects the two locals, we return TOP.
     *
     * This method shall never return null.
     */
    public Constraint getConstraint(Local l) {
        return this.eval(l);
    }

    /** Get the constraint connecting the two locals.
     *
     * If no edge directly connects the two locals, we return TOP.
     *
     * This method shall never return null.
     */
    public Constraint getConstraint(Local s, Local t) {
        return this.eval(s, t);
    }

    public Optional<Constraint> getValue(Local l) {
        return this.getValue(l, ZERO);
    }

    /** Get the constraint connecting the two locals.
     *
     * If no edge directly connects the two locals, we return TOP.
     *
     * This method shall never return null.
     */
    public Optional<Constraint> getValue(Local s, Local t) {
        return this.dbs.getValue(s, t);
    }

    private Constraint eval(IntConstant constant) {
        return new Constraint(constant.value, PredicateType.Eq);
    }

    private Constraint eval(Local l) {
        return this.eval(l, ZERO);
    }

    private Constraint eval(Local l, Local r) {
        return this.dbs.eval(l, r);
    }

    public boolean isFeasible() {
        return this.dbs.isFeasible();
    }


    public void widenWith(State inState) {
        if (inState instanceof DifferenceBoundedState) {
            widenWith((DifferenceBoundedState) inState);
        } else {
            throw new RuntimeException("invlaid types for widen");
        }
    }

    public void widenWith(DifferenceBoundedState inState) {
        LOGGER.debug("Widening ...");
        this.dbs.widenWith(inState.dbs);
    }

    public void mergeWith(State inState) {
        if (inState instanceof DifferenceBoundedState) {
            this.union((DifferenceBoundedState) inState);
        } else {
            throw new RuntimeException("invalid types for merge");
        }
    }

    public void mergeWith(DifferenceBoundedState inState) {
        // To ensure the best results, both graphs need to be "strongly closed"...
        LOGGER.debug("Merging Paths...");
        this.dbs.union(inState.dbs);
    }

    public void union(DifferenceBoundedState inState) {
        LOGGER.debug("joining paths...");
        this.dbs.union(inState.dbs);
    }

    public void intersection(DifferenceBoundedState inState) {
        LOGGER.debug("intersecting paths...");
        this.dbs.intersection(inState.dbs);
    }

    public boolean isSubset(State inState) {
        if (inState instanceof DifferenceBoundedState) {
            return this.isSubset((DifferenceBoundedState) inState);
        } else {
            throw new RuntimeException("invalid types for subset checks");
        }
    }

    public boolean isSubset(DifferenceBoundedState inState) {
        return false;
    }

    public static State initialFlow(Set<Local> locals, boolean top) {
        return new DifferenceBoundedState(locals, top);
    }

    public void copyTo(DifferenceBoundedState out) {
        if (out != null) {
            this.dbs.copyTo(out.dbs);
        }
    }

    public static Constraint negate(Constraint constraint) {
        Constraint c = constraint.copy();
        c.negate();
        return c;
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof DifferenceBoundedState) {
            updateState(lVar, (DifferenceBoundedState) inState, v);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    /** Assign lVar to the unary expression of v
     *
     */
    public void updateState(Local lVar, DifferenceBoundedState inState, Value v) {
        // we are assigning a value to lVar, no existing relationships hold.
        this.forgetConstraints(lVar);

        if (v instanceof JNegExpr) {
            v = ((JNegExpr) v).getOp();
            Constraint c = eval(inState, v);
            this.add(lVar, c.negate());
        } else if (v instanceof IntConstant) {
            IntConstant ic = (IntConstant) v;
            Constraint c = new Constraint(ic.value, PredicateType.Eq);
            this.add(lVar, ZERO, c);
        } else if (v instanceof Local) {
            this.add(lVar, (Local) v, new Constraint(0, PredicateType.Eq));
        } else {
            this.add(lVar, ZERO, Constraint.TOP());
        }
    }

    public void updateState(Local lVar,
                            State inState,
                            Value left,
                            Value right,
                            BinaryOperatorType operator) {
        if (inState instanceof DifferenceBoundedState) {
            updateState(lVar, (DifferenceBoundedState) inState, left, right, operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    /** Assign lVar to the result of the provided binary expression
     *
     * This method does the type checking to determine which overloaded
     * updateState should be called.  See the overloaded methods for more
     * details about the implementation.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Value left,
                            Value right,
                            BinaryOperatorType type) {
        LOGGER.debug("{} = {} ({}) {}", lVar, left, type, right);
        LOGGER.trace("inState: {}", inState);
        if (left instanceof Local && right instanceof IntConstant) {
            this.updateState(lVar, inState, (Local) left, (IntConstant) right, type);
        } else if (left instanceof IntConstant &&
                   right instanceof Local) {
            this.updateState(lVar, inState, (IntConstant) left, (Local) right, type);
        } else if (left instanceof Local && right instanceof Local) {
            this.updateState(lVar, inState, (Local) left, (Local) right, type);
        } else {
            this.add(lVar, ZERO, Constraint.TOP());
        }
    }

    /** Assign lVar value of constant arithmetic
     *
     * It's unlikely this method is necessary since Soot compiles out
     * (optimizes) constant arithmetic.  We leave it here for testing and
     * completeness.  However, since we are currently modeling 32-bit Integers,
     * the Constraint arithmetic goes to TOP on overflow and underflow
     * conditions.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            IntConstant left,
                            IntConstant right,
                            BinaryOperatorType op) {
        Constraint c = Constraint.transferBinary(left, right, op);
        this.add(lVar, ZERO, c);
    }

    /** Assign lVar constraint with respect to a single local.
     *
     * We distinguish between whether the local on the right hand side *is*
     * lVar or a different local.
     *
     * In the case that, lVar == left, we update the edges of lVar according to
     * the constant `right`.  If lVar != left, we remove all edges containing
     * lVar and add the assignment of the current expression.  However, if the
     * expression involves multiplication or division, we project to the
     * interval domain and compute the result, otherwise, we set the relation
     * between lVar and left to TOP.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Local left,
                            IntConstant right,
                            BinaryOperatorType op) {
        Optional<Constraint> leftConstraint;
        Constraint rightConstraint = this.eval(right);
        switch (op) {
        case ADDITION:
            if (lVar.equals(left)) {
                Constraint c = this.eval(right);
                this.dbs.addOutgoingFrom(lVar, c, inState.dbs);
                this.dbs.subIncomingFrom(lVar, c, inState.dbs);
            } else {
                forgetConstraints(lVar);
                this.add(lVar, left, new Constraint(right.value));
            }
            break;
        case SUBTRACTION:
            if (lVar.equals(left)) {
                Constraint c = this.eval(right);
                this.dbs.subOutgoingFrom(lVar, c, inState.dbs);
                this.dbs.addIncomingFrom(lVar, c, inState.dbs);
            } else {
                forgetConstraints(lVar);
                this.add(lVar, left, new Constraint(right.value * -1));
            }
            break;
        case MULTIPLICATION:
            this.incrementalClosure(lVar, this);
            this.incrementalClosure(left, this);
            forgetConstraints(lVar);
            if ((leftConstraint = this.getValue(left, ZERO)).isPresent()) {
                Constraint c = leftConstraint.get();
                this.add(lVar, Constraint.multiply(c, rightConstraint));
                break;
            }
        case DIVISION:
            this.incrementalClosure(lVar, this);
            this.incrementalClosure(left, this);
            forgetConstraints(lVar);
            if ((leftConstraint = this.getValue(left, ZERO)).isPresent()) {
                Constraint c = leftConstraint.get();
                this.add(lVar, Constraint.divide(c, rightConstraint));
                break;
            }
        case MODULUS:
        case BAND:
        case BOR:
        case BSHL:
        case BSHR:
        case BUSHR:
        case XOR:
        case INVALID:
        default:
            // update edges containing lVar to ⟙
            this.updateTop(lVar);
        }
        this.incrementalClosure(lVar, this);
    }

    /** Assign lVar constraint with respect to single local
     *
     * Similar to updateState where the local appears on the left hand side,
     * however, we unable to fully encode subtraction cases.  Like before, we
     * project and compute the interval values if we cannot compute the
     * relational domain values.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            IntConstant left,
                            Local right,
                            BinaryOperatorType op) {
        Constraint leftConstraint = this.eval(left);
        Optional<Constraint> rightConstraint;
        switch (op) {
        case ADDITION:
            if (lVar.equals(right)) {
                Constraint c = this.eval(left);
                this.dbs.addOutgoingFrom(lVar, c, inState.dbs);
                this.dbs.subIncomingFrom(lVar, c, inState.dbs);
            } else {
                this.forgetConstraints(lVar);
                this.add(lVar, right, this.eval(left));
            }
            break;
        case SUBTRACTION:
            this.incrementalClosure(lVar, this);
            this.incrementalClosure(right, this);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, Constraint.subtract(leftConstraint, rightConstraint.get()));
                break;
            }
        case MULTIPLICATION:
            this.incrementalClosure(lVar, this);
            this.incrementalClosure(right, this);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, Constraint.multiply(leftConstraint, rightConstraint.get()));
                break;
            }
        case DIVISION:
            this.incrementalClosure(lVar, this);
            this.incrementalClosure(right, this);
            this.forgetConstraints(lVar);
            if ((rightConstraint = this.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, Constraint.divide(leftConstraint, rightConstraint.get()));
                break;
            }
        case MODULUS:
        case BAND:
        case BOR:
        case BSHL:
        case BSHR:
        case BUSHR:
        case XOR:
        case INVALID:
        default:
            // update edges containing lVar to ⟙
            this.updateTop(lVar);
        }
        this.incrementalClosure(lVar, this);
    }

    /** Assign lVar with respect to two locals
     *
     * We do not have decision procedures that can handle the transfer of a
     * local with respect to two locals, we project to the interval domain, if
     * available.
     */
    public void updateState(Local lVar,
                            DifferenceBoundedState inState,
                            Local left,
                            Local right,
                            BinaryOperatorType op) {
        Consumer<BiFunction<Interval32Box, Interval32Box, Interval32Box>> computeInterval = (binop) -> {
            Optional<Constraint> leftLower = inState.getValue(ZERO, left);
            Optional<Constraint> leftUpper = inState.getValue(left, ZERO);
            Optional<Constraint> rightLower = inState.getValue(ZERO, right);
            Optional<Constraint> rightUpper = inState.getValue(right, ZERO);
            Interval32Box interval_c = new Interval32Box(leftLower.map(x -> x.bound()),
                                                     leftUpper.map(x -> x.bound()));
            Interval32Box interval_d = new Interval32Box(rightLower.map(x -> x.bound()),
                                                     rightUpper.map(x -> x.bound()));
            PredicateType predicateType = Stream.of(Constraint.superiorPredicate(leftLower, rightLower),
                                                    Constraint.superiorPredicate(leftLower, rightUpper),
                                                    Constraint.superiorPredicate(leftUpper, rightLower),
                                                    Constraint.superiorPredicate(leftUpper, rightUpper))
            .reduce((a, b) -> PredicateType.superior(a, b)).get();
            Interval32Box result = binop.apply(interval_c, interval_d);
            result.lowerBound().ifPresentOrElse(l -> this.add(ZERO, lVar, new Constraint(l, predicateType)),
                                                () -> this.updateTop(ZERO, lVar));
            result.upperBound().ifPresentOrElse(u -> this.add(lVar, ZERO, new Constraint(u, predicateType)),
                                                () -> this.updateTop(lVar, ZERO));
        };
        Optional<Constraint> c;
        Optional<Constraint> d;
        switch (op) {
        case ADDITION:
            if ((c = inState.getValue(left, right)).isPresent()) {
                this.add(lVar, ZERO, c.get());
            } else if ((c =  inState.getValue(right, left)).isPresent()) {
                this.add(lVar, ZERO, c.get());
            } else {
                computeInterval.accept((a, b) -> Interval32Box.add(a, b));
            }
            break;
        case SUBTRACTION:
            if ((c = inState.getValue(left, right)).isPresent()) {
                this.add(lVar, ZERO, c.get());
            } else if ((c = inState.getValue(right, left)).isPresent()) {
                Constraint x = c.get();
                this.add(lVar, ZERO, new Constraint(x.bound() * -1, x.predicate()));
            } else if ((c = inState.getValue(left, ZERO)).isPresent() &&
                       (d = inState.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, ZERO, Constraint.subtract(c.get(), d.get()));
            } else if ((c = inState.getValue(ZERO, left)).isPresent() &&
                       (d = inState.getValue(ZERO, right)).isPresent()) {
                this.add(ZERO, lVar, Constraint.subtract(Constraint.negate(c.get()),
                                                         Constraint.negate(d.get())).negate());
            } else {
                computeInterval.accept((a, b) -> Interval32Box.subtract(a, b));
            }
            break;
        case MULTIPLICATION:
            if ((c = inState.getValue(left, ZERO)).isPresent() &&
                (d = inState.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, ZERO, Constraint.multiply(c.get(), d.get()));
            } else if ((c = inState.getValue(ZERO, left)).isPresent() &&
                       (d = inState.getValue(ZERO, right)).isPresent()) {
                this.add(ZERO, lVar, Constraint.multiply(Constraint.negate(c.get()),
                                                         Constraint.negate(d.get())).negate());
            } else {
                computeInterval.accept((a, b) -> Interval32Box.multiply(a, b));
            }
            break;
        case DIVISION:
            if ((c = inState.getValue(left, ZERO)).isPresent() &&
                (d = inState.getValue(right, ZERO)).isPresent()) {
                this.add(lVar, ZERO, Constraint.divide(c.get(), d.get()));
            } else if ((c = inState.getValue(ZERO, left)).isPresent() &&
                       (d = inState.getValue(ZERO, right)).isPresent()) {
                this.add(ZERO, lVar, Constraint.divide(Constraint.negate(c.get()),
                                                       Constraint.negate(d.get())).negate());
            } else {
                computeInterval.accept((a, b) -> Interval32Box.divide(a, b));
            }
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
            this.updateTop(lVar);
        }
        this.incrementalClosure(lVar, this);
    }

    /** Remove all relations containing the provided Local
     *
     * First computes the closure of the graph then removes any constraint
     * connecting through <i>local</i>
     *
     * @param local Remove connecting edges passing through this variable
     */
    public void forget(Local local) {
        this.dbs.forget(local);
    }

    /** Remove all relations containing the provided Local
     *
     * @param l Local to remove connecting edges
     */
    public void forgetConstraints(Local l) {
        this.dbs.forgetConstraints(l);
    }

    private static Constraint eval(DifferenceBoundedState inState, Value v) {
        Constraint c;
        if (v instanceof IntConstant) {
            int value = ((IntConstant) v).value;
            c = new Constraint(value, PredicateType.Eq);
        } else if (v instanceof Local) {
            Local l = (Local) v;
            c = inState.eval(l, Variable.ZERO).copy();
        } else {
            c = Constraint.TOP();
        }
        return c;
    }

    /** Incrementally compute transitive closure for provided Local
     *
     * @param l Local to Project
     */
    public boolean incrementalClosure(Local l, DifferenceBoundedState inState) {
        return this.dbs.incrementalClosure(l, inState.dbs);
    }

    @Override
    public String toString() {
        return this.dbs.toString();
    }

    public String toSMT(SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        for (DBSTriple triple : this.dbs.getConstraints()) {
            sb.append(triple.source.toString());
            sb.append("->");
            sb.append(solver.smt2(this.toGrimpExpr(triple)));
            sb.append("\n");
        }
        return sb.toString();
    }

    public Graph<Local, DBSConstraint> toGraph() {
        throw new UnsupportedOperationException("I'm not doing this one yet");
    }

    public String toSMT(Local l, SolverWrapper solver) {
        StringBuilder sb = new StringBuilder();
        List<DBSTriple> triples = this.dbs.getConstraints(l)
            .stream()
            .filter(t -> !t.constraint.isTop())
            .filter(t -> !t.source.equals(t.target)) // remove over specified constraints: e.g., l0 - l0 <= 1
            .collect(Collectors.toList());
        Collections.sort(triples, (a, b) -> (a.target.toString().compareTo(b.target.toString())));
        LOGGER.debug("edges: {}", triples);
        if (triples.size() == 0) {
            sb.append(solver.smt2(this.toGrimpExpr(l, ZERO, Constraint.TOP())));
        } else if (triples.size() == 1) {
            DBSTriple triple = triples.iterator().next();
            sb.append(solver.smt2(this.toGrimpExpr(triple)));
        } else {
            sb.append("(and");
            for (DBSTriple triple : triples) {
                sb.append(" ");
                sb.append(solver.smt2(this.toGrimpExpr(triple)));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    private BinopExpr toGrimpExpr(DBSTriple triple) {
        return this.toGrimpExpr(triple.source, triple.target, triple.constraint);
    }

    private BinopExpr toGrimpExpr(Local s, Local t, Constraint c) {
        LOGGER.debug("translating {} - {} ? {}", s, t, c);
        BinopExpr r = null;
        Function<Local, BinopExpr> top = l -> {
            return Grimp.v().newOrExpr(Grimp.v().newLeExpr(l, IntConstant.v(0)),
                                       Grimp.v().newGtExpr(l, IntConstant.v(0)));
        };
        Function<PredicateType, BiFunction<Value, Value, BinopExpr>> predExpr = p -> {
            switch (p) {
            case Le:
            return Grimp.v()::newLeExpr;
            case Eq:
            return Grimp.v()::newEqExpr;
            case Lt:
            return Grimp.v()::newLtExpr;
            case Ge:
            return Grimp.v()::newGeExpr;
            case Gt:
            return Grimp.v()::newGtExpr;
            default:
            LOGGER.error("I do not know how to translate {} to grimp", p);
            return null;
            }
        };
        if (c.isBottom()) {
            BinopExpr tBottom = Grimp.v().newAndExpr(Grimp.v().newLeExpr(t, IntConstant.v(0)),
                                                     Grimp.v().newGtExpr(t, IntConstant.v(0)));
            BinopExpr sBottom = Grimp.v().newAndExpr(Grimp.v().newLeExpr(s, IntConstant.v(0)),
                                                     Grimp.v().newGtExpr(s, IntConstant.v(0)));
            if (s.equals(ZERO)) {
                r = tBottom;
            } else if (t.equals(ZERO)) {
                r = sBottom;
            } else {
                r = Grimp.v().newAndExpr(sBottom, tBottom);
            }
        } else if (!s.equals(ZERO) && t.equals(ZERO) && c.isTop()) {
            r = top.apply(s);
        } else if (!t.equals(ZERO) && s.equals(ZERO) && c.isTop()) {
            r = top.apply(t);
        } else if (t.equals(ZERO)) {
            IntConstant k = IntConstant.v(c.bound());
            r = predExpr.apply(c.predicate()).apply(s, k);
        } else if (s.equals(ZERO)) {
            Constraint negated = c.copy().negate();
            IntConstant k = IntConstant.v(negated.bound());
            r = predExpr.apply(negated.predicate()).apply(t, k);
        } else {
            IntConstant k = IntConstant.v(c.bound());
            r = predExpr.apply(c.predicate()).apply(s, Grimp.v().newAddExpr(k, t));
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof DifferenceBoundedState) {
            DifferenceBoundedState state = (DifferenceBoundedState)o;
            equal = (this.dbs.equals(state.dbs));
        }
        return equal;
    }

    public void updateTop(Local l) {
        this.dbs.updateTop(l);
    }

    public void updateTop(Local l, Local r) {
        this.dbs.updateTop(l, r);
    }

    /** Make all edges infeasible
     *
     * Deletes all existing edges and replaces them with bottom constraints
     */
    public void makeInfeasible() {
        this.dbs.makeInfeasible();
    }

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof DifferenceBoundedState) {
            return updateCond((DifferenceBoundedState) inState, left, right, type);
        } else {
            throw new RuntimeException("invalid state for update condition");
        }
    }

    public boolean updateCond(DifferenceBoundedState inState,
                           Value left,
                           Value right,
                           PredicateType type) {
        boolean feasible;
        LOGGER.trace("transfer condition: {} : {} {} {}", inState, left, type, right);
        if (left instanceof Local && right instanceof Local) {
            feasible = this.updateCond(inState, (Local) left, (Local) right, type);
        } else if (left instanceof Local && right instanceof IntConstant) {
            feasible = this.updateCond(inState, (Local) left, (IntConstant) right, type);
        } else if (left instanceof IntConstant && right instanceof Local) {
            feasible = this.updateCond(inState, (IntConstant) left, (Local) right, type);
        } else {
            LOGGER.warn("missing handler for x-condition: {} {} {}", left, type, right);
            feasible = true;
        }
        return feasible;
    }

    public boolean updateCond(DifferenceBoundedState inState,
                           Local left,
                           IntConstant right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.addWithNarrowing(left, ZERO, new Constraint(right.value, PredicateType.Le));
            break;
        case Lt:
            this.addWithNarrowing(left, ZERO, new Constraint(right.value - 1, PredicateType.Le));
            break;
        case Eq:
            this.addWithNarrowing(left, ZERO, new Constraint(right.value));
            break;
        case Ge:
            this.addWithNarrowing(ZERO, left, new Constraint(right.value * -1, PredicateType.Le));
            break;
        case Gt:
            this.addWithNarrowing(ZERO, left, new Constraint((right.value * - 1) - 1, PredicateType.Le));
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.incrementalClosure(left, this) && this.incrementalClosure(ZERO, this);
    }

    public boolean updateCond(DifferenceBoundedState inState,
                           IntConstant left,
                           Local right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.addWithNarrowing(ZERO, right, new Constraint(left.value * - 1, PredicateType.Le));
            break;
        case Lt:
            this.addWithNarrowing(ZERO, right, new Constraint((left.value * - 1) - 1, PredicateType.Le));
            break;
        case Eq:
            this.addWithNarrowing(right, ZERO, new Constraint(left.value));
            break;
        case Ge:
            this.addWithNarrowing(right, ZERO, new Constraint(left.value, PredicateType.Le));
            break;
        case Gt:
            this.addWithNarrowing(right, ZERO, new Constraint(left.value + 1, PredicateType.Le));
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.incrementalClosure(right, this) && this.incrementalClosure(ZERO, this);
    }

    public boolean updateCond(DifferenceBoundedState inState,
                           Local left,
                           Local right,
                           PredicateType type) {
        switch (type) {
        case Le:
            this.addWithNarrowing(left, right, new Constraint(0, PredicateType.Le));
            break;
        case Lt:
            this.addWithNarrowing(left, right, new Constraint(-1, PredicateType.Le));
            break;
        case Eq:
            this.addWithNarrowing(left, right, new Constraint(0));
            break;
        case Ge:
            this.addWithNarrowing(right, left, new Constraint(0, PredicateType.Le));
            break;
        case Gt:
            this.addWithNarrowing(right, left, new Constraint(-1, PredicateType.Le));
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.incrementalClosure(left, this) && this.incrementalClosure(right, this);
    }
}
