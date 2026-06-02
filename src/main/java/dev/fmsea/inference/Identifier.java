package dev.fmsea.inference;

import soot.Local;

public class Identifier extends InvariantExpression {
    public final Local identifier;

    public Identifier(Local local) {
        this.identifier = local;
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
