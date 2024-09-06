package dev.fmsea.absint.scalar.state;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import soot.Local;

public class ZoneDifferenceBoundedMatrixWithChangePriority extends ZoneDifferenceBoundedMatrix {

    protected Set<Local> changedVariables;

    public ZoneDifferenceBoundedMatrixWithChangePriority(Set<Local> locals, boolean top) {
        super(locals, top);
        this.changedVariables = new HashSet<>(4);
    }

    @Override
    public void setConstraint(Local source, Local target, Constraint constraint) {
        this.changedVariables.removeAll(this.changedVariables);
        this.changedVariables.add(source);
        this.changedVariables.add(target);
        super.setConstraint(source, target, constraint);
    }

    @Override
    public void setConstraint(int i, int j, Constraint c) {
        this.changedVariables.removeAll(this.changedVariables);
        Stream.of(i, j).forEach(index -> this.changedVariables.add(this.indicesToLocals.get(index)));
        super.setConstraint(i, j, c);
    }

    @Override
    public boolean putConstraint(Local source, Local target, Constraint constraint) {
        boolean ret = super.putConstraint(source, target, constraint);
        this.changedVariables.removeAll(this.changedVariables);
        if (ret) {
            this.changedVariables.add(source);
            this.changedVariables.add(target);
        }
        return ret;
    }

    @Override
    public boolean putConstraint(int i, int j, Constraint c) {
        boolean ret = super.putConstraint(i, j, c);
        this.changedVariables.removeAll(this.changedVariables);
        if (ret) {
            Stream.of(i, j).forEach(index -> this.changedVariables.add(this.indicesToLocals.get(index)));
        }
        return ret;
    }

    @Override
    public boolean putIncremental(Local source, Local target, Constraint constraint) {
        this.changedVariables.removeAll(this.changedVariables);
        this.changedVariables.add(source);
        this.changedVariables.add(target);
        return super.putIncremental(source, target, constraint);
    }

    @Override
    public boolean putIncremental(Local source,
                                  Local target,
                                  Constraint constraint,
                                  ZoneDifferenceBoundedMatrix in) {
        this.changedVariables.removeAll(this.changedVariables);
        this.changedVariables.add(source);
        this.changedVariables.add(target);
        return super.putIncremental(source, target, constraint, in);
    }

    @Override
    public boolean equals(ZoneDifferenceBoundedMatrix other) {
        if (this.locals.size() != other.locals.size()) {
            return false;
        } else {
            if (!this.changedVariables.isEmpty()) {
                Set<Integer> indices = this.changedVariables.stream().map(l -> this.localToIndices.get(l))
                    .collect(Collectors.toSet());
                for (int i : indices) {
                    if (!(this.matrix[i][0].equals(other.matrix[i][0]) &&
                          this.matrix[0][i].equals(other.matrix[0][i]))) {
                        return false;
                    }
                    for (int j : indices) {
                        if (!(this.matrix[i][j].equals(other.matrix[i][j]) &&
                              this.matrix[j][i].equals(other.matrix[j][i]))) {
                            return false;
                        }
                    }
                }
            }
            return super.equals(other);
        }
    }
}
