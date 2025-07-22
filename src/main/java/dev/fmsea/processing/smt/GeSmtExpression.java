package dev.fmsea.processing.smt;

public class GeSmtExpression extends BinopSmtExpression {

    public GeSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitGeExpr(this);
    }
}
