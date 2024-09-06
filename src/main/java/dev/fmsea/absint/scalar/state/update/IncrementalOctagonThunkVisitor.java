package dev.fmsea.absint.scalar.state.update;

import dev.fmsea.absint.scalar.state.ConstraintForgetThunk;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;

public class IncrementalOctagonThunkVisitor extends DefaultOctagonThunkVisitor {

    @Override
    public Boolean visitThunk(ConstraintUpdateThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        return m.putIncremental(thunk.s, thunk.t, thunk.c, m);
    }

    @Override
    public Boolean visitForgetThunk(ConstraintForgetThunk thunk, OctagonDifferenceBoundedMatrix m, OctagonDifferenceBoundedMatrix in) {
        m.forgetConstraintsSimple(thunk.i);
        m.forgetConstraintsSimple(thunk.ibar);
        return true;
    }
}
