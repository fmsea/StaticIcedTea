package tadr.rewrite.rules;

import java.util.function.Function;

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

public class InplaceRewriter implements TADR.Visitor<TADR> {

    private final Variable var;
    private final Function<Local, Interval32Box> lookup;

    public InplaceRewriter(Variable var, Function<Local, Interval32Box> lookup) {
        this.var = var;
        this.lookup = lookup;
    }

    public TADR visitEqCmp(EqCmp expr) {
        return expr;
    }

    public TADR visitLeCmp(LeCmp expr) {
        return expr;
    }

    public TADR visitLtCmp(LtCmp expr) {
        return expr;
    }

    public TADR visitGeCmp(GeCmp expr) {
        return expr;
    }

    public TADR visitGtCmp(GtCmp expr) {
        return expr;
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
        return TADR.newAddExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitSubtractionOp(SubtractionOp expr) {
        return TADR.newSubExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitMultiplicationOp(MultiplicationOp expr) {
        return TADR.newMulExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visitDivisionOp(DivisionOp expr) {
        return TADR.newDivExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }
    public TADR visitVariable(Variable variable) {
        if (!var.equals(variable)) {
            return TADR.newValue(lookup.apply(variable.variable));
        } else {
            return variable;
        }
    }

    public TADR visitValue(Value value) {
        return value;
    }
}
