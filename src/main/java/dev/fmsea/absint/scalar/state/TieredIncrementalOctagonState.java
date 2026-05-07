package dev.fmsea.absint.scalar.state;

import java.util.Set;

import dev.fmsea.absint.scalar.state.factory.OctagonDifferenceBoundedMatrixFactory;
import dev.fmsea.absint.scalar.state.factory.OctagonDifferenceBoundedMatrixType;
import dev.fmsea.absint.scalar.state.update.IncrementalOctagonRefiner;
import dev.fmsea.absint.scalar.state.update.IncrementalOctagonUpdater;
import dev.fmsea.absint.scalar.state.update.OctagonUpdater;
import soot.Local;

public class TieredIncrementalOctagonState extends IncrementalOctagonState {

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public TieredIncrementalOctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new IncrementalOctagonUpdater(), new IncrementalOctagonRefiner());
    }

    public TieredIncrementalOctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        super(locals, top, updater, refiner, new OctagonDifferenceBoundedMatrixFactory(OctagonDifferenceBoundedMatrixType.TIERED));
    }

    public TieredIncrementalOctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix m) {
        super(locals, m);
    }

    public TieredIncrementalOctagonState(OctagonState state) {
        super(state);
    }

    public OctagonState copy() {
        return new IncrementalOctagonState(this);
    }

    @Override
    public void mergeWith(OctagonState other) {
        if (other != null && other instanceof IncrementalOctagonState) {
            mergeWith((IncrementalOctagonState)other);
        }
    }

    public void mergeWith(IncrementalOctagonState other) {
        // We assume each state is closed, therefore, we do not need to close
        // them again before merging flows.
        if (other != null) {
            this.matrix.union(other.matrix);
        }
    }
}
