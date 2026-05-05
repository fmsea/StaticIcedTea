package dev.fmsea.tadr.visitors;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import soot.Local;

public class OctagonChangedVariableVisitor implements TADR.Visitor<Set<Local>> {

    public Set<Local> visit(EqCmp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visit(LeCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty()) {
            return left;
        } else {
            return right;
        }
    }

    public Set<Local> visit(LtCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty()) {
            return left;
        } else {
            return right;
        }
    }

    public Set<Local> visit(GeCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!right.isEmpty()) {
            return right;
        } else {
            return left;
        }
    }

    public Set<Local> visit(GtCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!right.isEmpty()) {
            return right;
        } else {
            return left;
        }
    }

    public Set<Local> visit(NeCmp expr) {
        return Set.of();
    }

    public Set<Local> visit(NegOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visit(NotOp expr) {
        return Set.of();
    }

    public Set<Local> visit(AdditionOp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visit(SubtractionOp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visit(MultiplicationOp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visit(DivisionOp expr) {
        return Set.of();
    }

    public Set<Local> visit(Variable variable) {
        return Set.of(variable.variable);
    }

    public Set<Local> visit(Value value) {
        return Set.of();
    }
}
