package abstractinterp.scalar.state;

import soot.jimple.ConditionExpr;
import soot.jimple.EqExpr;
import soot.jimple.NeExpr;
import soot.jimple.LeExpr;
import soot.jimple.GtExpr;
import soot.jimple.GeExpr;
import soot.jimple.LtExpr;

public enum PredicateType {
    Invalid, // -1
    Eq, // 0
    Ne, // 1
    Le, // 2
    Gt, // 3
    Ge, // 4
    Lt; // 5

    public PredicateType rotate() {
        switch (this) {
        case Invalid:
            return Invalid;
        case Eq:
            return Ne;
        case Ne:
            return Eq;
        case Le:
            return Gt;
        case Gt:
            return Le;
        case Ge:
            return Lt;
        case Lt:
            return Ge;
        default:
            return Invalid;
        }
    }

    public static PredicateType fromJimple(ConditionExpr expr) {
        if (expr instanceof EqExpr) {
            return Eq;
        } else if (expr instanceof NeExpr) {
            return Ne;
        } else if (expr instanceof LeExpr) {
            return Le;
        } else if (expr instanceof GtExpr) {
            return Gt;
        } else if (expr instanceof GeExpr) {
            return Ge;
        } else if (expr instanceof LtExpr) {
            return Lt;
        } else {
            return Invalid;
        }
    }

    @Override
    public String toString() {
        switch (this) {
        case Eq:
            return "==";
        case Ne:
            return "!=";
        case Gt:
            return ">";
        case Ge:
            return ">=";
        case Lt:
            return "<";
        case Le:
            return "<=";
        case Invalid:
        default:
            return "<invalid>";
        }
    }
}
