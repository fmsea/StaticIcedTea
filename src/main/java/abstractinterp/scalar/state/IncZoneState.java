package abstractinterp.scalar.state;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

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

public class IncZoneState implements State {

    protected DifferenceBoundedMatrix matrix;
    private static Logger LOGGER = LoggerFactory.getLogger(IncZoneState.class);
    public static final Local ZERO = Variable.ZERO;

    /** Create new instance of DBS with locals, initialized to top or bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public IncZoneState(Set<Local> locals, boolean top) {
        Set<Local> localsWithZero = new HashSet<>();
        localsWithZero.addAll(locals);
        localsWithZero.add(ZERO);
        this.matrix = new DifferenceBoundedMatrix(localsWithZero, top);
    }

    /** Copy Constructor: Creates new instance seeded with <i>state</i> values.
     *
     * @param state Incoming state to copy
     */
    public IncZoneState(IncZoneState state) {
        this(state.getLocals(), false);
        state.matrix.copyTo(this.matrix);
    }

    /** test constructor which allows injecting an initialized matrix
     *
     * Should not be used for regular use.
     */
    public IncZoneState(DifferenceBoundedMatrix matrix) {
        this.matrix = matrix;
    }

    /** Return the locals of this state
     *
     * @return set of Jimple Locals
     */
    public Set<Local> getLocals() {
        return this.matrix.getLocals();
    }

    /** Return new DBS instance with this instance data
     *
     * These copies are deep.
     */
    public State copy() {
        return new IncZoneState(this);
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     * @exception RuntimeException if given an incompatible State type.
     */
    public void copyTo(State destination) {
        if (destination instanceof IncZoneState) {
            this.copyTo((IncZoneState)destination);
        } else {
            throw new RuntimeException("Invalid type for copyTo");
        }
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     */
    public void copyTo(IncZoneState destination) {
        this.matrix.copyTo(destination.matrix);
    }

    public void add(Local source,
                    Local target,
                    ZoneConstraint constraint) {
        this.add(source, target, constraint, this);
    }

    /** Introduce new constraint between source and target
     *
     * Encode a new constraint in <pre>source - target <= constraint</pre>
     * format if the new constraint is more precise than the existing
     * constraint.
     *
     * @param source
     * @param target
     * @param constraint
     * @param inState
     */
    public void add(Local source,
                    Local target,
                    ZoneConstraint constraint,
                    IncZoneState inState) {
        LOGGER.trace("Introducing new Constraint: {} - {} ≤ {}", source, target, constraint);
        this.matrix.putIncremental(source, target, constraint, inState.matrix);
    }

    protected void addWithoutClosure(Local source,
                                     Local target,
                                     ZoneConstraint constraint,
                                     IncZoneState inState) {
        LOGGER.trace("Introducing new Constraint: {} - {} ≤ {}", source, target, constraint);
        this.matrix.putConstraint(source, target, constraint, inState.matrix);
    }

    public void assign(Local source,
                       Local target,
                       Interval32Box box) {
        this.assign(source, target, box, this);
    }

    public void assign(Local source,
                       Local target,
                       Interval32Box box,
                       IncZoneState inState) {
        this.add(source,
                 target,
                 ZoneConstraint.of(box.upperBound()),
                 inState);
        this.add(target,
                 source,
                 ZoneConstraint.of(box.lowerBound().map(b -> b * -1)),
                 inState);
    }

    public boolean isFeasible() {
        return this.matrix.isFeasible();
    }

    public void mergeWith(State inState) {
        if (inState instanceof IncZoneState) {
            this.mergeWith((IncZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for mergeWith");
        }
    }

    public void mergeWith(IncZoneState inState) {
        this.matrix.union(inState.matrix);
    }

    public void widenWith(State inState) {
        if (inState instanceof IncZoneState) {
            this.widenWith((IncZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for widenWith");
        }
    }

    public void widenWith(IncZoneState inState) {
        this.matrix.widenWith(inState.matrix);
    }

    public boolean isSubset(State inState) {
        if (inState instanceof IncZoneState) {
            return this.isSubset((IncZoneState) inState);
        } else {
            throw new RuntimeException("invalid type for isSubset");
        }
    }

    public boolean isSubset(IncZoneState inState) {
        return this.matrix.isSubset(inState.matrix);
    }

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperatorType operator) {
        if (inState instanceof IncZoneState) {
            this.updateState(lVar,
                             (IncZoneState) inState,
                             left,
                             right,
                             operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar,
                            IncZoneState inState,
                            Value left,
                            Value right,
                            BinaryOperatorType operator) {
        if (left instanceof IntConstant && right instanceof IntConstant) {
            this.updateState(lVar, inState, (IntConstant) left, (IntConstant) right, operator);
        } else if (left instanceof Local && right instanceof IntConstant) {
            this.updateState(lVar, inState, (Local) left, (IntConstant) right, operator);
        } else if (left instanceof IntConstant && right instanceof Local) {
            this.updateState(lVar, inState, (IntConstant) left, (Local) right, operator);
        } else if (left instanceof Local && right instanceof Local) {
            this.updateState(lVar, inState, (Local) left, (Local) right, operator);
        } else {
            LOGGER.warn("missing handler for expression: {} {} {}", left, operator, right);
        }
    }

    public void updateState(Local lVar,
                            IncZoneState inState,
                            IntConstant left,
                            IntConstant right,
                            BinaryOperatorType operator) {
        IntConstant c;
        switch (operator) {
        case ADDITION:
            c = IntConstant.v(left.value + right.value);
            break;
        case SUBTRACTION:
            c = IntConstant.v(left.value - right.value);
            break;
        case MULTIPLICATION:
            c = IntConstant.v(left.value * right.value);
            break;
        case DIVISION:
            c = IntConstant.v(left.value / right.value);
            break;
        case MODULUS:
            c = IntConstant.v(left.value % right.value);
            break;
        default:
            LOGGER.info("Encountered unhandled integer binary operator: {}", operator);
            return;
        }

        this.updateState(lVar, inState, c);
    }

    public void updateState(Local lVar,
                            IncZoneState inState,
                            Local left,
                            IntConstant right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box leftInterval = inState.matrix.projectToInterval(left);
            Interval32Box newValue = binop.apply(leftInterval, Interval32Box.of(right.value));
            this.assign(lVar, ZERO, newValue);
        };
        switch (operator) {
        case ADDITION:
            if (lVar.equals(left)) {
                ZoneConstraint c = ZoneConstraint.of(right.value);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, left, ZoneConstraint.of(right.value));
                this.add(left, lVar, ZoneConstraint.of(right.value * -1));
            }
            break;
        case SUBTRACTION:
            if (lVar.equals(left)) {
                ZoneConstraint c = ZoneConstraint.of(right.value * -1);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, left, ZoneConstraint.of(right.value * -1));
                this.add(left, lVar, ZoneConstraint.of(right.value));
            }
            break;
        case MULTIPLICATION:
            this.forget(lVar);
            computeInterval.accept(Interval32Box::multiply);
            break;
        case DIVISION:
            this.forget(lVar);
            computeInterval.accept(Interval32Box::divide);
            break;
        default:
            LOGGER.warn("unhandled binary operator, transfering ⟙ [{} = {} {} {}]",
                        lVar, left, right, operator);
            this.forget(lVar);
        }
    }

    public void updateState(Local lVar,
                            IncZoneState inState,
                            IntConstant left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box rightInterval = inState.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(Interval32Box.of(left.value), rightInterval);
            this.assign(lVar, ZERO, newValue);
        };
        switch (operator) {
        case ADDITION:
            if (lVar.equals(right)) {
                ZoneConstraint c = ZoneConstraint.of(left.value);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, right, ZoneConstraint.of(left.value));
                this.add(right, lVar, ZoneConstraint.of(left.value * -1));
            }
            break;
        case SUBTRACTION:
            this.forget(lVar);
            computeInterval.accept(Interval32Box::subtract);
            break;
        case MULTIPLICATION:
            this.forget(lVar);
            computeInterval.accept(Interval32Box::multiply);
            break;
        case DIVISION:
            this.forget(lVar);
            computeInterval.accept(Interval32Box::divide);
            break;
        default:
            LOGGER.warn("unhandled binary operator, transfering ⟙ [{} = {} {} {}]",
                        lVar, left, right, operator);
            this.forget(lVar);
        }
    }

    public void updateState(Local lVar,
                            IncZoneState inState,
                            Local left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box leftInterval = inState.matrix.projectToInterval(left);
            Interval32Box rightInterval = inState.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(leftInterval, rightInterval);
            this.assign(lVar, ZERO, newValue);
        };
        this.forget(lVar);
        switch (operator) {
        case ADDITION:
            computeInterval.accept(Interval32Box::add);
            break;
        case SUBTRACTION:
            computeInterval.accept(Interval32Box::subtract);
            break;
        case MULTIPLICATION:
            computeInterval.accept(Interval32Box::multiply);
            break;
        case DIVISION:
            computeInterval.accept(Interval32Box::divide);
            break;
        default:
        }
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof IncZoneState) {
            this.updateState(lVar, (IncZoneState) inState, v);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar, IncZoneState inState, Value v) {
        this.forget(lVar);
        if (v instanceof JNegExpr) {
            v = ((JNegExpr)v).getOp();
            if (v instanceof IntConstant) {
                IntConstant ic = (IntConstant)v;
                this.updateState(lVar, inState, IntConstant.v(ic.value * -1));
            } else if (v instanceof Local) {
                Local l = (Local)v;
                Interval32Box interval = inState.matrix.projectToInterval(l);
                interval.negate();
                this.assign(lVar, ZERO, interval);
            }
        } else if (v instanceof IntConstant) {
            this.updateState(lVar, inState, (IntConstant) v);
        } else if (v instanceof Local) {
            this.updateState(lVar, inState, (Local) v);
        } else {
            LOGGER.warn("missing handler for {} = {}", lVar, v);
        }
    }

    public void updateState(Local lVar, IncZoneState inState, IntConstant c) {
        this.forget(lVar);
        this.assign(lVar, ZERO, Interval32Box.of(c.value));
    }

    public void updateState(Local lVar, IncZoneState inState, Local l) {
        this.forget(lVar);
        this.add(lVar, l, ZoneConstraint.of(0));
        this.add(l, lVar, ZoneConstraint.of(0));
    }

    @Override
    public String toString() {
        return this.matrix.toString();
    }

    public Optional<BinopExpr> toBinop() {
        return this.matrix.toBinop();
    }

    public String toSMT(SolverWrapper solver) {
        return this.matrix.toSMT(solver);
    }

    public String toSMT(Local l, SolverWrapper solver) {
        return this.matrix.toSMT(l, solver);
    }

    public Graph<Local, ZoneConstraint> toGraph() {
        return this.matrix.toGraph();
    }

    public void updateTop(Local local) {
        this.forget(local);
    }

    protected boolean isConstant(Local lVar) {
        return this.isConstant(lVar, this);
    }

    protected boolean isConstant(Local lVar, IncZoneState inState) {
        ZoneConstraint upper = this.matrix.getConstraint(lVar, ZERO);
        ZoneConstraint lower = this.matrix.getConstraint(ZERO, lVar);
        Interval32Box interval = Interval32Box.of(lower.bound().map(b -> b * -1),
                                                  upper.bound());
        // Testing whether the value is a "singleton" in the interval sense is
        // equivalent to testing if the value is a constant.  Specifically,
        // isSingleton tests whether the interval is bounded and the bounds are
        // equal, exactly the same answer we want from the DBM perspective.
        return interval.isSingleton();
    }

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof IncZoneState) {
            return this.updateCond((IncZoneState) inState,
                                   left,
                                   right,
                                   type);
        } else {
            throw new RuntimeException("invalid type for update condition");
        }
    }

    public boolean updateCond(IncZoneState inState,
                              Value left,
                              Value right,
                              PredicateType type) {
        boolean feasible = false;
        if (left instanceof Local && right instanceof Local) {
            feasible = updateCond(inState, (Local) left, (Local) right, type);
        } else if (left instanceof Local && right instanceof IntConstant) {
            feasible = updateCond(inState, (Local) left, (IntConstant) right, type);
        } else if (left instanceof IntConstant && right instanceof Local) {
            feasible = updateCond(inState, (IntConstant) left, (Local) right, type);
        } else {
            LOGGER.warn("missing handler for x-condition: {} {} {}", left, type, right);
            feasible = true;
        }
        return feasible;
    }

    public boolean updateCond(IncZoneState inState,
                              Local left,
                              IntConstant right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(left, ZERO, ZoneConstraint.of(right.value), inState);
            break;
        case Lt:
            this.add(left, ZERO, ZoneConstraint.of(right.value - 1), inState);
            break;
        case Eq:
            this.add(left, ZERO, ZoneConstraint.of(right.value), inState);
            this.add(ZERO, left, ZoneConstraint.of(right.value * -1), inState);
            break;
        case Ge:
            this.add(ZERO, left, ZoneConstraint.of(right.value * -1), inState);
            break;
        case Gt:
            this.add(ZERO, left, ZoneConstraint.of((right.value * - 1) - 1), inState);
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.isFeasible();
    }

    public boolean updateCond(IncZoneState inState,
                              IntConstant left,
                              Local right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(ZERO, right, ZoneConstraint.of(left.value * -1), inState);
            break;
        case Lt:
            this.add(ZERO, right, ZoneConstraint.of((left.value * -1) - 1), inState);
            break;
        case Eq:
            this.add(ZERO, right, ZoneConstraint.of(left.value * -1), inState);
            this.add(right, ZERO, ZoneConstraint.of(left.value), inState);
            break;
        case Ge:
            this.add(right, ZERO, ZoneConstraint.of(left.value), inState);
            break;
        case Gt:
            this.add(right, ZERO, ZoneConstraint.of(left.value - 1), inState);
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.isFeasible();
    }

    public boolean updateCond(IncZoneState inState,
                              Local left,
                              Local right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(left, right, ZoneConstraint.of(0), inState);
            break;
        case Lt:
            this.add(left, right, ZoneConstraint.of(-1), inState);
            break;
        case Eq:
            this.add(left, right, ZoneConstraint.of(0), inState);
            this.add(right, left, ZoneConstraint.of(0), inState);
            break;
        case Ge:
            this.add(right, left, ZoneConstraint.of(0), inState);
            break;
        case Gt:
            this.add(right, left, ZoneConstraint.of(-1), inState);
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.isFeasible();
    }

    public void forget(Local local) {
        this.matrix.forgetConstraintsSimple(local);
    }

    public void makeInfeasible() {
        this.matrix.makeInfeasible();
    }

    public Set<Local> getConnectedVariablesOf(Local source) {
        return this.matrix.getConnectedVariablesOf(source);
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof IncZoneState) {
            equal = this.equals((IncZoneState) o);
        }
        return equal;
    }

    public boolean equals(IncZoneState other) {
        return other != null && this.matrix.equals(other.matrix);
    }
}
