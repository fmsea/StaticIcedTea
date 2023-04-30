package processing.smt;

public class AdditionSmtExpression extends BinopSmtExpression {

    public AdditionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(+ %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
