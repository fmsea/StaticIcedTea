package dev.fmsea.absint.scalar.state.update;

import java.util.LinkedList;
import java.util.List;

import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class DeferredOctagonThunkVisitor extends DefaultOctagonThunkVisitor {

    private List<ConstraintUpdateThunk> updates;

    public DeferredOctagonThunkVisitor() {
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
        this.updates.stream().forEach(thunk -> {
            m.putConstraint(thunk.s, thunk.t, thunk.c, m);
        });
        LOGGER.trace("After Application: {}", m);
        return m.canonicalize();
    }
}
