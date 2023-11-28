package abstractinterp.scalar.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IntConstant;
import util.Pair;

/** Deferred Comparison Map between two Values.
 *
 * Enables refinement of long values since a different instruction is used to
 * compare long types.  Since longs take two stack places, comparison between
 * variables needs to be done by the `lcmp` bytecode instruction.  The result
 * of the comparison is pushed onto the stack, it is then compared to 0 via the
 * "original" predicate type, e.g., >= -> `ifge`, etc
 */
public class DeferredCmpMap {

    private Map<Local, Pair<Value, Value>> comparisons;

    public DeferredCmpMap() {
        this.comparisons = new HashMap<>();
    }

    public boolean contains (Local key) {
        return this.comparisons.containsKey(key);
    }

    public void put(Local key, Value lhs, Value rhs) {
        this.comparisons.put(key, Pair.of(lhs, rhs));
    }

    public Optional<Pair<Value, Value>> get(Local key) {
        return Optional.ofNullable(this.comparisons.get(key));
    }

    public void remove(Local key) {
        this.comparisons.remove(key);
    }
}
