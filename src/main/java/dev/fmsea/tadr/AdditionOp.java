package dev.fmsea.tadr;

public class AdditionOp extends BinaryOp {
    public AdditionOp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitAdditionOp(this);
    }
}
