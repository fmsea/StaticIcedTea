package abstractinterp.scalar.state;

import java.util.Optional;

class TopConstraint extends Constraint {
    public TopConstraint() {
        super(Optional.empty(), false);
    }

    @Override
    public String toString() {
        return "⟙";
    }

    private Constraint combine(Constraint c) {
        if (c.isBottom()) {
            return c;
        } else {
            return this;
        }
    }

    @Override
    public Constraint add(Constraint c) {
        return this.combine(c);
    }

    @Override
    public Constraint subtract(Constraint c) {
        return this.combine(c);
    }

    @Override
    public Constraint multiply(Constraint c) {
        return this.combine(c);
    }

    @Override
    public Constraint divide(Constraint c) {
        return this.combine(c);
    }

}
