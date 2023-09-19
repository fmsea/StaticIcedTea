package tadr.rewrite.rules;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.AdditionOp;
import tadr.DivisionOp;
import tadr.EqCmp;
import tadr.GeCmp;
import tadr.GtCmp;
import tadr.LeCmp;
import tadr.LtCmp;
import tadr.MultiplicationOp;
import tadr.NeCmp;
import tadr.NegOp;
import tadr.NotOp;
import tadr.SubtractionOp;
import tadr.TADR;
import tadr.Value;
import tadr.Variable;

public class IntervalFolder implements TADR.Visitor<TADR> {

    public IntervalFolder() {
    }

    public TADR visitEqCmp(EqCmp expr) {
        return TADR.newEqExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitLeCmp(LeCmp expr) {
        return TADR.newLeExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitLtCmp(LtCmp expr) {
        return TADR.newLtExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitGeCmp(GeCmp expr) {
        return TADR.newGeExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitGtCmp(GtCmp expr) {
        return TADR.newGtExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitNeCmp(NeCmp expr) {
        return TADR.newNeExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitNegOp(NegOp expr) {
        return TADR.newNegExpr(
            expr.expr.accept(this));
    }

    public TADR visitNotOp(NotOp expr) {
        return TADR.newNotExpr(
            expr.expr.accept(this));
    }

    public TADR visitAdditionOp(AdditionOp expr) {
        TADR leftExpr = expr.left.accept(this);
        TADR rightExpr = expr.right.accept(this);
        if (leftExpr instanceof Value && rightExpr instanceof Value) {
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.add(lVal, rVal));
        } else if (leftExpr instanceof Value && rightExpr instanceof NegOp) {
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)right.expr).number;
            return TADR.newValue(Interval32Box.subtract(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof Value) {
            NegOp left = (NegOp)leftExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.add(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof NegOp) {
            NegOp left = (NegOp)leftExpr;
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)right.expr).number.copy().negate();
            return TADR.newValue(Interval32Box.add(lVal, rVal));
        } else {
            return expr;
        }
    }

    public TADR visitSubtractionOp(SubtractionOp expr) {
        TADR leftExpr = expr.left.accept(this);
        TADR rightExpr = expr.right.accept(this);
        if (leftExpr instanceof Value && rightExpr instanceof Value) {
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.subtract(lVal, rVal));
        } else if (leftExpr instanceof Value && rightExpr instanceof NegOp) {
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)right.expr).number;
            return TADR.newValue(Interval32Box.add(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof Value) {
            NegOp left = (NegOp)leftExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.subtract(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof NegOp) {
            NegOp left = (NegOp)leftExpr;
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)right.expr).number;
            return TADR.newValue(Interval32Box.add(lVal, rVal));
        } else {
            return expr;
        }
    }

    public TADR visitMultiplicationOp(MultiplicationOp expr) {
        TADR leftExpr = expr.left.accept(this);
        TADR rightExpr = expr.right.accept(this);
        if (leftExpr instanceof Value && rightExpr instanceof Value) {
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.multiply(lVal, rVal));
        } else if (leftExpr instanceof Value && rightExpr instanceof NegOp) {
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)right.expr).number.copy().negate();
            return TADR.newValue(Interval32Box.multiply(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof Value) {
            NegOp left = (NegOp)leftExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.multiply(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof NegOp) {
            NegOp left = (NegOp)leftExpr;
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)right.expr).number.copy().negate();
            return TADR.newValue(Interval32Box.multiply(lVal, rVal));
        } else {
            return expr;
        }
    }

    public TADR visitDivisionOp(DivisionOp expr) {
        TADR leftExpr = expr.left.accept(this);
        TADR rightExpr = expr.right.accept(this);
        if (leftExpr instanceof Value && rightExpr instanceof Value) {
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.divide(lVal, rVal));
        } else if (leftExpr instanceof Value && rightExpr instanceof NegOp) {
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)leftExpr).number;
            Interval32Box rVal = ((Value)right.expr).number.copy().negate();
            return TADR.newValue(Interval32Box.divide(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof Value) {
            NegOp left = (NegOp)leftExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)rightExpr).number;
            return TADR.newValue(Interval32Box.divide(lVal, rVal));
        } else if (leftExpr instanceof NegOp && rightExpr instanceof NegOp) {
            NegOp left = (NegOp)leftExpr;
            NegOp right = (NegOp)rightExpr;
            Interval32Box lVal = ((Value)left.expr).number.copy().negate();
            Interval32Box rVal = ((Value)right.expr).number.copy().negate();
            return TADR.newValue(Interval32Box.divide(lVal, rVal));
        } else {
            return expr;
        }
    }
    public TADR visitVariable(Variable variable) {
        return variable;
    }

    public TADR visitValue(Value value) {
        return value;
    }
}
