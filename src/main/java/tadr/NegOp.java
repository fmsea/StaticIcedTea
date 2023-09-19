package tadr;

public class NegOp extends TADR {
    public TADR expr;

    public NegOp(TADR expr) {
        this.expr = expr;
    }

    public String toSmt() {
        return String.format("(- %s)", this.expr.toSmt());
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitNegOp(this);
    }
}
