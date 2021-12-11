package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class PADO01ConstraintProperties {

    @Property
    boolean testCopy(@ForAll PADO01Constraint c) {
        PADO01Constraint d = c.copy();
        return d.equals(c) && d.hashCode() == c.hashCode();
    }
}
