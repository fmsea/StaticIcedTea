package dev.fmsea.tadr;

public class DivisionOp extends BinaryOp {
    public DivisionOp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(div %s %s)", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitDivisionOp(this);
    }
}
