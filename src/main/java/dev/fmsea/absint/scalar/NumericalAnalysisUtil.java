package dev.fmsea.absint.scalar;

import soot.BooleanType;
import soot.ByteType;
import soot.IntType;
import soot.LongType;
import soot.ShortType;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;

public class NumericalAnalysisUtil {

    private NumericalAnalysisUtil() {
    }

    public static boolean isIntType(Value val) {
        Type t = val.getType();
        return !(val instanceof ArrayRef)
            && !(val instanceof InstanceFieldRef)
            && (t instanceof IntType ||
                t instanceof LongType ||
                t instanceof ByteType ||
                t instanceof ShortType ||
                t instanceof BooleanType);
    }
}
