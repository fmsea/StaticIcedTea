package dev.fmsea.processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.fmsea.processing.Smt2UnionType;
import dev.fmsea.processing.smt.visitors.ContainsAllVisitor;
import dev.fmsea.processing.smt.visitors.ContainsLocalVisitor;
import dev.fmsea.processing.smt.visitors.LocalsVisitor;
import dev.fmsea.processing.smt.visitors.PredicateCounter;
import dev.fmsea.processing.smt.visitors.SmtConverter;
import dev.fmsea.processing.smt.visitors.SmtGraphConverter;
import dev.fmsea.processing.smt.visitors.SubSmtConverter;
import dev.fmsea.util.Sets;
import soot.Local;

public abstract class SmtExpression {

    public interface Visitor<R> {
        R visitFalse(FalseSmtExpression falsy);
        R visitTrue(TrueSmtExpression truthy);
        R visitAndExpr(AndSmtExpression expr);
        R visitOrExpr(OrSmtExpression expr);
        R visitAdditionExpr(AdditionSmtExpression expr);
        R visitSubtractionExpr(SubtractionSmtExpression expr);
        R visitMultiplicationExpr(MultiplicationSmtExpression expr);
        R visitDivisionExpr(DivisionSmtExpression expr);
        R visitModulusExpr(ModulusSmtExpression expr);
        R visitEqExpr(EqSmtExpression expr);
        R visitLeExpr(LeSmtExpression expr);
        R visitLtExpr(LtSmtExpression expr);
        R visitGeExpr(GeSmtExpression expr);
        R visitGtExpr(GtSmtExpression expr);
        R visitNegExpr(NegSmtExpression expr);
        R visitNotExpr(NotSmtExpression expr);
        R visitIdentifier(Identifier identifier);
        R visitNumber(Number number);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static SmtExpression TRUE() {
        return new TrueSmtExpression();
    }

    public static SmtExpression FALSE() {
        return new FalseSmtExpression();
    }

    public Set<Local> getLocals() {
        return this.accept(new LocalsVisitor());
    }

    public Set<Local> getLocals(Local id) {
        return this.getConnectedVariables().get(id);
    }

    public String toString() {
        return this.toSmt2();
    }

    public String toSmt2() {
        return this.accept(new SmtConverter());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.accept(new SubSmtConverter(variables));
    }

    public int getPredicateCount() {
        return this.accept(new PredicateCounter());
    }

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

    public static Set<Local> union(Set<Local> changedVariables,
                                   SmtExpression left,
                                   SmtExpression right,
                                   Smt2UnionType method) {
        switch (method) {
        case CONNECTED:
            return connectedUnion(changedVariables, left, right);
        case REACHABLE:
        default:
            return reachableUnion(changedVariables, left, right);
        }
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
        double variables = IntStream.of(1,
                                        left.getLocals().size(),
                                        right.getLocals().size()).max().getAsInt();
        int s1sup = 0;
        int s2sup = 0;
        int neither = 0;
        double proportion;
        Set<Local> v1 = leftChanged;
        Set<Local> v2 = rightChanged;
        Set<Local> s1 = connective.apply(left, v1);
        Set<Local> s2 = connective.apply(right, v2);
        Set<Local> previous = Sets.union(v1, v2);
        Set<Local> current = Sets.union(s1, s2);
        while (!(Sets.equal(current, previous))) {
            if (Sets.subset(s2, s1)) { // s1 ⊃ s2
                s1sup++;
                v2 = Sets.difference(s1, s2);
                s2 = Sets.union(s2, connective.apply(right, v2));
            } else if (Sets.subset(s1, s2)) { // s2 ⊃ s1
                s2sup++;
                v1 = Sets.difference(s2, s1);
                s1 = Sets.union(s1, connective.apply(left, v1));
            } else {
                neither++;
                v1 = Sets.difference(s2, s1);
                v2 = Sets.difference(s1, s2);
                s1 = Sets.union(s1, connective.apply(left, v1));
                s2 = Sets.union(s2, connective.apply(right, v2));
            }
            previous = current;
            current = Sets.union(s1, s2);
        }
        proportion = current.size() / variables;
        System.err.println(String.format("union\t%d\t%d\t%d\t%f", s1sup, s2sup, neither, proportion));
        return current;
    }

    public boolean contains(Local identifier) {
        return this.accept(new ContainsLocalVisitor(identifier));
    }

    public boolean containsAll(Set<Local> variables) {
        return this.accept(new ContainsAllVisitor(variables));
    }

    public SmtGraph toGraph() {
        return this.accept(new SmtGraphConverter());
    }
}
