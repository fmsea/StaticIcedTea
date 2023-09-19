package tadr;

public class LeCmp extends BinaryOp {
    public LeCmp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(<= %s %s)", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitLeCmp(this);
    }
}
