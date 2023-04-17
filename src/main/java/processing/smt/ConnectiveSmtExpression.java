package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.ConditionExpr;
import soot.IntType;
import soot.grimp.Grimp;

import processing.Locals;

public abstract class ConnectiveSmtExpression extends SmtExpression {
    private List<SmtExpression> expressions;

    public ConnectiveSmtExpression(List<SmtExpression> expressions) {
        super();
        this.expressions = expressions;
    }

    public ConnectiveSmtExpression() {
        this(new ArrayList<>());
    }

    public List<SmtExpression> getExpressions() {
        return Collections.unmodifiableList(this.expressions);
    }

    public void addExpression(SmtExpression expr) {
        this.expressions.add(expr);
    }

    protected Value getValue(BinaryOperator<Value> combinator) {
        return this.expressions.stream()
            .map(e -> e.getValue())
            .reduce(combinator)
            .get();
    }

    protected Optional<Value> getValue(Set<Local> variables, BinaryOperator<Value> combinator) {
        return this.expressions
            .stream()
            .map(expr -> expr.getValue(variables))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toMap(expr -> expr.equivHashCode(), expr -> expr, (oExpr, nExpr) -> oExpr))
            .values()
            .stream()
            .sorted((a, b) -> a.toString().compareTo(b.toString()))
            .reduce(combinator);
    }

    protected Optional<Value> getConnectedValue(Local id, BinaryOperator<Value> combinator) {
        Set<Local> variables = new HashSet<>();
        variables.addAll(this.getConnectedVariables().getOrDefault(id, Set.of()));
        variables.add(id);

        return variables.stream()
            .map(v -> this.expressions.stream().map(e -> e.getConnectedValue(v)))
            .flatMap(s -> s.map(v -> v))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toMap(expr -> expr.equivHashCode(), expr -> expr, (oExpr, nExpr) -> oExpr))
            .values()
            .stream()
            .sorted((a, b) -> a.toString().compareTo(b.toString()))
            .reduce(combinator);
    }

    protected Optional<Value> getConnectedValue(Set<Local> variables, BinaryOperator<Value> combinator) {
        Set<Local> connectedVariables = new HashSet<>();
        connectedVariables.addAll(variables);
        variables.stream()
            .map(v -> this.getConnectedVariables().getOrDefault(v, Set.of()))
            .forEach(v -> connectedVariables.addAll(v));
        return this.expressions.stream()
            .map(expr -> expr.getConnectedValue(connectedVariables))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toMap(expr -> expr.equivHashCode(), expr -> expr, (oExpr, nExpr) -> oExpr))
            .values()
            .stream()
            .sorted((a, b) -> a.toString().compareTo(b.toString()))
            .reduce(combinator);
    }

    protected Optional<Value> getReachableValue(Set<Local> sources, BinaryOperator<Value> combinator) {
        Set<Local> reachableVariables = new HashSet<>();
        reachableVariables.addAll(sources);
        sources.stream()
            .map(v -> this.getReachableVariables().getOrDefault(v, Set.of()))
            .forEach(v -> reachableVariables.addAll(v));
        return this.expressions.stream()
            .map(expr -> expr.getReachableValue(reachableVariables))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toMap(expr -> expr.equivHashCode(), expr -> expr, (oExpr, nExpr) -> oExpr))
            .values()
            .stream()
            .sorted((a, b) -> a.toString().compareTo(b.toString()))
            .reduce(combinator);
    }

    private static Set<Local> mergeSets(Set<Local> v1, Set<Local> v2) {
        return Stream.concat(v1.stream(),
                             v2.stream())
            .collect(Collectors.toSet());
    }

    protected String toSmt2(String combinator) {
        Set<String> exprs = this.expressions
            .stream()
            .map(expr -> expr.toSmt2())
            .collect(Collectors.toSet());
        if (this.expressions.size() > 1) {
            return this.expressions
                .stream()
                .map(expr -> expr.toSmt2())
                .sorted()
                .collect(Collectors.joining(" ", String.format("(%s ", combinator), ")"));
        } else if (exprs.size() == 1) {
            return this.expressions.get(0).toSmt2();
        } else {
            return "";
        }
    }

    protected Optional<String> toSmt2(Set<Local> variables, String combinator) {
        Set<String> exprs = this.expressions
            .stream()
            .map(expr -> expr.toSmt2(variables))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .sorted()
            .collect(Collectors.toSet());
        if (exprs.size() > 1) {
            return exprs
                .stream()
                .reduce((a, b) -> a + " " + b)
                .map(expr -> String.format("(%s %s)", combinator, expr));
        } else if (exprs.size() == 1) {
            return exprs.stream().findFirst();
        } else {
            return Optional.empty();
        }
    }

    public boolean containsAll(Set<Local> variables) {
        return this.expressions.stream()
            .map(expr -> ValueToMap.getLocals(expr.getValue()))
            .collect(Collectors.toSet())
            .containsAll(variables);
    }

    public int getPredicateCount() {
        return this.expressions.size();
    };

    public SmtGraph toGraph() {
        return this.expressions.stream()
            .map(expr -> expr.toGraph())
            .reduce(SmtGraph::union)
            .orElse(SmtGraph.empty())
            .computeClosure();
    }
}
