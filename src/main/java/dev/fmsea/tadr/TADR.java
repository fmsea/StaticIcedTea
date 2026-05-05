package dev.fmsea.tadr;

import java.util.Optional;

import dev.fmsea.absint.scalar.state.BinaryOperatorType;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.PredicateType;
import dev.fmsea.tadr.visitors.SmtConverter;
import soot.Local;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NegExpr;
import soot.jimple.NullConstant;

public abstract class TADR {

    public interface Visitor<R> {
        R visit(EqCmp expr);
        R visit(LeCmp expr);
        R visit(LtCmp expr);
        R visit(GeCmp expr);
        R visit(GtCmp expr);
        R visit(NeCmp expr);
        R visit(NegOp expr);
        R visit(NotOp expr);
        R visit(AdditionOp expr);
        R visit(SubtractionOp expr);
        R visit(MultiplicationOp expr);
        R visit(DivisionOp expr);
        R visit(Variable variable);
        R visit(Value value);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public String toString() {
        return this.toSmt();
    }

    public String toSmt() {
        return this.accept(new SmtConverter());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TADR) {
            return this.equals((TADR) o);
        } else {
            return false;
        }
    }

    public boolean equals(TADR o) {
        if (o == null) {
            return false;
        } else {
            return this.toSmt().equals(o.toSmt());
        }
    }

    @Override
    public int hashCode() {
        return this.toSmt().hashCode();
    }

    public static LeCmp newLeExpr(TADR left, TADR right) {
        return new LeCmp(left, right);
    }

    public static LtCmp newLtExpr(TADR left, TADR right) {
        return new LtCmp(left, right);
    }

    public static GeCmp newGeExpr(TADR left, TADR right) {
        return new GeCmp(left, right);
    }

    public static GtCmp newGtExpr(TADR left, TADR right) {
        return new GtCmp(left, right);
    }

    public static EqCmp newEqExpr(TADR left, TADR right) {
        return new EqCmp(left, right);
    }

    public static NeCmp newNeExpr(TADR left, TADR right) {
        return new NeCmp(left, right);
    }

    public static NegOp newNegExpr(TADR expr) {
        return new NegOp(expr);
    }

    public static NotOp newNotExpr(TADR expr) {
        return new NotOp(expr);
    }

    public static Value newValue(Interval32Box number) {
        return new Value(number);
    }

    public static Value newValue(int number) {
        return new Value(Interval32Box.of(number));
    }

    public static Value newValue(long number) {
        return new Value(Interval32Box.of(number));
    }

    public static Variable newVariable(Local variable) {
        return new Variable(variable);
    }

    public static AdditionOp newAddExpr(TADR left, TADR right) {
        return new AdditionOp(left, right);
    }

    public static SubtractionOp newSubExpr(TADR left, TADR right) {
        return new SubtractionOp(left, right);
    }

    public static MultiplicationOp newMulExpr(TADR left, TADR right) {
        return new MultiplicationOp(left, right);
    }

    public static DivisionOp newDivExpr(TADR left, TADR right) {
        return new DivisionOp(left, right);
    }

    public static TADR from(soot.Value v) {
        if (v == null) {
            return newValue(Interval32Box.TOP());
        } else if (v instanceof Local) {
            return newVariable((Local) v);
        } else if (v instanceof IntConstant) {
            return newValue(((IntConstant)v).value);
        } else if (v instanceof LongConstant) {
            return newValue(((LongConstant)v).value);
        } else if (v instanceof DoubleConstant) {
            var d = (DoubleConstant)v;
            return newValue(Interval32Box.of(Math.round(d.value)));
        } else if (v instanceof FloatConstant) {
            var f = (FloatConstant)v;
            return newValue(Interval32Box.of(Math.round(f.value)));
        } else if (v instanceof NullConstant) {
            return newValue(Interval32Box.TOP());
        } else if (v instanceof NegExpr) {
            NegExpr expr = (NegExpr)v;
            return newNegExpr(from(expr.getOp()));
        } else {
            return newValue(Interval32Box.TOP());
        }
    }

    public static Optional<BinaryOp> from(BinaryOperatorType type, TADR left, TADR right) {
        switch (type) {
            case ADDITION:
                return Optional.of(newAddExpr(left, right));
            case SUBTRACTION:
                return Optional.of(newSubExpr(left, right));
            case MULTIPLICATION:
                return Optional.of(newMulExpr(left, right));
            case DIVISION:
                return Optional.of(newDivExpr(left, right));
            default:
                return Optional.empty();
        }
    }

    public static Optional<BinaryOp> from(PredicateType type, TADR left, TADR right) {
        switch (type) {
            case Eq:
                return Optional.of(TADR.newEqExpr(left, right));
            case Ne:
                return Optional.of(TADR.newNeExpr(left, right));
            case Le:
                return Optional.of(TADR.newLeExpr(left, right));
            case Lt:
                return Optional.of(TADR.newLtExpr(left, right));
            case Ge:
                return Optional.of(TADR.newGeExpr(left, right));
            case Gt:
                return Optional.of(TADR.newGtExpr(left, right));
            case Invalid:
            default:
                return Optional.empty();
        }
    }
}
