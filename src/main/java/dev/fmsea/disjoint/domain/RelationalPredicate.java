package dev.fmsea.disjoint.domain;

import soot.Value;
import soot.jimple.ConditionExpr;

public class RelationalPredicate extends Predicate {

    public RelationalPredicate(String setOp) {
        op = setOp;
    }

    @Override
    public boolean evaluate(long... solutions) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        String s = "X" + op + "Y";
        return s;
    }

    @Override
    public ConditionExpr instantitate(Value var) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return op.hashCode();
    }

}
