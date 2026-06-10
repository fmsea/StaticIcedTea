package dev.fmsea.absint.scalar.state;

import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.update.DefaultOctagonRefineThunkVisitor;
import dev.fmsea.absint.scalar.state.update.DefaultOctagonThunkVisitor;
import dev.fmsea.absint.scalar.state.update.OctagonUpdater;
import dev.fmsea.absint.scalar.state.update.SimpleOctagonRefiner;
import dev.fmsea.absint.scalar.state.update.SimpleOctagonUpdater;
import soot.Local;

public class SimpleOctagonState extends OctagonState {

    private static DefaultOctagonThunkVisitor updater = new DefaultOctagonThunkVisitor();
    private static DefaultOctagonRefineThunkVisitor refiner = new DefaultOctagonRefineThunkVisitor();
    private static Logger LOGGER = LoggerFactory.getLogger(OctagonState.class);

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public SimpleOctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new SimpleOctagonUpdater(), new SimpleOctagonRefiner());
    }

    public SimpleOctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        super(locals, top, updater, refiner);
    }

    public SimpleOctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix m) {
        super(locals, m);
    }

    public SimpleOctagonState(OctagonState state) {
        super(state);
    }

    public OctagonState copy() {
        return new SimpleOctagonState(this);
    }

    @Override
    protected boolean applyUpdates(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        boolean result = thunks.map(t -> t.accept(updater, this.matrix, this.matrix)).reduce((a, b) -> a && b).orElse(true);
        LOGGER.debug("Matrix after updates: {}", this.matrix);
        LOGGER.debug("applied updates to matrix");
        return result && this.matrix.canonicalize();
    }

    @Override
    protected boolean applyRefinements(Stream<ConstraintThunk> thunks, OctagonDifferenceBoundedMatrix in) {
        LOGGER.trace("Matrix before updates: {}", this.matrix);
        boolean result = thunks.map(t -> t.accept(refiner, this.matrix, in)).reduce((a, b) -> a && b).orElse(true);
        LOGGER.debug("Matrix after updates: {}", this.matrix);
        LOGGER.debug("applied updates to matrix");
        return result && this.matrix.canonicalize();
    }
}
