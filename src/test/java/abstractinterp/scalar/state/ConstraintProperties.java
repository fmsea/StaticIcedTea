package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class ConstraintProperties {

    @Property
    boolean testCopy(@ForAll Constraint c) {
        Constraint d = c.copy();
        return d.equals(c) && d.hashCode() == c.hashCode();
    }
}
