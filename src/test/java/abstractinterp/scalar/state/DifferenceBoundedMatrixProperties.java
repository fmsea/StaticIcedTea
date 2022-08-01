package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class DifferenceBoundedMatrixProperties {

    @Property
    boolean testCompareToTop(@ForAll Constraint c) {
        return Constraint.compare(c, Constraint.TOP()) == -1;
    }
}
