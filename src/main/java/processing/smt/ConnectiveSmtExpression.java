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

    public Set<Local> getLocals() {
        return this.expressions.stream()
            .flatMap(expr -> expr.getLocals().stream())
            .collect(Collectors.toSet());
    }

    public List<SmtExpression> getExpressions() {
        return Collections.unmodifiableList(this.expressions);
    }

    public void addExpression(SmtExpression expr) {
        this.expressions.add(expr);
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
            .collect(Collectors.toSet());
        if (exprs.size() > 1) {
            return exprs
                .stream()
                .sorted()
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
            .flatMap(expr -> expr.getLocals().stream())
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
