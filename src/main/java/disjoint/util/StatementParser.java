package disjoint.util;

import java.util.Optional;
import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.Local;
import soot.ShortType;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.InstanceFieldRef;
import soot.grimp.Grimp;

public class StatementParser {

    private StatementParser() {
    }

    public static Optional<BinopExpr> parse(AssignStmt stmt) {
        Value lhs = stmt.getLeftOp();
        Value rhs = stmt.getRightOp();
        if (isAnyIntType(lhs)) {
            return Optional.of(Grimp.v().newEqExpr(lhs, rhs));
        } else {
            return Optional.empty();
        }
    }

    private static boolean isAnyIntType(Value v) {
        Type t = v.getType();
        return (!(v instanceof ArrayRef) &&
                !(v instanceof InstanceFieldRef) &&
                (t instanceof IntType ||
                 t instanceof ByteType ||
                 t instanceof ShortType ||
                 t instanceof BooleanType));
    }
}
