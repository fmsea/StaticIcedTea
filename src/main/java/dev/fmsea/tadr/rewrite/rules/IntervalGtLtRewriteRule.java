package dev.fmsea.tadr.rewrite.rules;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import soot.Local;
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
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Variable;
import dev.fmsea.tadr.Value;

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
