package dev.fmsea.processing.smt;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;

public class ValueToMap {

    public static Set<Local> getLocals(Value expr) {
        if (expr instanceof IntConstant) {
            return Set.of();
        } else if (expr instanceof Local) {
            return Set.of((Local) expr);
        } else if (expr instanceof BinopExpr) {
            BinopExpr e = (BinopExpr)expr;
            Set<Local> locals = Stream.concat(getLocals(e.getOp1()).stream(),
                                              getLocals(e.getOp2()).stream())
                .collect(Collectors.toSet());
            return locals;
        } else {
            return Set.of();
        }
    }

    public static Set<Local> getLeftLocals(Value expr) {
        if (expr instanceof BinopExpr) {
            BinopExpr e = (BinopExpr)expr;
            return getLocals(e.getOp1());
        } else {
            return Set.of();
        }
    }

    /** Get locals which are "pointed to"/"reachable" from the left?
     */
    public static Set<Local> getRightLocals(Value expr) {
        if (expr instanceof BinopExpr) {
            BinopExpr e = (BinopExpr)expr;
            return getLocals(e.getOp2());
        } else {
            return Set.of();
        }
    }
}
