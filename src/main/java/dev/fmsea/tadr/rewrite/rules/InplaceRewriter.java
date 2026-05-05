package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;

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
import soot.Local;

public class InplaceRewriter implements TADR.Visitor<TADR> {

    private final Variable var;
    private final Function<Local, Interval32Box> lookup;

    public InplaceRewriter(Variable var, Function<Local, Interval32Box> lookup) {
        this.var = var;
        this.lookup = lookup;
    }

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
        return expr;
    }

    public TADR visit(GeCmp expr) {
        return expr;
    }

    public TADR visit(GtCmp expr) {
        return expr;
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
        return TADR.newAddExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visit(SubtractionOp expr) {
        return TADR.newSubExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visit(MultiplicationOp expr) {
        return TADR.newMulExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public TADR visit(DivisionOp expr) {
        return TADR.newDivExpr(
            expr.left.accept(this),
            expr.right.accept(this));
    }
    public TADR visit(Variable variable) {
        if (!var.equals(variable)) {
            return TADR.newValue(lookup.apply(variable.variable));
        } else {
            return variable;
        }
    }

    public TADR visit(Value value) {
        return value;
    }
}
