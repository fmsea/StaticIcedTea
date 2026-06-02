package dev.fmsea.inference;

public class NegIdentifier extends InvariantExpression {
    public final Identifier identifier;

    public NegIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
