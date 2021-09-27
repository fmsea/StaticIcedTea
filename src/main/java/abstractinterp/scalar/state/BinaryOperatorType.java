package abstractinterp.scalar.state;

import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.BinopExpr;
import soot.jimple.DivExpr;
import soot.jimple.MulExpr;
import soot.jimple.OrExpr;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SubExpr;
import soot.jimple.UshrExpr;
import soot.jimple.XorExpr;
import org.slf4j.LoggerFactory;

public enum BinaryOperatorType {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MODULUS,
    BAND,
    BOR,
    BSHL,
    BSHR,
    BUSHR,
    XOR,
    INVALID;

    public static BinaryOperatorType fromJimple(BinopExpr expr) {
        BinaryOperatorType op;
        if (expr instanceof AddExpr) {
            op = ADDITION;
        } else if (expr instanceof SubExpr) {
            op = SUBTRACTION;
        } else if (expr instanceof MulExpr) {
            op = MULTIPLICATION;
        } else if (expr instanceof DivExpr) {
            op = DIVISION;
        } else if (expr instanceof RemExpr) {
            op = MODULUS;
        } else if (expr instanceof AndExpr) {
            op = BAND;
        } else if (expr instanceof OrExpr) {
            op = BOR;
        } else if (expr instanceof ShlExpr) {
            op = BSHL;
        } else if (expr instanceof ShrExpr) {
            op = BSHR;
        } else if (expr instanceof UshrExpr) {
            op = BUSHR;
        } else if (expr instanceof XorExpr) {
            op = XOR;
        } else {
            LoggerFactory.getLogger(BinaryOperatorType.class)
                .error("No conversion for binary operation: {}", expr);
            op = INVALID;
        }
        return op;
    }
}
