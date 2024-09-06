package dev.fmsea.tadr;

public class SubtractionOp extends BinaryOp {
    public SubtractionOp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(- %s %s)", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitSubtractionOp(this);
    }
}
