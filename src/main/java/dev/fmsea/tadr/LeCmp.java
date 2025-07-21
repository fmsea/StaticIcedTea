package dev.fmsea.tadr;

public class LeCmp extends BinaryOp {
    public LeCmp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitLeCmp(this);
    }
}
