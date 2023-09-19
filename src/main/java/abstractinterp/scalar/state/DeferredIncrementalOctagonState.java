package abstractinterp.scalar.state;

import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.update.DeferredIncrementalOctagonRefineThunkVisitor;
import abstractinterp.scalar.state.update.DeferredIncrementalOctagonRefiner;
import abstractinterp.scalar.state.update.DeferredIncrementalOctagonThunkVisitor;
import abstractinterp.scalar.state.update.DeferredIncrementalOctagonUpdater;
import abstractinterp.scalar.state.update.OctagonUpdater;
import soot.Local;

public class DeferredIncrementalOctagonState extends IncrementalOctagonState {

    private static DeferredIncrementalOctagonThunkVisitor updater = new DeferredIncrementalOctagonThunkVisitor();
    private static DeferredIncrementalOctagonRefineThunkVisitor refiner = new DeferredIncrementalOctagonRefineThunkVisitor();
    private static Logger LOGGER = LoggerFactory.getLogger(OctagonState.class);

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public DeferredIncrementalOctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new DeferredIncrementalOctagonUpdater(), new DeferredIncrementalOctagonRefiner());
    }

    public DeferredIncrementalOctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        super(locals, top, updater, refiner);
    }

    public DeferredIncrementalOctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix m) {
        super(locals, m);
    }

    public DeferredIncrementalOctagonState(OctagonState state) {
        super(state);
    }

    @Override
    public void mergeWith(OctagonState other) {
        if (other != null && other instanceof DeferredIncrementalOctagonState) {
            mergeWith((DeferredIncrementalOctagonState)other);
        }
    }

    public void mergeWith(DeferredIncrementalOctagonState other) {
        // We assume each state is closed, therefore, we do not need to close
        // them again before merging flows.
        if (other != null) {
            this.matrix.union(other.matrix);
        }
    }

    @Override
    public OctagonState copy() {
        return new DeferredIncrementalOctagonState(this);
    }

    @Override
    protected boolean applyUpdates(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        updater.reset();
        boolean result = (thunks.map(t -> t.accept(updater, this.matrix, in)).reduce((a, b) -> a && b).orElse(true) &&
                          updater.finalize(this.matrix, in));
        LOGGER.trace("Matrix after updates: {}", this.matrix);
        LOGGER.debug("applied updates to matrix");
        return result && this.isFeasible();
    }

    @Override
    protected boolean applyRefinements(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        refiner.reset();
        boolean result = (thunks.map(t -> t.accept(refiner, this.matrix, in)).reduce((a, b) -> a && b).orElse(true) &&
                          refiner.finalize(this.matrix, in));
        LOGGER.trace("Matrix after updates: {}", this.matrix);
        LOGGER.debug("applied updates to matrix");
        return result && this.isFeasible();
    }
}
