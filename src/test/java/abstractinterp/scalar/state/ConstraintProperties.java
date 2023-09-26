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

    @Property
    boolean overflowAdditionBecomesTop(@ForAll Constraint c) {
        if (c.bound().map(b -> b > 0).orElse(false)) {
            return Constraint.TOP().equals(Constraint.add(Constraint.of(Integer.MAX_VALUE), c));
        }
        return true;
    }

    @Property
    boolean underflowSubtractionBecomesTop(@ForAll Constraint c) {
        if (c.bound().map(b -> b > 0).orElse(false)) {
            return Constraint.TOP().equals(Constraint.subtract(Constraint.of(Integer.MIN_VALUE), c));
        }
        return true;
    }
}
