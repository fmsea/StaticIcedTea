package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class ZoneConstraintProperties {

    @Property
    boolean testCopy(@ForAll ZoneConstraint c) {
        ZoneConstraint d = c.copy();
        return d.equals(c) && d.hashCode() == c.hashCode();
    }
}
