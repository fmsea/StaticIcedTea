package dev.fmsea.processing.smt;

public class ModulusSmtExpression extends BinopSmtExpression {

    public ModulusSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitModulusExpr(this);
    }
}
