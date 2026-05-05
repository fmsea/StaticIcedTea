package dev.fmsea.tadr;

public class EqCmp extends BinaryOp {
    public EqCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
