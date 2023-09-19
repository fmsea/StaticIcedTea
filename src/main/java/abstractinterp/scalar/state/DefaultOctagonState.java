package abstractinterp.scalar.state;

import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.update.DefaultOctagonRefineThunkVisitor;
import abstractinterp.scalar.state.update.DefaultOctagonRefiner;
import abstractinterp.scalar.state.update.DefaultOctagonThunkVisitor;
import abstractinterp.scalar.state.update.DefaultOctagonUpdater;
import abstractinterp.scalar.state.update.OctagonUpdater;
import soot.Local;

public class DefaultOctagonState extends OctagonState {

    private static DefaultOctagonThunkVisitor updater = new DefaultOctagonThunkVisitor();
    private static DefaultOctagonRefineThunkVisitor refiner = new DefaultOctagonRefineThunkVisitor();
    private static Logger LOGGER = LoggerFactory.getLogger(OctagonState.class);

    /** Create new instance of Octagon with locals, initialized to top or
     * bottom
     *
     * @param locals Jimple Locals to initialize the matrix
     * @param top Create TOP (⟙) elements for each relation or BOT (⟘)
     */
    public DefaultOctagonState(Set<Local> locals, boolean top) {
        this(locals, top, new DefaultOctagonUpdater(), new DefaultOctagonRefiner());
    }

    public DefaultOctagonState(Set<Local> locals, boolean top, OctagonUpdater updater, OctagonUpdater refiner) {
        super(locals, top, updater, refiner);
    }

    public DefaultOctagonState(Set<Local> locals, OctagonDifferenceBoundedMatrix m) {
        super(locals, m);
    }

    public DefaultOctagonState(OctagonState state) {
        super(state);
    }

    public OctagonState copy() {
        return new DefaultOctagonState(this);
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
