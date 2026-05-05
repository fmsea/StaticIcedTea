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

    public Boolean visit(EqCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visit(LeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visit(LtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visit(GeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visit(GtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Boolean visit(NeCmp expr) {
        return false;
    }

    public Boolean visit(NegOp expr) {
        return false;
    }

    public Boolean visit(NotOp expr) {
        return false;
    }

    public Boolean visit(AdditionOp expr) {
        return false;
    }

    public Boolean visit(SubtractionOp expr) {
        return false;
    }

    public Boolean visit(MultiplicationOp expr) {
        return false;
    }

    public Boolean visit(DivisionOp expr) {
        return false;
    }

    public Boolean visit(Variable variable) {
        return false;
    }

    public Boolean visit(Value value) {
        return false;
    }
}
