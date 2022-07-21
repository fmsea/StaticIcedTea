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

public class MinZoneState implements State {

    private DifferenceBoundedMatrix matrix;
    private static Logger LOGGER = LoggerFactory.getLogger(MinZoneState.class);
    public static final Local ZERO = Variable.ZERO;

    /** Create new instance of DBS with locals, initialized to top or bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public MinZoneState(Set<Local> locals, boolean top) {
        Set<Local> localsWithZero = new HashSet<>();
        localsWithZero.addAll(locals);
        localsWithZero.add(ZERO);
        this.matrix = new DifferenceBoundedMatrix(localsWithZero, top);
    }

    /** Copy Constructor: Creates new instance seeded with <i>state</i> values.
     *
     * @param state Incoming state to copy
     */
    public MinZoneState(MinZoneState state) {
        this(state.getLocals(), false);
        state.matrix.copyTo(this.matrix);
    }

    /** test constructor which allows injecting an initialized matrix
     *
     * Should not be used for regular use.
     */
    public MinZoneState(DifferenceBoundedMatrix matrix) {
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
        return new MinZoneState(this);
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     * @exception RuntimeException if given an incompatible State type.
     */
    public void copyTo(State destination) {
        if (destination instanceof MinZoneState) {
            this.copyTo((MinZoneState)destination);
        } else {
            throw new RuntimeException("Invalid type for copyTo");
        }
    }

    /** (Deeply) copy this to <i>destination</i>
     *
     * @param destination
     */
    public void copyTo(MinZoneState destination) {
        this.matrix.copyTo(destination.matrix);
    }

    public boolean add(Local source,
                       Local target,
                       ZoneConstraint constraint) {
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
                       ZoneConstraint constraint,
                       MinZoneState inState) {
        boolean added = false;
        LOGGER.trace("Introducing new Constraint: {} - {} ≤ {}", source, target, constraint);
        ZoneConstraint existing = inState.matrix.getConstraint(source, target);
        LOGGER.trace("Compare to existing constraint: {} ≤ {}", constraint, existing);
        if (ZoneConstraint.compare(constraint, existing) < 0) {
            this.matrix.setConstraint(source, target, constraint);
            added = true;
        }
        return added;
    }

    public boolean isFeasible() {
        return this.matrix.isFeasible();
    }

    public void mergeWith(State inState) {
        if (inState instanceof MinZoneState) {
            this.mergeWith((MinZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for mergeWith");
        }
    }

    public void mergeWith(MinZoneState inState) {
        this.matrix.computeClosure();
        inState.matrix.computeClosure();
        this.matrix.union(inState.matrix);
    }

    public void widenWith(State inState) {
        if (inState instanceof MinZoneState) {
            this.widenWith((MinZoneState) inState);
        } else {
            throw new RuntimeException("Invalid type for widenWith");
        }
    }

    public void widenWith(MinZoneState inState) {
        this.matrix.computeClosure();
        inState.matrix.computeClosure();
        this.matrix.widenWith(inState.matrix);
    }

    public boolean isSubset(State inState) {
        if (inState instanceof MinZoneState) {
            return this.isSubset((MinZoneState) inState);
        } else {
            throw new RuntimeException("invalid type for isSubset");
        }
    }

    public boolean isSubset(MinZoneState inState) {
        this.matrix.computeClosure();
        inState.matrix.computeClosure();
        return this.matrix.isSubset(inState.matrix);
    }

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperatorType operator) {
        if (inState instanceof MinZoneState) {
            this.updateState(lVar,
                             (MinZoneState) inState,
                             left,
                             right,
                             operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar,
                            MinZoneState inState,
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
                            MinZoneState inState,
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
                            MinZoneState inState,
                            Local left,
                            IntConstant right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            this.matrix.computeClosure();
            Interval32Box leftInterval = this.matrix.projectToInterval(left);
            Interval32Box newValue = binop.apply(leftInterval, Interval32Box.of(right.value));
            this.add(lVar, ZERO, ZoneConstraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, ZoneConstraint.of(newValue.lowerBound().map(b -> b * -1)));
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
                            MinZoneState inState,
                            IntConstant left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            this.matrix.computeClosure();
            Interval32Box rightInterval = this.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(Interval32Box.of(left.value), rightInterval);
            this.forget(lVar);
            this.add(lVar, ZERO, ZoneConstraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, ZoneConstraint.of(newValue.lowerBound().map(b -> b * -1)));
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
    }

    public void updateState(Local lVar,
                            MinZoneState inState,
                            Local left,
                            Local right,
                            BinaryOperatorType operator) {
        Consumer<BinaryOperator<Interval32Box>> computeInterval = (binop) -> {
            this.matrix.computeClosure();
            Interval32Box leftInterval = this.matrix.projectToInterval(left);
            Interval32Box rightInterval = this.matrix.projectToInterval(right);
            Interval32Box newValue = binop.apply(leftInterval, rightInterval);
            this.forget(lVar);
            this.add(lVar, ZERO, ZoneConstraint.of(newValue.upperBound()));
            this.add(ZERO, lVar, ZoneConstraint.of(newValue.lowerBound().map(b -> b * -1)));
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
    }

    public void updateState(Local lVar, State inState, Value v) {
        if (inState instanceof MinZoneState) {
            this.updateState(lVar, (MinZoneState) inState, v);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar, MinZoneState inState, Value v) {
        if (v instanceof JNegExpr) {
            v = ((JNegExpr)v).getOp();
            if (v instanceof IntConstant) {
                IntConstant ic = (IntConstant)v;
                this.forget(lVar);
                this.updateState(lVar, inState, IntConstant.v(ic.value * -1));
            } else if (v instanceof Local) {
                Local l = (Local)v;
                this.matrix.computeClosure();
                Interval32Box interval = this.matrix.projectToInterval(l);
                interval.negate();
                this.forget(lVar);
                this.add(lVar, ZERO, ZoneConstraint.of(interval.upperBound()));
                this.add(ZERO, lVar, ZoneConstraint.of(interval.lowerBound().map(b -> b * -1)));
            }
        } else if (v instanceof IntConstant) {
            this.updateState(lVar, inState, (IntConstant) v);
        } else if (v instanceof Local) {
            this.updateState(lVar, inState, (Local) v);
        } else {
            LOGGER.warn("missing handler for {} = {}", lVar, v);
        }
    }

    public void updateState(Local lVar, MinZoneState inState, IntConstant c) {
        this.forget(lVar);
        this.add(lVar, ZERO, ZoneConstraint.of(c.value));
        this.add(ZERO, lVar, ZoneConstraint.of(c.value * -1));
    }

    public void updateState(Local lVar, MinZoneState inState, Local l) {
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

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof MinZoneState) {
            return this.updateCond((MinZoneState) inState,
                                   left,
                                   right,
                                   type);
        } else {
            throw new RuntimeException("invalid type for update condition");
        }
    }

    public boolean updateCond(MinZoneState inState,
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

    public boolean updateCond(MinZoneState inState,
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
            this.add(ZERO, left, ZoneConstraint.of(right.value * - 1), inState);
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
        return this.matrix.computeClosure();
    }

    public boolean updateCond(MinZoneState inState,
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
        return this.matrix.computeClosure();
    }

    public boolean updateCond(MinZoneState inState,
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

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof MinZoneState) {
            equal = this.equals((MinZoneState) o);
        }
        return equal;
    }

    public boolean equals(MinZoneState other) {
        this.matrix.computeClosure();
        other.matrix.computeClosure();
        return other != null && this.matrix.equals(other.matrix);
    }
}
