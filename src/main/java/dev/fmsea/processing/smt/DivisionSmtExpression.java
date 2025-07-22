package dev.fmsea.processing.smt;

public class DivisionSmtExpression extends BinopSmtExpression {

    public DivisionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitDivisionExpr(this);
    }
}
