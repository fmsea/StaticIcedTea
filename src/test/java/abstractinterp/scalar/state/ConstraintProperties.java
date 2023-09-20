package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class ConstraintProperties {

    @Property
    boolean testCompareToTop(@ForAll Constraint c) {
        return Constraint.compare(c, Constraint.TOP()) == -1;
    }

    @Property
    boolean testCompareToTopReflexive(@ForAll Constraint c) {
        return Constraint.compare(Constraint.TOP(), c) == 1;
    }

    @Property
    boolean testCompareToBot(@ForAll Constraint c) {
        return Constraint.compare(c, Constraint.BOT()) == 1;
    }

    @Property
    boolean testCompareToBotReflexive(@ForAll Constraint c) {
        return Constraint.compare(Constraint.BOT(), c) == -1;
    }

    @Property
    boolean testCopy(@ForAll Constraint c) {
        Constraint d = c.copy();
        return d.equals(c) && d.hashCode() == c.hashCode();
    }
}
