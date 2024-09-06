package dev.fmsea.absint.scalar.state;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.update.DefaultOctagonRefiner;
import dev.fmsea.absint.scalar.state.update.DefaultOctagonUpdater;
import dev.fmsea.absint.scalar.state.update.OctagonUpdater;
import dev.fmsea.absint.scalar.state.util.GraphProjection;
import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.visitors.OctagonChangedVariableVisitor;
import dev.fmsea.tadr.visitors.VariableVisitor;
import dev.fmsea.util.Pair;
import dev.fmsea.util.Properties;
import dev.fmsea.util.Streams;
import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;

public abstract class OctagonState implements State {

    protected final Set<Local> locals;
    protected final Map<Local, Pair<Integer, Integer>> localsToIndices;
    protected final Map<Integer, Local> indicesToLocals;
    protected OctagonDifferenceBoundedMatrix matrix;
    protected final int N;
    protected final OctagonUpdater updater;
    protected final OctagonUpdater refiner;
    protected final VariableVisitor variableVisitor = new VariableVisitor();
    protected final OctagonChangedVariableVisitor deltaVisitor = new OctagonChangedVariableVisitor();
    private static Logger LOGGER = LoggerFactory.getLogger(OctagonState.class);

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public OctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new DefaultOctagonUpdater(), new DefaultOctagonRefiner());
    }

    public OctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        this.locals = Collections.unmodifiableSet(locals);
        this.N = this.locals.size() * 2;
        this.localsToIndices = Streams.zipToMap(this.locals.stream().sorted((a, b) -> a.toString().compareTo(b.toString())),
                                                IntStream.range(0, this.locals.size())
                                                .boxed()
                                                .map(i -> i * 2)
                                                .map(i -> Pair.of(i, i ^ 1)));
        this.indicesToLocals = this.localsToIndices.entrySet().stream()
            .flatMap(kv -> Stream.of(Pair.of(kv.getValue().fst(), kv.getKey()),
                                     Pair.of(kv.getValue().snd(), kv.getKey())))
            .collect(Collectors.toMap(p -> p.fst(),
                                      p -> p.snd()));
        LOGGER.trace("the map between locals to indices: {}", this.localsToIndices);
        LOGGER.trace("the map between locals to indices: {}", this.indicesToLocals);
        this.updater = updater;
        this.refiner = refiner;
        this.matrix = new OctagonDifferenceBoundedMatrix(N, top);
    }

    public OctagonState(OctagonState copyMe) {
        this(copyMe.locals, true);

        copyMe.matrix.copyTo(this.matrix);
    }

    public OctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix matrix) {
        this(locals, true);

        this.matrix = matrix;
    }

    public abstract OctagonState copy();

    public void copyTo(State dest) {
        if (dest instanceof OctagonState) {
            copyTo((OctagonState)dest);
        } else {
            throw new RuntimeException("invalid type for copyTo");
        }
    }

    public void copyTo(OctagonState dest) {
        this.matrix.copyTo(dest.matrix);
    }

    public Set<Local> getLocals() {
        return this.locals.stream().collect(Collectors.toSet());
    }

    public boolean isSubset(State inState) {
        if (inState != null && inState instanceof OctagonState) {
            return isSubset((OctagonState)inState);
        } else {
            return false;
        }
    }

    public boolean isSubset(OctagonState inState) {
        if (inState != null) {
            return this.matrix.isSubset(inState.matrix);
        } else {
            return false;
        }
    }

    public void widenWith(State other) {
        widenWith(other, Set.of());
    }

    public void widenWith(State other, Set<Integer> thresholds) {
        if (other instanceof OctagonState) {
            this.widenWith((OctagonState)other, thresholds);
        } else {
            throw new RuntimeException("invalid type for widenWith");
        }
    }

    public void widenWith(OctagonState other, Set<Integer> thresholds) {
        this.matrix.widenWith(other.matrix, thresholds);
    }

    public void mergeWith(State other) {
        if (other instanceof OctagonState) {
            this.mergeWith((OctagonState)other);
        } else {
            throw new RuntimeException("invalid type for merge with");
        }
    }

    public void mergeWith(OctagonState other) {
        if (other != null) {
            this.close();
            other.close();
            this.matrix.union(other.matrix);
        }
    }

    public void updateState(Local lVar, State inState, Value left, Value right, BinaryOperatorType operator) {
        if (inState instanceof OctagonState) {
            this.updateState(lVar,
                             (OctagonState) inState,
                             left,
                             right,
                             operator);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar,
                            OctagonState inState,
                            Value left,
                            Value right,
                            BinaryOperatorType operator) {
        LOGGER.trace("transferring: [{} = {} {} {}]", lVar, left, operator, right);
        TADR.from(operator, TADR.from(left), TADR.from(right))
            .map(expr -> TADR.newEqExpr(TADR.newVariable(lVar), expr))
            .ifPresent(expr -> update(expr, inState));
    }

    public void updateState(Local lVar, State inState, Value right) {
        if (inState instanceof OctagonState) {
            updateState(lVar, (OctagonState)inState, right);
        } else {
            throw new RuntimeException("invalid type for updateState");
        }
    }

    public void updateState(Local lVar, OctagonState inState, Value right) {
        LOGGER.trace("transferring: [{} = {}]", lVar, right);
        update(TADR.newEqExpr(TADR.from(lVar), TADR.from(right)), inState);
    }

    public boolean updateCond(State inState, Value left, Value right, PredicateType type) {
        if (inState instanceof OctagonState) {
            return this.updateCond((OctagonState) inState, left, right, type);
        } else {
            throw new RuntimeException("invalid type for update condition");
        }
    }

    public boolean updateCond(OctagonState inState,
                              Value left,
                              Value right,
                              PredicateType type) {
        LOGGER.trace("transferring: [{} {} {}]", left, type, right);
        return TADR.from(type, TADR.from(left), TADR.from(right))
            .map(expr -> refine(expr, inState))
            .orElse(true);
    }

    protected abstract boolean applyUpdates(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in);

    protected abstract boolean applyRefinements(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in);

    private Interval32Box getIntervalValue(Local l) {
        return this.matrix.projectToInterval(this.localsToIndices.get(l));
    }

    public void update(TADR expr, OctagonState inState) {
        var updates = updater.update(expr,
            inState::getIntervalValue,
            inState.localsToIndices::get)
            .peek(update -> LOGGER.debug("updating expr {} with {}", expr, update));
        applyUpdates(updates, inState.matrix);
    }

    public boolean close() {
        return this.matrix.canonicalize();
    }

    public boolean isFeasible() {
        return this.matrix.isFeasible();
    }

    public void makeInfeasible() {
        this.matrix.makeInfeasible();
    }

    public boolean refine(TADR expr, OctagonState inState) {
        var updates = refiner.update(
            expr,
            inState::getIntervalValue,
            inState.localsToIndices::get)
            .peek(refinement -> LOGGER.trace("refining expr {} with {}", expr, refinement));
        return applyRefinements(updates, inState.matrix);
    }

    public void forget(Local l) {
        var idx = this.localsToIndices.get(l);
        this.matrix.forgetConstraints(idx.fst());
        this.matrix.forgetConstraints(idx.snd());
    }

    public void updateTop(Local l) {
        this.forget(l);
    }

    public String toSmt() {
        // System.err.println(this.matrix);
        Constraint two = Constraint.of(2);
        if (!this.matrix.isFeasible()) {
            return "false";
        } else if (this.matrix.isTop()) {
            return "true";
        } else {
            var exprs = IntStream.range(0, this.N).boxed()
                .flatMap(i -> {
                    return IntStream.range(0, i).boxed()
                        .flatMap(j -> {
                            if (i == j) {
                                return Stream.of();
                            } else if ((i ^ j) == 1) {
                                Constraint upper = Constraint.divide(this.matrix.getConstraint(j, i), two);
                                Constraint lower = Constraint.multiply(Constraint.divide(this.matrix.getConstraint(i, j), two), Constraint.of(-1));
                                String var = this.indicesToLocals.get(i).toString();
                                return Stream.of(upper.map(c -> String.format("(<= %s %s)",
                                    var, c.toSmt())),
                                    lower.map(c -> String.format("(>= %s %s)",
                                        var, c.toSmt())))
                                    .filter(o -> o.isPresent())
                                    .map(o -> o.get());
                            } else {
                                String s = this.indicesToLocals.get(i).toString();
                                String t = this.indicesToLocals.get(j).toString();
                                if ((i & 1) == 0 && (j & 1) == 0) { // Both "even"  x- - y- <= b
                                    return Stream.of(this.matrix.getConstraint(i, j).map(c -> String.format("(<= %s (+ %s %s))",
                                        s, t, c.toSmt())))
                                        .filter(o -> o.isPresent())
                                        .map(o -> o.get());
                                } else if ((i & 1) == 1 && (j & 1) == 1) { // both "odd" indices y+ - x+ <= b --> reorder
                                    return Stream.of(this.matrix.getConstraint(i, j).map(c -> String.format("(<= %s (+ %s %s))",
                                        t, s, c.toSmt())))
                                        .filter(o -> o.isPresent())
                                        .map(o -> o.get());
                                } else if ((i & 1) == 0 && (j & 1) == 1) { // "even" - "odd" -> x + y
                                    return Stream.of(this.matrix.getConstraint(i, j).map(c -> String.format("(<= %s (- %s %s))",
                                        s, c.toSmt(), t)))
                                        .filter(o -> o.isPresent())
                                        .map(o -> o.get());
                                } else if ((i & 1) == 1 && (j & 1) == 0)  { // "odd" - "even" -> x - y
                                    return Stream.of(this.matrix.getConstraint(i, j).map(c -> String.format("(>= %s (- %s %s))",
                                        s, c.toSmt(), t)))
                                        .filter(o -> o.isPresent())
                                        .map(o -> o.get());
                                } else {
                                    LOGGER.error("What's this??? [i={}, j={}, c = {}]", i, j, this.matrix.getConstraint(i, j));
                                    return Stream.of();
                                }
                            }
                        });
                })
                .collect(Collectors.toSet());
            if (exprs.size() > 1) {
                return exprs.stream().sorted().collect(Collectors.joining(" ", "(and ", ")"));
            } else if (exprs.size() == 1) {
                return exprs.stream().findFirst().orElse("true");
            } else {
                return "true";
            }
        }
    }

    public Optional<BinopExpr> toBinop() {
        return Optional.empty();
    }

    public String toSMT(Local l, SolverWrapper solver) {
        return "false";
    }

    public String toSMT(Set<Local> locals, SolverWrapper solver) {
        return "false";
    }

    public String toSMT(SolverWrapper solver) {
        return this.toSmt();
    }

    public GraphProjection toGraph() {
        return this.matrix.toGraph(i -> {
                return String.format("%s^%s", this.indicesToLocals.get(i).toString(), i % 2 == 0 ? "+" : "-");
            });
    }

    public boolean reduce() {
        return this.matrix.w0zReduction();
    }

    @Override
    public int hashCode() {
        return this.matrix.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof OctagonState) {
            return equals((OctagonState)o);
        } else {
            return false;
        }
    }

    public boolean equals(OctagonState other) {
        if (other != null) {
            return this.matrix.equals(other.matrix);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.matrix.toString();
    }

    public Set<Local> getChangedVariables(BinaryOperatorType type, Value left, Value right) {
        var assignment = TADR.from(type, TADR.from(left), TADR.from(right));
        if (Properties.OutputMinimumChangedVariables) {
            return assignment.map(expr -> expr.accept(deltaVisitor))
                .orElse(Set.of());
        } else {
            return assignment.map(expr -> expr.accept(variableVisitor))
                .orElse(Set.of());
        }
    }

    public Set<Local> getChangedVariables(PredicateType type, Value left, Value right) {
        var compar = TADR.from(type, TADR.from(left), TADR.from(right));
        if (Properties.OutputMinimumChangedVariables) {
            return compar.map(expr -> expr.accept(deltaVisitor))
                .orElse(Set.of());
        } else {
            return compar.map(expr -> expr.accept(variableVisitor))
                .orElse(Set.of());
        }
    }

    public Set<Local> getChangedVariables(Value value) {
        if (Properties.OutputMinimumChangedVariables) {
            return TADR.from(value).accept(deltaVisitor);
        } else {
            return TADR.from(value).accept(variableVisitor);
        }
    }

}
