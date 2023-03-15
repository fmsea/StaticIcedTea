package processing.smt;

import soot.Value;
import soot.grimp.Grimp;

public class MultiplicationSmtExpression extends BinopSmtExpression {

    public MultiplicationSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newMulExpr(this.left.getValue(),
                                    this.right.getValue());
    }
}
