package tadr;

public class AdditionOp extends BinaryOp {
    public AdditionOp(TADR left, TADR right) {
        super(left, right);
    }

    public String toSmt() {
        return String.format("(+ %s %s)", left.toSmt(), right.toSmt());
    }

    @Override
    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitAdditionOp(this);
    }
}
