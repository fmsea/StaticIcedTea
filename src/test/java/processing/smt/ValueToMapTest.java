package processing.smt;

import java.util.Set;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import soot.Value;
import soot.Local;
import soot.IntType;
import soot.jimple.IntConstant;
import soot.jimple.BinopExpr;
import soot.jimple.Jimple;
import soot.grimp.Grimp;

import processing.Locals;

public class ValueToMapTest {

    @Test
    public void testValueToMapGetLocals() {
        {
            assertEquals(Set.of(), ValueToMap.getLocals(IntConstant.v(0)));
        }

        {
            Local i0 = Locals.get("i0");
            assertEquals(Set.of(i0), ValueToMap.getLocals(i0));
        }

        {
            Local i0 = Locals.get("i0");
            Local i1 = Locals.get("i1");
            assertEquals(Set.of(i0, i1), ValueToMap.getLocals(Grimp.v().newAddExpr(i0, i1)));
        }

        {
            Local i0 = Locals.get("i0");
            Local i1 = Locals.get("i1");
            assertEquals(Set.of(i0, i1),
                         ValueToMap.getLocals(Grimp.v().newLeExpr(i0, Grimp.v().newAddExpr(i1, IntConstant.v(3)))));
        }
    }

    @Test
    void testValueToMapGetRightLocals() {
        {
            assertEquals(Set.of(), ValueToMap.getRightLocals(IntConstant.v(0)));
        }

        {
            Local i0 = Locals.get("i0");
            assertEquals(Set.of(), ValueToMap.getRightLocals(i0));
        }

        {
            Local i0 = Locals.get("i0");
            Local i1 = Locals.get("i1");
            assertEquals(Set.of(i1), ValueToMap.getRightLocals(Grimp.v().newAddExpr(i0, i1)));
        }

        {
            Local i0 = Locals.get("i0");
            Local i1 = Locals.get("i1");
            assertEquals(Set.of(i1),
                         ValueToMap.getRightLocals(Grimp.v().newLeExpr(i0, Grimp.v().newAddExpr(i1, IntConstant.v(3)))));
        }
    }
}
