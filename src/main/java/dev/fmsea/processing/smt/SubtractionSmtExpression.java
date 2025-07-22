package dev.fmsea.processing.smt;

public class SubtractionSmtExpression extends BinopSmtExpression {

    public SubtractionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitSubtractionExpr(this);
    }
}
