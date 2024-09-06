package dev.fmsea.absint.scalar.state.update;

import java.util.LinkedList;
import java.util.List;

import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class DeferredIncrementalOctagonThunkVisitor extends IncrementalOctagonThunkVisitor {

    private List<ConstraintUpdateThunk> updates;

    public DeferredIncrementalOctagonThunkVisitor() {
        super();
        this.updates = new LinkedList<>();
    }

    public void reset() {
        this.updates.clear();
    }

    @Override
    public Boolean visitThunk(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.debug("updates before new thunk: {}", this.updates);
        this.updates.add(thunk);
        LOGGER.debug("Added thunk to updates: {}", this.updates);
        return true;
    }

    public boolean finalize(OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.debug("applied the following thunks: {}", this.updates);
        LOGGER.trace("Before Application: {}", m);
        boolean result = m.incrementalClosure(this.updates);
        LOGGER.trace("After Application: {}", m);
        return result;
    }
}
