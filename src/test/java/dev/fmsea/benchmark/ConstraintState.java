package dev.fmsea.benchmark;

import java.util.Set;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import dev.fmsea.absint.scalar.state.providers.ConstraintUpdateThunkProvider;

@State(Scope.Thread)
public class ConstraintState {
    @Param({"1"})
    int count;

    Set<ConstraintUpdateThunk> thunks;

    @Setup
    public void prepare() {
        this.thunks = ConstraintUpdateThunkProvider.sample(count);
    }
}
