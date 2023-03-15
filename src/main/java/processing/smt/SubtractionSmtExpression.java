package processing.smt;

import soot.Value;
import soot.grimp.Grimp;

public class SubtractionSmtExpression extends BinopSmtExpression {

    public SubtractionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newSubExpr(this.left.getValue(),
                                    this.right.getValue());
    }
}
