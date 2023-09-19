package tadr.visitors;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tadr.TADR;
import tadr.BinaryOp;
import tadr.AdditionOp;
import tadr.SubtractionOp;
import tadr.MultiplicationOp;
import tadr.DivisionOp;
import tadr.NegOp;
import tadr.NotOp;
import tadr.EqCmp;
import tadr.LeCmp;
import tadr.LtCmp;
import tadr.GeCmp;
import tadr.GtCmp;
import tadr.NeCmp;
import tadr.Variable;
import tadr.Value;

import soot.Local;

public class VariableVisitor implements TADR.Visitor<Set<Local>> {
    public Set<Local> visitBinaryOp(BinaryOp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }
    public Set<Local> visitEqCmp(EqCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitLeCmp(LeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitLtCmp(LtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitGeCmp(GeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitGtCmp(GtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitNeCmp(NeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitNegOp(NegOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitNotOp(NotOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitAdditionOp(AdditionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitSubtractionOp(SubtractionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitMultiplicationOp(MultiplicationOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitDivisionOp(DivisionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visitVariable(Variable variable) {
        return Set.of(variable.variable);
    }

    public Set<Local> visitValue(Value value) {
        return Set.of();
    }
}
