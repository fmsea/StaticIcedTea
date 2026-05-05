package dev.fmsea.tadr;

public class SubtractionOp extends BinaryOp {
    public SubtractionOp(TADR left, TADR right) {
        super(left, right);
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
