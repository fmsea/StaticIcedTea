package dev.fmsea.processing.smt;

public class LtSmtExpression extends BinopSmtExpression {

    public LtSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitLtExpr(this);
    }
}
