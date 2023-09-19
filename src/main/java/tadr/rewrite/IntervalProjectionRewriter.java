package tadr.rewrite;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import abstractinterp.scalar.state.Interval32Box;
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
import tadr.Variable;
import tadr.Value;

public class IntervalProjectionRewriter implements TADR.Visitor<Stream<TADR>> {

    private final Function<Local, Interval32Box> env;

    public IntervalProjectionRewriter(Function<Local, Interval32Box> env) {
        this.env = env;
    }

    public Stream<TADR> visitEqCmp(EqCmp expr) {
        return expr.right.accept(this)
            .flatMap(right -> Stream.<TADR>of(
                TADR.newLeExpr(expr.left, right),
                TADR.newGeExpr(expr.left, right)));
    }

    public Stream<TADR> visitLeCmp(LeCmp expr) {
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

    public Stream<TADR> visitLtCmp(LtCmp expr) {
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

    public Stream<TADR> visitGeCmp(GeCmp expr) {
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

    public Stream<TADR> visitGtCmp(GtCmp expr) {
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

    public Stream<TADR> visitNeCmp(NeCmp expr) {
        return Stream.of(expr);
    }

    public Stream<TADR> visitNegOp(NegOp expr) {
        return expr.expr.accept(this).map(e -> TADR.newNegExpr(e));
    }

    public Stream<TADR> visitNotOp(NotOp expr) {
        return Stream.of(expr);
    }

    public Stream<TADR> visitAdditionOp(AdditionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newAddExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visitSubtractionOp(SubtractionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newSubExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visitMultiplicationOp(MultiplicationOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newMulExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visitDivisionOp(DivisionOp expr) {
        return expr.left.accept(this)
            .flatMap(left -> {
                return expr.right.accept(this).map(right -> {
                        return TADR.newDivExpr(left, right);
                    });
            });
    }

    public Stream<TADR> visitVariable(Variable variable) {
        return Stream.of(TADR.newValue(env.apply(variable.variable)));
    }

    public Stream<TADR> visitValue(Value value) {
        return Stream.of(value);
    }
}
