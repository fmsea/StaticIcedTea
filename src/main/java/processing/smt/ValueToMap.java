package processing.smt;

import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import soot.Value;
import soot.Local;
import soot.jimple.IntConstant;
import soot.jimple.BinopExpr;

public class ValueToMap {

    public static Set<Local> getLocals(Value expr) {
        if (expr instanceof IntConstant) {
            return Set.of();
        } else if (expr instanceof Local) {
            return Set.of((Local) expr);
        } else if (expr instanceof BinopExpr) {
            BinopExpr e = (BinopExpr)expr;
            Set<Local> locals = new HashSet<>();
            locals.addAll(getLocals(e.getOp1()));
            locals.addAll(getLocals(e.getOp2()));
            return locals;
        } else {
            return Set.of();
        }
    }
}
