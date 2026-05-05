package dev.fmsea.tadr.rewrite;

import java.util.function.Function;
import java.util.stream.Stream;

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

public class IntervalProjectionRewriter implements TADR.Visitor<Stream<TADR>> {

    private final Function<Local, Interval32Box> env;

    public IntervalProjectionRewriter(Function<Local, Interval32Box> env) {
        this.env = env;
    }

    public Stream<TADR> visit(PrimeAssign expr) {
        return Stream.of(expr);
    }

    public Stream<TADR> visit(EqCmp expr) {
        return expr.right.accept(this)
            .flatMap(right -> Stream.<TADR>of(
                TADR.newLeExpr(expr.left, right),
                TADR.newGeExpr(expr.left, right)));
    }

    public Stream<TADR> visit(LeCmp expr) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            return Stream.concat(
                expr.left.accept(this)
                    .map(left -> TADR.newGeExpr(expr.right, left)),
                expr.right.accept(this)
                    .map(right -> TADR.newLeExpr(expr.left, right)));
        } else {
            return expr.right.accept(this)
                .map(right -> TADR.newLeExpr(expr.left, right));
        }
    }

    public Stream<TADR> visit(LtCmp expr) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            return Stream.concat(
                expr.left.accept(this)
                    .map(left -> TADR.newGtExpr(expr.right, left)),
                expr.right.accept(this)
                    .map(right -> TADR.newLtExpr(expr.left, right)));
        } else {
        return expr.right.accept(this)
            .map(right -> TADR.newLtExpr(expr.left, right));
        }
    }

    public Stream<TADR> visit(GeCmp expr) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            return Stream.concat(
                expr.left.accept(this)
                    .map(left -> TADR.newLeExpr(expr.right, left)),
                expr.right.accept(this)
                    .map(right -> TADR.newGeExpr(expr.left, right)));
        } else {
            return expr.right.accept(this)
                .map(right -> TADR.newGeExpr(expr.left, right));
        }
    }

    public Stream<TADR> visit(GtCmp expr) {
        if (expr.left instanceof Variable && expr.right instanceof Variable) {
            return Stream.concat(
                expr.left.accept(this)
                    .map(left -> TADR.newLtExpr(expr.right, left)),
                expr.right.accept(this)
                    .map(right -> TADR.newGtExpr(expr.left, right)));
        } else {
            return expr.right.accept(this)
                .map(right -> TADR.newGtExpr(expr.left, right));
        }
    }

    public Stream<TADR> visit(NeCmp expr) {
        return Stream.of(expr);
    }

    public Stream<TADR> visit(NegOp expr) {
        return expr.expr.accept(this).map(e -> TADR.newNegExpr(e));
    }

    public Stream<TADR> visit(NotOp expr) {
        return Stream.of(expr);
    }

    public Stream<TADR> visit(AdditionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newAddExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visit(SubtractionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newSubExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visit(MultiplicationOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newMulExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visit(DivisionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newDivExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visit(Variable variable) {
        return Stream.of(TADR.newValue(env.apply(variable.variable)));
    }

    public Stream<TADR> visit(Value value) {
        return Stream.of(value);
    }
}
