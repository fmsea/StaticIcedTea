package dev.fmsea.processing.smt;

public abstract class BinopSmtExpression extends SmtExpression {
    public final SmtExpression left;
    public final SmtExpression right;

    public BinopSmtExpression(SmtExpression left, SmtExpression right) {
        super();
        this.left = left;
        this.right = right;
    }
}
