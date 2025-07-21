package dev.fmsea.tadr;

public class MultiplicationOp extends BinaryOp {
    public MultiplicationOp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitMultiplicationOp(this);
    }
}
