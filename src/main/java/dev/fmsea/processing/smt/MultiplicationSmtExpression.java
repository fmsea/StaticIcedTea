package dev.fmsea.processing.smt;

public class MultiplicationSmtExpression extends BinopSmtExpression {

    public MultiplicationSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(* %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
