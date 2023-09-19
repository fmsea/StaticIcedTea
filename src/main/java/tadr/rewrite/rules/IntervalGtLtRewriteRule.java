package tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import tadr.Variable;
import tadr.Value;

public class IntervalGtLtRewriteRule implements TADR.Visitor<TADR> {

    public TADR visitEqCmp(EqCmp expr) {
        return expr;
    }

    public TADR visitLeCmp(LeCmp expr) {
        return expr;
    }

    public TADR visitLtCmp(LtCmp expr) {
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

    public TADR visitGeCmp(GeCmp expr) {
        return expr;
    }

    public TADR visitGtCmp(GtCmp expr) {
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

    public TADR visitNeCmp(NeCmp expr) {
        return expr;
    }

    public TADR visitNegOp(NegOp expr) {
        return expr;
    }

    public TADR visitNotOp(NotOp expr) {
        return expr;
    }

    public TADR visitAdditionOp(AdditionOp expr) {
        return expr;
    }

    public TADR visitSubtractionOp(SubtractionOp expr) {
        return expr;
    }

    public TADR visitMultiplicationOp(MultiplicationOp expr) {
        return expr;
    }

    public TADR visitDivisionOp(DivisionOp expr) {
        return expr;
    }

    public TADR visitVariable(Variable variable) {
        System.exit(-42); // die!
        return variable;
    }

    public TADR visitValue(Value value) {
        return value;
    }
}
