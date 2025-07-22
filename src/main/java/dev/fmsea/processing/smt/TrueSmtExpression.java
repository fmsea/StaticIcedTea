package dev.fmsea.processing.smt;

import java.util.Optional;
import java.util.Set;

import soot.Local;

public class TrueSmtExpression extends SmtExpression {

    public TrueSmtExpression() {
        super();
    }

    public Set<Local> getLocals() {
        return Set.of();
    }

    public String toSmt2() {
        return "true";
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.empty();
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<String> toReachableSmt2(Local id) {
        return Optional.empty();
    }

    public Optional<String> toReachableSmt2(Set<Local> sources) {
        return Optional.empty();
    }

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
        return visitor.visitTrue(this);
    }
}
