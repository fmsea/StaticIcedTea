package dev.fmsea.processing.smt;

import java.util.Optional;
import java.util.Set;

import soot.Local;

public class Number extends SmtExpression {
    public final long value;

    public Number(long value) {
        super();
        this.value = value;
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

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitNumber(this);
    }
}
