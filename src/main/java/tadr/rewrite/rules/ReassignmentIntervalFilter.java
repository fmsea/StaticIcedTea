package tadr.rewrite.rules;

import tadr.AdditionOp;
import tadr.BinaryOp;
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
