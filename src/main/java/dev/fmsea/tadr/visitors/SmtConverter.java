package dev.fmsea.tadr.visitors;

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

public class SmtConverter implements TADR.Visitor<String> {

    public String visitEqCmp(EqCmp expr) {
        return String.format("(= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitLeCmp(LeCmp expr) {
        return String.format("(<= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitLtCmp(LtCmp expr) {
        return String.format("(< %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitGeCmp(GeCmp expr) {
        return String.format("(>= %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitGtCmp(GtCmp expr) {
        return String.format("(> %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitNeCmp(NeCmp expr) {
        return String.format("(not (= %s %s))",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitNegOp(NegOp expr) {
        return String.format("(- %s)", expr.expr.accept(this));
    }

    public String visitNotOp(NotOp expr) {
        return String.format("(not %s)", expr.expr.accept(this));
    }

    public String visitAdditionOp(AdditionOp expr) {
        return String.format("(+ %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitSubtractionOp(SubtractionOp expr) {
        return String.format("(- %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitMultiplicationOp(MultiplicationOp expr) {
        return String.format("(* %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitDivisionOp(DivisionOp expr) {
        return String.format("(div %s %s)",
            expr.left.accept(this),
            expr.right.accept(this));
    }

    public String visitVariable(Variable variable) {
        return variable.variable.toString();
    }

    public String visitValue(Value value) {
        if (value.number.isSingleton() && value.number.lowerBoundOrElse() < 0) {
            return String.format("(- %s)", value.number.lowerBoundOrElse() * -1);
        } else {
            return value.number.toString();
        }
    }
}
