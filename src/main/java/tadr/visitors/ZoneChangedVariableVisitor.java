package tadr.visitors;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public class ZoneChangedVariableVisitor implements TADR.Visitor<Set<Local>> {

    public Set<Local> visitEqCmp(EqCmp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visitLeCmp(LeCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty()) {
            return left;
        } else {
            return right;
        }
    }

    public Set<Local> visitLtCmp(LtCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty()) {
            return left;
        } else {
            return right;
        }
    }

    public Set<Local> visitGeCmp(GeCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!right.isEmpty()) {
            return right;
        } else {
            return left;
        }
    }

    public Set<Local> visitGtCmp(GtCmp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!right.isEmpty()) {
            return right;
        } else {
            return left;
        }
    }

    public Set<Local> visitNeCmp(NeCmp expr) {
        return Stream.concat(
            expr.left.accept(this).stream(),
            expr.right.accept(this).stream())
            .collect(Collectors.toSet());
    }

    public Set<Local> visitNegOp(NegOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitNotOp(NotOp expr) {
        return expr.expr.accept(this);
    }

    public Set<Local> visitAdditionOp(AdditionOp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty() && !right.isEmpty()) {
            return Set.of();
        } else if (right.isEmpty()) {
            return left;
        } else {
            return right;
        }
    }

    public Set<Local> visitSubtractionOp(SubtractionOp expr) {
        Set<Local> left = expr.left.accept(this);
        Set<Local> right = expr.right.accept(this);
        if (!left.isEmpty() && !right.isEmpty()) {
            return Set.of();
        } else if (!left.isEmpty()) {
            return left;
        } else {
            return Set.of();
        }
    }

    public Set<Local> visitMultiplicationOp(MultiplicationOp expr) {
        return Set.of();
    }

    public Set<Local> visitDivisionOp(DivisionOp expr) {
        return Set.of();
    }

    public Set<Local> visitVariable(Variable variable) {
        return Set.of(variable.variable);
    }

    public Set<Local> visitValue(Value value) {
        return Set.of();
    }
}
