package dev.fmsea.processing.smt;

import java.util.Optional;
import java.util.Set;

import soot.Local;

public class Identifier extends SmtExpression {

    public final Local identifier;

    public Identifier(Local identifier) {
        super();
        this.identifier = identifier;
    }

    public Optional<String> toSmt2(Local id) {
        if (this.identifier.toString().equals(id.toString())) {
            return Optional.of(this.identifier.toString());
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        if (variables.contains(this.identifier)) {
            return Optional.of(this.toSmt2());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean containsAll(Set<Local> variables) {
        return variables.contains(this.identifier) && variables.size() == 1;
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitIdentifier(this);
    }
}
