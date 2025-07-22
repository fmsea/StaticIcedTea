package dev.fmsea.processing.smt;

public class GtSmtExpression extends BinopSmtExpression {

    public GtSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitGtExpr(this);
    }
}
