package dev.fmsea.tadr;

public class NeCmp extends BinaryOp {
    public NeCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
