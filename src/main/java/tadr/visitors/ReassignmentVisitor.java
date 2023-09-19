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
