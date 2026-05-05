package dev.fmsea.tadr;

public class PrimeAssign extends TADR {

    public final Variable variable;
    public final Value constant;

    public PrimeAssign(Variable variable, Value constant) {
        this.variable = variable;
        this.constant = constant;
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
