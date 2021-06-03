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

    /** negate the inequality expression
     *
     * This is different than rotation because we are multiplying the
     * expression by -1.
     * Rotation is considering the "side" of the inequality.
     */
    public PredicateType negate() {
        switch (this) {
        case Le:
            return Ge;
        case Lt:
            return Gt;
        case Ge:
            return Le;
        case Gt:
            return Lt;
        case Eq:
            return Eq;
        case Ne:
            return Ne;
        case Invalid:
        default:
            return Invalid;
        }
    }

    public static PredicateType minimum(PredicateType a, PredicateType b) {
        if (a == b) {
            return a;
        } else if ((a == Le && b == Lt) || (a == Lt && b == Le)) {
            return Lt;
        } else if ((a == Ge && b == Gt) || (a == Gt && b == Ge)) {
            return Gt;
        } else if ((a == Eq && b == Le) ||
                   (a == Le && b == Eq)) {
            return Le;
        } else if ((a == Eq && b == Ge) ||
                   (a == Ge && b == Eq)) {
            return Ge;
        } else {
            // incomparable
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
