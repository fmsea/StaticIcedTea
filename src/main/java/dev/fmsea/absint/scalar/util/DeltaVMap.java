package dev.fmsea.absint.scalar.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DeltaVMap<T, S> {

    private Map<T, S> fallVariables;
    private Map<T, S> branchVariables;

    public DeltaVMap() {
        this.fallVariables = new HashMap<>();
        this.branchVariables = new HashMap<>();
    }

    public void putFall(T key, S value) {
        this.fallVariables.put(key, value);
    }

    public void putBranch(T key, S value) {
        this.branchVariables.put(key, value);
    }

    public Optional<S> getFallVariables(T key) {
        return Optional.ofNullable(this.fallVariables.get(key));
    }

    public Optional<S> getBranchVariables(T key) {
        return Optional.ofNullable(this.branchVariables.get(key));
    }
}
