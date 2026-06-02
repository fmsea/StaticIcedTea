package dev.fmsea.inference;

public class LtInvariant extends InvariantExpression {
    public final InvariantExpression left;
    public final InvariantExpression right;

    public LtInvariant(InvariantExpression left, InvariantExpression right) {
        this.left = left;
        this.right = right;
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
