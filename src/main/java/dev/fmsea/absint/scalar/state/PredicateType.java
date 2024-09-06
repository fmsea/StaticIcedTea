package dev.fmsea.absint.scalar.state;

import soot.Value;
import soot.jimple.Jimple;
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

    public static int compare(PredicateType a, PredicateType b) {
        int order;
        if (a == b) {
            order = 0;
        } else if ((a == Eq && b == Le) ||
                   (a == Eq && b == Ge) ||
                   (a == Lt && b == Le) ||
                   (a == Gt && b == Ge)) {
            order = -1;
        } else if ((a == Le && b == Lt) ||
                   (a == Ge && b == Gt) ||
                   (a == Ge && b == Eq) ||
                   (a == Le && b == Eq)) {
            order = 1;
        } else {
            // Incomparable
            order = 2;
        }
        return order;
    }

    public static PredicateType superior(PredicateType a, PredicateType b) {
        int order = compare(a, b);
        if (order == 0) {
            return a;
        } else if (order == -1) {
            return b;
        } else if (order == +1) {
            return a;
        } else {
            // Incomparable (order == 2)
            return Invalid;
        }
    }

    public static PredicateType inferior(PredicateType a, PredicateType b) {
        int order = compare(a, b);
        if (order == 0) {
            return a;
        } else if (order == -1) {
            return a;
        } else if (order == +1) {
            return b;
        } else {
            // Incomparable (order == 2)
            return Invalid;
        }
    }

    public static PredicateType minimum(PredicateType a, PredicateType b) {
        return inferior(a, b);
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

    public static ConditionExpr toJimple(PredicateType t, Value lhs, Value rhs) {
        switch (t) {
        case Eq:
            return Jimple.v().newEqExpr(lhs, rhs);
        case Ne:
            return Jimple.v().newNeExpr(lhs, rhs);
        case Ge:
            return Jimple.v().newGeExpr(lhs, rhs);
        case Gt:
            return Jimple.v().newGtExpr(lhs, rhs);
        case Le:
            return Jimple.v().newLeExpr(lhs, rhs);
        case Lt:
            return Jimple.v().newLtExpr(lhs, rhs);
        case Invalid:
        default:
            throw new IllegalArgumentException("Cannot create Jimple expression for unsupported Predicate type.");
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
