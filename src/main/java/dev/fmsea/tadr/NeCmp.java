package dev.fmsea.tadr;

public class NeCmp extends BinaryOp {
    public NeCmp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(not (= %s %s))", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitNeCmp(this);
    }
}
