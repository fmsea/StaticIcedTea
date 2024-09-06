package dev.fmsea.tadr.visitors;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
