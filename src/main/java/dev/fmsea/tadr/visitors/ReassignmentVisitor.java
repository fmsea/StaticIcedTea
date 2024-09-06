package dev.fmsea.tadr.visitors;

import java.util.Set;

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

public class ReassignmentVisitor implements TADR.Visitor<Boolean> {

    private Variable variable;
    private VariableVisitor variableCounts = new VariableVisitor();

    public Boolean visitEqCmp(EqCmp expr) {
        Set<Local> right = expr.right.accept(variableCounts);
        if (expr.left instanceof Variable) {
            this.variable = (Variable)expr.left;
            return right.size() < 2 && expr.right.accept(this);
        } else {
            return false;
        }
    }

    public Boolean visitLeCmp(LeCmp expr) {
        return false;
    }

    public Boolean visitLtCmp(LtCmp expr) {
        return false;
    }

    public Boolean visitGeCmp(GeCmp expr) {
        return false;
    }

    public Boolean visitGtCmp(GtCmp expr) {
        return false;
    }

    public Boolean visitNeCmp(NeCmp expr) {
        return false;
    }

    public Boolean visitNegOp(NegOp expr) {
        return false;
    }

    public Boolean visitNotOp(NotOp expr) {
        return false;
    }

    public Boolean visitAdditionOp(AdditionOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visitSubtractionOp(SubtractionOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visitMultiplicationOp(MultiplicationOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visitDivisionOp(DivisionOp expr) {
        return false;
    }

    public Boolean visitVariable(Variable variable) {
        return this.variable.equals(variable);
    }

    public Boolean visitValue(Value value) {
        return false;
    }
}
