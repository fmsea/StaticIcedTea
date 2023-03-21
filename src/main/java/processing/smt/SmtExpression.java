package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import soot.Local;
import soot.Value;

import solver.SolverWrapper;
import solver.SolverFactory;

public abstract class SmtExpression {

    protected SolverWrapper solver;

    public SmtExpression() {
        this.solver = SolverFactory.getSolver();
    }

    public static SmtExpression TRUE() {
        return new TrueSmtExpression();
    }

    public static SmtExpression FALSE() {
        return new FalseSmtExpression();
    }

    public abstract Value getValue();

    /** Return Value which are connected to the local `id`.
     *
     * If `id` is not in the expression, then result shall be empty.
     */
    public abstract Optional<Value> getValue(Local id);

    /** Return Value which is "connected" to the set of variables.
     *
     * If variables do not occur, then result shall be empty.
     */
    public abstract Optional<Value> getValue(Set<Local> variables);

    /** Return Value which is reachable from the local `id`.
     *
     * This should mimic getValue, except instead of "contains", we are looking
     * to see if `source` is in the first location of the BinOp expression, for
     * example.
     *
     * If the expression does not contain reachable expressions, the result is
     * empty.
     */
    public Optional<Value> getReachableValue(Local source) {
        return this.getReachableValue(Set.of(source));
    }

    /** Return value which is reachable from the local sources.
     *
     * This is essentially a union of expressions over the singular version.
     */
    public abstract Optional<Value> getReachableValue(Set<Local> sources);

    public Set<Local> getLocals() {
        return ValueToMap.getLocals(this.getValue());
    }

    public Set<Local> getLocals(Local id) {
        return this.getConnectedVariables().get(id);
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }

    public String toSmt2() {
        return this.solver.smt2(this.getValue());
    }

    public Optional<String> toSmt2(Local id) {
        return this.getValue(id).map(v -> this.solver.smt2(v));
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.getValue(variables).map(v -> this.solver.smt2(v));
    }

    public Optional<String> toReachableSmt2(Local id) {
        return this.toReachableSmt2(Set.of(id));
    }

    public Optional<String> toReachableSmt2(Set<Local> sources) {
        return this.getReachableValue(sources).map(v -> this.solver.smt2(v));
    }

    public abstract int getPredicateCount();

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtExpression) {
            return this.equals((SmtExpression) o);
        } else {
            return false;
        }
    }

    public boolean equals(SmtExpression o) {
        return this.getValue().equivTo(o.getValue());
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return this.toGraph().connectedProjection();
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        return this.toGraph().reachableProjection();
    }

    public Set<Local> getConnectedVariables(Set<Local> variables) {
        Map<Local, Set<Local>> connected = this.getConnectedVariables();
        return variables.stream()
            .flatMap(source -> connected.getOrDefault(source, Set.of()).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> getReachableVariables(Set<Local> variables) {
        Map<Local, Set<Local>> reachable = this.getReachableVariables();
        return variables.stream()
            .flatMap(source -> reachable.getOrDefault(source, Set.of()).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> getNeighbors(Set<Local> variables) {
        Map<Local, Set<Local>> neighbors = this.toGraph().neighborProjection();
        return variables.stream()
            .flatMap(v -> neighbors.getOrDefault(v, Set.of()).stream())
            .collect(Collectors.toSet());
    }

    public static Set<Local> connectedUnion(Set<Local> changedVariables,
                                            SmtExpression left,
                                            SmtExpression right) {
        return SmtExpression.union(changedVariables,
                                   changedVariables,
                                   left,
                                   right,
                                   (expr, vars) -> expr.getConnectedVariables(vars));
    }

    public static Set<Local> reachableUnion(Set<Local> changedVariables,
                                            SmtExpression left,
                                            SmtExpression right) {
        return SmtExpression.union(changedVariables,
                                   changedVariables,
                                   left,
                                   right,
                                   (expr, vars) -> expr.getNeighbors(vars));
    }

    private static Set<Local> union(Set<Local> leftChanged,
                                    Set<Local> rightChanged,
                                    SmtExpression left,
                                    SmtExpression right,
                                    BiFunction<SmtExpression, Set<Local>, Set<Local>> connective) {
        BinaryOperator<Set<Local>> cup = (a, b) -> Stream.concat(a.stream(), b.stream()).collect(Collectors.toSet());
        BinaryOperator<Set<Local>> slash = (a, b) -> a.stream().filter(e -> !b.contains(e)).collect(Collectors.toSet());
        BiPredicate<Set<Local>, Set<Local>> eq = (a, b) -> a.containsAll(b) && b.containsAll(a);
        BiPredicate<Set<Local>, Set<Local>> subset = (a, b) -> b.containsAll(a);
        Set<Local> v1 = leftChanged;
        Set<Local> v2 = rightChanged;
        Set<Local> s1 = connective.apply(left, v1);
        Set<Local> s2 = connective.apply(right, v2);
        Set<Local> previous = cup.apply(v1, v2);
        Set<Local> current = cup.apply(s1, s2);
        while (!(eq.test(current, previous))) {
            if (subset.test(s2, s1)) { // s1 ⊃ s2
                v2 = slash.apply(s1, s2);
                s2 = cup.apply(s2, connective.apply(right, v2));
            } else if (subset.test(s1, s2)) { // s2 ⊃ s1
                v1 = slash.apply(s2, s1);
                s1 = cup.apply(s1, connective.apply(left, v1));
            } else {
                v1 = slash.apply(s2, s1);
                v2 = slash.apply(s1, s2);
                s1 = cup.apply(s1, connective.apply(left, v1));
                s2 = cup.apply(s2, connective.apply(right, v2));
            }
            previous = current;
            current = cup.apply(s1, s2);
        }
        return current;
    }

    public boolean contains(Local identifier) {
        return ValueToMap.getLocals(this.getValue()).contains(identifier);
    }

    public abstract boolean containsAll(Set<Local> variables);

    public abstract SmtGraph toGraph();
}
