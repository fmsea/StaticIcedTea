package dev.fmsea.absint.scalar.state.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.ConstraintForgetThunk;
import dev.fmsea.absint.scalar.state.ConstraintInplaceThunk;
import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class DefaultOctagonRefineThunkVisitor implements ConstraintThunk.Visitor<Boolean> {

    protected static Logger LOGGER = LoggerFactory.getLogger("<Thunk Refinement Visitor>");

    public Boolean visit(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.debug("applying the following thunk: {}", thunk);
        LOGGER.debug("apply to this matrix: {}", m);
        LOGGER.debug("considering this input matrix: {}", in);
        m.putConstraint(thunk.s, thunk.t, thunk.c, in);
        LOGGER.debug("matrix after thunk: {}", m);
        return true;
    }

    public Boolean visit(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        m.forgetConstraints(thunk.i);
        m.forgetConstraints(thunk.ibar);
        return true;
    }

    public Boolean visit(ConstraintInplaceThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        LOGGER.error("refinement doesn't support reassignment");
        return false;
    }
}
