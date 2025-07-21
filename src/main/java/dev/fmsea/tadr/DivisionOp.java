package dev.fmsea.tadr;

public class DivisionOp extends BinaryOp {
    public DivisionOp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitDivisionOp(this);
    }
}
