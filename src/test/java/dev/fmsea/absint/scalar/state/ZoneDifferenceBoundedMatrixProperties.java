package dev.fmsea.absint.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class ZoneDifferenceBoundedMatrixProperties {

    @Property
    boolean closureAndFeasibilityAreSame(@ForAll ZoneDifferenceBoundedMatrix m) {
        return m.computeClosure() == m.isFeasible();
    }
}
