package dev.fmsea.tadr.rewrite.rules;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.DivisionOp;
import dev.fmsea.tadr.EqCmp;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.GtCmp;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.LtCmp;
import dev.fmsea.tadr.MultiplicationOp;
import dev.fmsea.tadr.NeCmp;
import dev.fmsea.tadr.NegOp;
import dev.fmsea.tadr.NotOp;
import dev.fmsea.tadr.PrimeAssign;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

public class IntervalGtLtRewriteRule implements TADR.Visitor<TADR> {

    public TADR visit(PrimeAssign expr) {
        return expr;
    }

    public TADR visit(EqCmp expr) {
        return expr;
    }

    public TADR visit(LeCmp expr) {
        return expr;
    }

    public TADR visit(LtCmp expr) {
        TADR right = expr.right.accept(this);
        if (right instanceof Value) {
            Interval32Box val = ((Value)right).number;
            return TADR.newLeExpr(expr.left, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))));
        } else if (right instanceof NegOp) {
            NegOp r = (NegOp)right;
            Interval32Box val = ((Value)r.expr).number.copy().negate();
            return TADR.newLeExpr(expr.left, TADR.newValue(Interval32Box.subtract(val, Interval32Box.of(1))));
        } else {
            return expr;
        }
    }

    public TADR visit(GeCmp expr) {
        return expr;
    }

    public TADR visit(GtCmp expr) {
        TADR right = expr.right.accept(this);
        if (right instanceof Value) {
            Interval32Box val = ((Value)right).number;
            return TADR.newGeExpr(expr.left, TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))));
        } else if (right instanceof NegOp) {
            NegOp r = (NegOp)right;
            Interval32Box val = ((Value)r.expr).number.copy().negate();
            return TADR.newGeExpr(expr.left, TADR.newValue(Interval32Box.add(val, Interval32Box.of(1))));
        } else {
            return expr;
        }
    }

    public TADR visit(NeCmp expr) {
        return expr;
    }

    public TADR visit(NegOp expr) {
        return expr;
    }

    public TADR visit(NotOp expr) {
        return expr;
    }

    public TADR visit(AdditionOp expr) {
        return expr;
    }

    public TADR visit(SubtractionOp expr) {
        return expr;
    }

    public TADR visit(MultiplicationOp expr) {
        return expr;
    }

    public TADR visit(DivisionOp expr) {
        return expr;
    }

    public TADR visit(Variable variable) {
        System.exit(-42); // die!
        return variable;
    }

    public TADR visit(Value value) {
        return value;
    }
}
