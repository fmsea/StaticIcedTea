package dev.fmsea.processing.smt;

public class LeSmtExpression extends BinopSmtExpression {

    public LeSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitLeExpr(this);
    }
 }
