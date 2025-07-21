package dev.fmsea.tadr;

public class GeCmp extends BinaryOp {
    public GeCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitGeCmp(this);
    }
}
