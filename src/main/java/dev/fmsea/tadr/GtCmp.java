package dev.fmsea.tadr;

public class GtCmp extends BinaryOp {
    public GtCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitGtCmp(this);
    }
}
