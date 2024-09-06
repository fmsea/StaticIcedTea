package dev.fmsea.absint.scalar.state;

import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.update.IncrementalOctagonRefineThunkVisitor;
import dev.fmsea.absint.scalar.state.update.IncrementalOctagonRefiner;
import dev.fmsea.absint.scalar.state.update.IncrementalOctagonThunkVisitor;
import dev.fmsea.absint.scalar.state.update.IncrementalOctagonUpdater;
import dev.fmsea.absint.scalar.state.update.OctagonUpdater;
import soot.Local;

public class IncrementalOctagonState extends OctagonState {

    private static IncrementalOctagonThunkVisitor updater = new IncrementalOctagonThunkVisitor();
    private static IncrementalOctagonRefineThunkVisitor refiner = new IncrementalOctagonRefineThunkVisitor();
    private static Logger LOGGER = LoggerFactory.getLogger(OctagonState.class);

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public IncrementalOctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new IncrementalOctagonUpdater(), new IncrementalOctagonRefiner());
    }

    public IncrementalOctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        super(locals, top, updater, refiner);
    }

    public IncrementalOctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix m) {
        super(locals, m);
    }

    public IncrementalOctagonState(OctagonState state) {
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

    @Override
    protected boolean applyUpdates(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        boolean result = thunks.map(t -> t.accept(updater, this.matrix, in)).reduce((a, b) -> a && b).orElse(true);
        LOGGER.trace("Matrix after updates: {}", this.matrix, in);
        LOGGER.debug("applied updates to matrix");
        return result && this.isFeasible();
    }

    @Override
    protected boolean applyRefinements(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        boolean result = thunks.map(t -> t.accept(refiner, this.matrix, in)).reduce((a, b) -> a && b).orElse(true);
        LOGGER.trace("Matrix after updates: {}", this.matrix, in);
        LOGGER.debug("applied updates to matrix");
        return result && this.isFeasible();
    }
}
