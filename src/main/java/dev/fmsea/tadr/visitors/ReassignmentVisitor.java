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
import dev.fmsea.tadr.PrimeAssign;
import dev.fmsea.tadr.SubtractionOp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import soot.Local;

public class ReassignmentVisitor implements TADR.Visitor<Boolean> {

    private Variable variable;
    private VariableVisitor variableCounts = new VariableVisitor();

    public Boolean visit(PrimeAssign expr) {
        // I mean, yes, but we're past that.
        return false;
    }

    public Boolean visit(EqCmp expr) {
        Set<Local> right = expr.right.accept(variableCounts);
        if (expr.left instanceof Variable) {
            this.variable = (Variable)expr.left;
            return right.size() < 2 && expr.right.accept(this);
        } else {
            return false;
        }
    }

    public Boolean visit(LeCmp expr) {
        return false;
    }

    public Boolean visit(LtCmp expr) {
        return false;
    }

    public Boolean visit(GeCmp expr) {
        return false;
    }

    public Boolean visit(GtCmp expr) {
        return false;
    }

    public Boolean visit(NeCmp expr) {
        return false;
    }

    public Boolean visit(NegOp expr) {
        return false;
    }

    public Boolean visit(NotOp expr) {
        return false;
    }

    public Boolean visit(AdditionOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visit(SubtractionOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visit(MultiplicationOp expr) {
        return expr.left.accept(this) || expr.right.accept(this);
    }

    public Boolean visit(DivisionOp expr) {
        return false;
    }

    public Boolean visit(Variable variable) {
        return this.variable.equals(variable);
    }

    public Boolean visit(Value value) {
        return false;
    }
}
