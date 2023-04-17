package processing.smt;

import soot.Value;
import soot.grimp.Grimp;

public class ModulusSmtExpression extends BinopSmtExpression {

    public ModulusSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newRemExpr(this.left.getValue(),
                                    this.right.getValue());
    }

    public String toSmt2() {
        return String.format("(mod %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }
}
