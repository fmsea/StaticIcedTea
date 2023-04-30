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

import util.Sets;

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

    public abstract Set<Local> getLocals();

    public Set<Local> getLocals(Local id) {
        return this.getConnectedVariables().get(id);
    }

    public String toString() {
        return this.toSmt2();
    }

    public abstract String toSmt2();

    public abstract Optional<String> toSmt2(Set<Local> variables);

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
        if (o == null) {
            return false;
        } else {
            return this.toSmt2().equals(o.toSmt2());
        }
    }

    @Override
    public int hashCode() {
        return this.toSmt2().hashCode();
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
        Set<Local> v1 = leftChanged;
        Set<Local> v2 = rightChanged;
        Set<Local> s1 = connective.apply(left, v1);
        Set<Local> s2 = connective.apply(right, v2);
        Set<Local> previous = Sets.union(v1, v2);
        Set<Local> current = Sets.union(s1, s2);
        while (!(Sets.equal(current, previous))) {
            if (Sets.subset(s2, s1)) { // s1 ⊃ s2
                v2 = Sets.difference(s1, s2);
                s2 = Sets.union(s2, connective.apply(right, v2));
            } else if (Sets.subset(s1, s2)) { // s2 ⊃ s1
                v1 = Sets.difference(s2, s1);
                s1 = Sets.union(s1, connective.apply(left, v1));
            } else {
                v1 = Sets.difference(s2, s1);
                v2 = Sets.difference(s1, s2);
                s1 = Sets.union(s1, connective.apply(left, v1));
                s2 = Sets.union(s2, connective.apply(right, v2));
            }
            previous = current;
            current = Sets.union(s1, s2);
        }
        return current;
    }

    public boolean contains(Local identifier) {
        return this.getLocals().contains(identifier);
    }

    public boolean containsAll(Set<Local> variables) {
        return this.getLocals().containsAll(variables);
    }

    public abstract SmtGraph toGraph();
}
