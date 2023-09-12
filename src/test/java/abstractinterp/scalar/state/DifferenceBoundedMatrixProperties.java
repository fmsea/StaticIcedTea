package abstractinterp.scalar.state;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

public class DifferenceBoundedMatrixProperties {

    @Property
    boolean closureAndFeasibilityAreSame(@ForAll DifferenceBoundedMatrix m) {
        return m.computeClosure() == m.isFeasible();
    }
}
