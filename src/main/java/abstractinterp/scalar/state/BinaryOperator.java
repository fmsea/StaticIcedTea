package abstractinterp.scalar.state;

import soot.jimple.BinopExpr;
import soot.jimple.AddExpr;
import soot.jimple.SubExpr;
import soot.jimple.MulExpr;
import soot.jimple.DivExpr;

public enum BinaryOperator {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MODULUS,
    INVALID;

    public static BinaryOperator fromJimple(BinopExpr expr) {
        BinaryOperator op;
        if (expr instanceof AddExpr) {
            op = ADDITION;
        } else if (expr instanceof SubExpr) {
            op = SUBTRACTION;
        } else if (expr instanceof MulExpr) {
            op = MULTIPLICATION;
        } else if (expr instanceof DivExpr) {
            op = DIVISION;
        } else {
            op = INVALID;
        }
        return op;
    }
}
