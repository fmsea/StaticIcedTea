package dev.fmsea.inference;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class AndExpression extends InvariantExpression {
    public final List<InvariantExpression> exprs;

    public AndExpression(InvariantExpression... exprs) {
        this.exprs = Arrays.asList(exprs);
    }

    public AndExpression(Collection<InvariantExpression> exprs) {
        this.exprs = List.copyOf(exprs);
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Stream<InvariantExpression> asConjuncts() {
        return this.exprs.stream();
    }
}
