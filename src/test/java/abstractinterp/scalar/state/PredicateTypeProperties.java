package abstractinterp.scalar.state;

import net.jqwik.api.Property;
import net.jqwik.api.ForAll;

public class PredicateTypeProperties {
    @Property
    boolean twoRotationsEqualsNoRotations(@ForAll PredicateType p) {
        return p == p.rotate().rotate();
    }

    @Property
    boolean twoNegationsEqualsNoNegations(@ForAll PredicateType p) {
        return p == p.negate().negate();
    }
}
