package dev.fmsea.processing.smt;

public class EqSmtExpression extends BinopSmtExpression {

    public EqSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitEqExpr(this);
    }
}
