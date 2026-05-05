package dev.fmsea.tadr;

public class NegOp extends TADR {
    public TADR expr;

    public NegOp(TADR expr) {
        this.expr = expr;
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
