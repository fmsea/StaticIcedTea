package dev.fmsea.inference;

public class Numeral extends InvariantExpression {
    public final int value;

    public Numeral(int value) {
        this.value = value;
    }

    public <R> R accept(InvariantExpression.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
