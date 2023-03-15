package processing.smt;

import soot.Value;
import soot.grimp.Grimp;

public class DivisionSmtExpression extends BinopSmtExpression {

    public DivisionSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newDivExpr(this.left.getValue(),
                                    this.right.getValue());
    }
}
