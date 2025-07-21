package dev.fmsea.tadr;

public class NotOp extends TADR {
    public final TADR expr;

    public NotOp(TADR expr) {
        this.expr = expr;
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitNotOp(this);
    }
}
