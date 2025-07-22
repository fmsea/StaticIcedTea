package dev.fmsea.processing.smt;

public class MultiplicationSmtExpression extends BinopSmtExpression {

    public MultiplicationSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitMultiplicationExpr(this);
    }
}
