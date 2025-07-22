package dev.fmsea.processing.smt;

public class AdditionSmtExpression extends BinopSmtExpression {

    public AdditionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitAdditionExpr(this);
    }
}
