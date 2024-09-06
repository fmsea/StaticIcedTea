package dev.fmsea.absint.scalar.state;

import java.util.Optional;

class TopConstraint extends Constraint {
    public TopConstraint() {
        super(Optional.empty(), false);
    }

    @Override
    public String toString() {
        return "⟙";
    }
}
