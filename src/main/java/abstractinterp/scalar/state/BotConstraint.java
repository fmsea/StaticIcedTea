package abstractinterp.scalar.state;

import java.util.Optional;

class BotConstraint extends Constraint {
    public BotConstraint() {
        super(Optional.empty(), true);
    }

    @Override
    public String toString() {
        return "⟘";
    }

    private Constraint combine(Constraint c) {
        return this;
    }

    @Override
    public Constraint add(Constraint c) {
        return this;
    }

    @Override
    public Constraint subtract(Constraint c) {
        return this;
    }

    @Override
    public Constraint multiply(Constraint c) {
        return this;
    }

    @Override
    public Constraint divide(Constraint c) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof Constraint) {
            equal = this.equals((Constraint) o);
        }
        return equal;
    }

    public boolean equals(Constraint c) {
        boolean equal = false;
        if (c instanceof BotConstraint || c.isBottom()) {
            equal = true;
        }
        return equal;
    }

}
