package dev.fmsea.absint.scalar.state.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.ConstraintForgetThunk;
import dev.fmsea.absint.scalar.state.ConstraintInplaceThunk;
import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class DefaultOctagonThunkVisitor implements ConstraintThunk.Visitor<Boolean> {

    protected static Logger LOGGER = LoggerFactory.getLogger("<Thunk Visitor>");

    public Boolean visitThunk(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.debug("applying the following thunk: {}", thunk);
        LOGGER.debug("apply to this matrix: {}", m);
        LOGGER.debug("considering this input matrix: {}", m);
        m.putConstraint(thunk.s, thunk.t, thunk.c, m);
        LOGGER.debug("matrix after thunk: {}", m);
        return true;
    }

    public Boolean visitForgetThunk(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        m.forgetConstraints(thunk.i);
        m.forgetConstraints(thunk.ibar);
        return true;
    }

    public Boolean visitInplaceThunk(ConstraintInplaceThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.debug("applying the following reassignment thunk: {}", thunk);
        LOGGER.trace("Before Application: {}", m);
        m.addInterval(thunk.s, thunk.c, in);
        m.addOutgoing(thunk.s, thunk.c, in);
        m.subIncoming(thunk.s, thunk.c, in);
        m.addIncoming(thunk.sbar, thunk.c, in);
        m.subOutgoing(thunk.sbar, thunk.c, in);
        LOGGER.trace("After Application: {}", m);
        return true;
    }
}
