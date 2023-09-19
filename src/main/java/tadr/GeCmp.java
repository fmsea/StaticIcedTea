package tadr;

public class GeCmp extends BinaryOp {
    public GeCmp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(>= %s %s)", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitGeCmp(this);
    }
}
