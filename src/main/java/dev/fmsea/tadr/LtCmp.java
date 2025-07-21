package dev.fmsea.tadr;

public class LtCmp extends BinaryOp {
    public LtCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitLtCmp(this);
    }
}
