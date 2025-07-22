package dev.fmsea.processing.smt;

import java.util.Optional;
import java.util.Set;

import soot.Local;

public class FalseSmtExpression extends SmtExpression {

    public FalseSmtExpression() {
        super();
    }

    public Set<Local> getLocals() {
        return Set.of();
    }

    @Override
    public String toSmt2() {
        return "false";
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.of("false");
    }

    @Override
    public Optional<String> toSmt2(Set<Local> variables) {
        return Optional.of("false");
    }


    @Override
    public boolean containsAll(Set<Local> variables) {
        return variables.size() == 0;
    }

    public int getPredicateCount() {
        return 1;
    }

    public SmtGraph toGraph() {
        return SmtGraph.empty();
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitFalse(this);
    }
}
