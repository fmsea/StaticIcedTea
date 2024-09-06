package dev.fmsea.processing.smt;

public class ModulusSmtExpression extends BinopSmtExpression {

    public ModulusSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(mod %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
