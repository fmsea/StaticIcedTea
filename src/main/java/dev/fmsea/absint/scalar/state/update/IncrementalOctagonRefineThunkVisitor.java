package dev.fmsea.absint.scalar.state.update;

import dev.fmsea.absint.scalar.state.ConstraintForgetThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class IncrementalOctagonRefineThunkVisitor extends DefaultOctagonRefineThunkVisitor {

    @Override
    public Boolean visit(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        return m.putIncremental(thunk.s, thunk.t, thunk.c, in);
    }

    @Override
    public Boolean visit(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        m.forgetConstraintsSimple(thunk.i);
        m.forgetConstraintsSimple(thunk.ibar);
        return true;
    }
}
