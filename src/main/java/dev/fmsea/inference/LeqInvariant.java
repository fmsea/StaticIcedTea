package dev.fmsea.inference;

public class LeqInvariant extends InvariantExpression {
    public final InvariantExpression left;
    public final InvariantExpression right;

    public LeqInvariant(InvariantExpression left, InvariantExpression right) {
        this.left = left;
        this.right = right;
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
