package processing.util;

import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FlowSet<T> {

    private Set<T> fallThrough;
    private Set<T> branchOut;

    public FlowSet() {
        this.fallThrough = new HashSet<>();
        this.branchOut = new HashSet<>();
    }

    public void addFallThrough(T v) {
        this.fallThrough.add(v);
    }

    public void addAllFallThrough(Collection<T> vs) {
        this.fallThrough.addAll(vs);
    }

    public void addBranchOut(T v) {
        this.branchOut.add(v);
    }

    public void addAllBranchOut(Collection<T> vs) {
        this.branchOut.addAll(vs);
    }

    public Set<T> getFallThrough() {
        return Collections.unmodifiableSet(this.fallThrough);
    }

    public Set<T> getBranchOut() {
        return Collections.unmodifiableSet(this.branchOut);
    }
}
