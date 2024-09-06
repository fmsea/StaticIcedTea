package dev.fmsea.processing.smt;

public class DivisionSmtExpression extends BinopSmtExpression {

    public DivisionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(div %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
