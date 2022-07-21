package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class DifferenceBoundedMatrixProperties {

    @Property
    boolean testCompareToTop(@ForAll ZoneConstraint c) {
        return ZoneConstraint.compare(c, ZoneConstraint.TOP()) == -1;
    }
}
