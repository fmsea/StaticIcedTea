package processing.smt;

public class SubtractionSmtExpression extends BinopSmtExpression {

    public SubtractionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(- %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
