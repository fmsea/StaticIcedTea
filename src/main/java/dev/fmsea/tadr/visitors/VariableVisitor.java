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
    public Set<Local> visit(EqCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(LeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(LtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(GeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(GtCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(NeCmp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(NegOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visit(NotOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visit(AdditionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(SubtractionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(MultiplicationOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(DivisionOp expr) {
        return visitBinaryOp(expr);
    }

    public Set<Local> visit(Variable variable) {
        return Set.of(variable.variable);
    }

    public Set<Local> visit(Value value) {
        return Set.of();
    }
}
