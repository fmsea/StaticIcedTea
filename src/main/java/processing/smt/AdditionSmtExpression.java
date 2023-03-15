package processing.smt;

import soot.Value;
import soot.grimp.Grimp;

public class AdditionSmtExpression extends BinopSmtExpression {

    public AdditionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newAddExpr(this.left.getValue(),
                                    this.right.getValue());
    }
}
