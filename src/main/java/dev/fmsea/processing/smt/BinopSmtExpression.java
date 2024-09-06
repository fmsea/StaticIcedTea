package dev.fmsea.processing.smt;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;

public abstract class BinopSmtExpression extends SmtExpression {
    protected final SmtExpression left;
    protected final SmtExpression right;

    public BinopSmtExpression(SmtExpression left, SmtExpression right) {
        super();
        this.left = left;
        this.right = right;
    }

    public Set<Local> getLocals() {
        return Stream.concat(this.left.getLocals().stream(),
                             this.right.getLocals().stream()).collect(Collectors.toSet());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        BiPredicate<Local, SmtExpression> contains = (v, expr) -> {
            Set<Local> locals = expr.getLocals();
            return locals.isEmpty() || locals.contains(v);
        };
        if (variables.stream().map(v -> contains.test(v, left)).reduce((a, b) -> a || b).orElse(false) &&
            variables.stream().map(v -> contains.test(v, right)).reduce((a, b) -> a || b).orElse(false)) {
            return Optional.of(this.toSmt2());
        } else {
            return Optional.empty();
        }
    }

    public boolean containsAll(Set<Local> variables) {
        return this.getLocals().containsAll(variables);
    }

    public int getPredicateCount() {
        return 0;
    }

    public SmtGraph toGraph() {
        return SmtGraph.union(this.left.toGraph(),
                              this.right.toGraph());
    }
}
