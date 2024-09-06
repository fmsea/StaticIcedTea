package dev.fmsea.tadr.rewrite.rules;

import dev.fmsea.tadr.AdditionOp;
import dev.fmsea.tadr.BinaryOp;
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
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;

public class ReassignmentIntervalFilter implements TADR.Visitor<Boolean> {

    public Boolean visitBinaryOp(BinaryOp expr) {
        return expr.right instanceof Value;
    }

    public Boolean visitEqCmp(EqCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visitLeCmp(LeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visitLtCmp(LtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visitGeCmp(GeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visitGtCmp(GtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visitNeCmp(NeCmp expr) {
        return false;
    }

    public Boolean visitNegOp(NegOp expr) {
        return false;
    }

    public Boolean visitNotOp(NotOp expr) {
        return false;
    }

    public Boolean visitAdditionOp(AdditionOp expr) {
        return false;
    }

    public Boolean visitSubtractionOp(SubtractionOp expr) {
        return false;
    }

    public Boolean visitMultiplicationOp(MultiplicationOp expr) {
        return false;
    }

    public Boolean visitDivisionOp(DivisionOp expr) {
        return false;
    }

    public Boolean visitVariable(Variable variable) {
        return false;
    }

    public Boolean visitValue(Value value) {
        return false;
    }
}
