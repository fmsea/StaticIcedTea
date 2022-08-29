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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;

import abstractinterp.scalar.state.util.GraphProjection;
import solver.SolverWrapper;

public class MaxZoneState implements State {

    private DifferenceBoundedMatrix matrix;
    private static Logger LOGGER = LoggerFactory.getLogger(MaxZoneState.class);
    public static final Local ZERO = Variable.ZERO;

    /** Create new instance of DBS with locals, initialized to top or bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public MaxZoneState(Set<Local> locals, boolean top) {
        Set<Local> localsWithZero = new HashSet<>();
        localsWithZero.addAll(locals);
        localsWithZero.add(ZERO);
        this.matrix = new DifferenceBoundedMatrix(localsWithZero, top);
    }

    /** Copy Constructor: Creates new instance seeded with <i>state</i> values.
     *
     * @param state Incoming state to copy
     */
    public MaxZoneState(MaxZoneState state) {
        this(state.getLocals(), false);
        state.matrix.copyTo(this.matrix);
    }

    /** test constructor which allows injecting an initialized matrix
     *
     * Should not be used for regular use.
     */
    public MaxZoneState(DifferenceBoundedMatrix matrix) {
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
        return new MaxZoneState(this);
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     * @exception RuntimeException if given an incompatible State type.
     */
    public void copyTo(State destination) {
        if (destination instanceof MaxZoneState) {
            this.copyTo((MaxZoneState)destination);
        } else {
            throw new RuntimeException("Invalid type for copyTo");
        }
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     */
    public void copyTo(MaxZoneState destination) {
        this.matrix.copyTo(destination.matrix);
    }

    public boolean add(Local source,
                       Local target,
                       Constraint constraint) {
        return this.add(source, target, constraint, this);
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
    public boolean add(Local source,
                       Local target,
                       Constraint constraint,
                       MaxZoneState inState) {
        boolean added = false;
        LOGGER.trace("Introducing new Constraint: {} - {} ≤ {}", source, target, constraint);
        Constraint existing = inState.matrix.getConstraint(source, target);
        LOGGER.trace("Compare to existing constraint: {} ≤ {}", constraint, existing);
        if (Constraint.compare(constraint, existing) < 0) {
            this.matrix.setConstraint(source, target, constraint);
            added = true;
        }
        return added;
    }

    public boolean isFeasible() {
        return this.matrix.isFeasible();
    }

    public void mergeWith(State inState) {
        if (inState instanceof MaxZoneState) {
            this.mergeWith((MaxZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for mergeWith");
        }
    }

    public void mergeWith(MaxZoneState inState) {
        this.matrix.union(inState.matrix);
    }

    public void widenWith(State inState) {
        if (inState instanceof MaxZoneState) {
            this.widenWith((MaxZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for widenWith");
        }
    }

    public void widenWith(MaxZoneState inState) {
        this.matrix.widenWith(inState.matrix);
    }

    public boolean isSubset(State inState) {
        if (inState instanceof MaxZoneState) {
            return this.isSubset((MaxZoneState) inState);
        } else {
            throw new RuntimeException("invalid type for isSubset");
        }
    }

    public boolean isSubset(MaxZoneState inState) {
        return this.matrix.isSubset(inState.matrix);
    }

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperatorType operator) {
        if (inState instanceof MaxZoneState) {
            this.updateState(lVar,
                             (MaxZoneState) inState,
                             left,
                             right,
                             operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar,
                            MaxZoneState inState,
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
            LOGGER.warn("missing handler for assignment transfer [{} = {} {} {}]",
                        lVar, left, operator, right);
            this.forget(lVar);
        }
    }

    public void updateState(Local lVar,
                            MaxZoneState inState,
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
            LOGGER.warn("Encountered unhandled integer binary operator: {}", operator);
            return;
        }

        this.updateState(lVar, inState, c);
    }

    public void updateState(Local lVar,
                            MaxZoneState inState,
                            Local left,
                            IntConstant right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box leftInterval = this.matrix.projectToInterval(left);
            Interval32Box newValue = binop.apply(leftInterval, Interval32Box.of(right.value));
            this.add(lVar, ZERO, Constraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, Constraint.of(newValue.lowerBound().map(b -> b * -1)));
        };
        switch (operator) {
        case ADDITION:
            if (lVar.equals(left)) {
                Constraint c = Constraint.of(right.value);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, left, Constraint.of(right.value));
                this.add(left, lVar, Constraint.of(right.value * -1));
            }
            break;
        case SUBTRACTION:
            if (lVar.equals(left)) {
                Constraint c = Constraint.of(right.value * -1);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, left, Constraint.of(right.value * -1));
                this.add(left, lVar, Constraint.of(right.value));
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
        this.matrix.computeClosure();
    }

    public void updateState(Local lVar,
                            MaxZoneState inState,
                            IntConstant left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box rightInterval = this.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(Interval32Box.of(left.value), rightInterval);
            this.forget(lVar);
            this.add(lVar, ZERO, Constraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, Constraint.of(newValue.lowerBound().map(b -> b * -1)));
        };
        switch (operator) {
        case ADDITION:
            if (lVar.equals(right)) {
                Constraint c = Constraint.of(left.value);
                this.matrix.addOutgoing(lVar, c, inState.matrix);
                this.matrix.subIncoming(lVar, c, inState.matrix);
            } else {
                this.forget(lVar);
                this.add(lVar, right, Constraint.of(left.value));
                this.add(right, lVar, Constraint.of(left.value * -1));
            }
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
            LOGGER.warn("unhandled binary operator, transfering ⟙ [{} = {} {} {}]",
                        lVar, left, right, operator);
            this.forget(lVar);
        }
        this.matrix.computeClosure();
    }

    public void updateState(Local lVar,
                            MaxZoneState inState,
                            Local left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            Interval32Box leftInterval = this.matrix.projectToInterval(left);
            Interval32Box rightInterval = this.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(leftInterval, rightInterval);
            this.forget(lVar);
            this.add(lVar, ZERO, Constraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, Constraint.of(newValue.lowerBound().map(b -> b * -1)));
        };
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
            this.forget(lVar);
        }
        this.matrix.computeClosure();
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof MaxZoneState) {
            this.updateState(lVar, (MaxZoneState) inState, v);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar, MaxZoneState inState, Value v) {
        if (v instanceof JNegExpr) {
            v = ((JNegExpr)v).getOp();
            if (v instanceof IntConstant) {
                IntConstant ic = (IntConstant)v;
                this.forget(lVar);
                this.updateState(lVar, inState, IntConstant.v(ic.value * -1));
            } else if (v instanceof Local) {
                Local l = (Local)v;
                Interval32Box interval = this.matrix.projectToInterval(l);
                interval.negate();
                this.forget(lVar);
                this.add(lVar, ZERO, Constraint.of(interval.upperBound()));
                this.add(ZERO, lVar, Constraint.of(interval.lowerBound().map(b -> b * -1)));
            }
        } else if (v instanceof IntConstant) {
            this.updateState(lVar, inState, (IntConstant) v);
        } else if (v instanceof Local) {
            this.updateState(lVar, inState, (Local) v);
        } else {
            LOGGER.warn("missing handler for {} = {}", lVar, v);
        }
        this.matrix.computeClosure();
    }

    public void updateState(Local lVar, MaxZoneState inState, IntConstant c) {
        this.forget(lVar);
        this.add(lVar, ZERO, Constraint.of(c.value));
        this.add(ZERO, lVar, Constraint.of(c.value * -1));
    }

    public void updateState(Local lVar, MaxZoneState inState, Local l) {
        this.forget(lVar);
        this.add(lVar, l, Constraint.of(0));
        this.add(l, lVar, Constraint.of(0));
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

    public String toSMT(Set<Local> locals, SolverWrapper solver) {
        return this.matrix.toSMT(locals, solver);
    }

    public String toReachableSMT(Local source, SolverWrapper solver) {
        return this.matrix.toReachableSMT(source, solver);
    }

    public String toReachableSMT(Set<Local> sources, SolverWrapper solver) {
        return this.matrix.toReachableSMT(sources, solver);
    }

    public GraphProjection toGraph() {
        return this.matrix.toGraph();
    }

    public void updateTop(Local local) {
        this.forget(local);
    }

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof MaxZoneState) {
            return this.updateCond((MaxZoneState) inState,
                                   left,
                                   right,
                                   type);
        } else {
            throw new RuntimeException("invalid type for update condition");
        }
    }

    public boolean updateCond(MaxZoneState inState,
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

    public boolean updateCond(MaxZoneState inState,
                              Local left,
                              IntConstant right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(left, ZERO, Constraint.of(right.value), inState);
            break;
        case Lt:
            this.add(left, ZERO, Constraint.of(right.value - 1), inState);
            break;
        case Eq:
            this.add(left, ZERO, Constraint.of(right.value), inState);
            this.add(ZERO, left, Constraint.of(right.value * - 1), inState);
            break;
        case Ge:
            this.add(ZERO, left, Constraint.of(right.value * -1), inState);
            break;
        case Gt:
            this.add(ZERO, left, Constraint.of((right.value * - 1) - 1), inState);
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.computeClosure();
    }

    public boolean updateCond(MaxZoneState inState,
                              IntConstant left,
                              Local right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(ZERO, right, Constraint.of(left.value * -1), inState);
            break;
        case Lt:
            this.add(ZERO, right, Constraint.of((left.value * -1) - 1), inState);
            break;
        case Eq:
            this.add(ZERO, right, Constraint.of(left.value * -1), inState);
            this.add(right, ZERO, Constraint.of(left.value), inState);
            break;
        case Ge:
            this.add(right, ZERO, Constraint.of(left.value), inState);
            break;
        case Gt:
            this.add(right, ZERO, Constraint.of(left.value - 1), inState);
            break;
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.computeClosure();
    }

    public boolean updateCond(MaxZoneState inState,
                              Local left,
                              Local right,
                              PredicateType type) {
        switch (type) {
        case Le:
            this.add(left, right, Constraint.of(0), inState);
            break;
        case Lt:
            this.add(left, right, Constraint.of(-1), inState);
            break;
        case Eq:
            this.add(left, right, Constraint.of(0), inState);
            this.add(right, left, Constraint.of(0), inState);
            break;
        case Ge:
            this.add(right, left, Constraint.of(0), inState);
            break;
        case Gt:
            this.add(right, left, Constraint.of(-1), inState);
        case Ne:
            break;
        case Invalid:
            this.makeInfeasible();
            return false;
        }
        return this.matrix.computeClosure();
    }

    public void forget(Local local) {
        this.matrix.forgetConstraints(local);
    }

    public void makeInfeasible() {
        this.matrix.makeInfeasible();
    }

    public Set<Local> getConnectedVariablesOf(Local source) {
        return this.matrix.getConnectedVariablesOf(source);
    }

    public Set<Local> getReachableVariablesOf(Local source) {
        return this.matrix.getReachableVariablesOf(source);
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof MaxZoneState) {
            equal = this.equals((MaxZoneState) o);
        }
        return equal;
    }

    public boolean equals(MaxZoneState other) {
        return other != null && this.matrix.equals(other.matrix);
    }
}
