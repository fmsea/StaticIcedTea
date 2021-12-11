package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class PADO01DifferenceBoundedMatrixProperties {

    @Property
    boolean testCompareToTop(@ForAll PADO01Constraint c) {
        return PADO01Constraint.compare(c, PADO01Constraint.TOP()) == -1;
    }
}
