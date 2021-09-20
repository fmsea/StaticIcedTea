package abstractinterp.scalar.state;

import java.util.Optional;

class PADO01BotConstraint extends PADO01Constraint {
    public PADO01BotConstraint() {
        super(Optional.empty(), true);
    }

    @Override
    public String toString() {
        return "⟘";
    }

    private PADO01Constraint combine(PADO01Constraint c) {
        return this;
    }

    @Override
    public PADO01Constraint add(PADO01Constraint c) {
        return this;
    }

    @Override
    public PADO01Constraint subtract(PADO01Constraint c) {
        return this;
    }

    @Override
    public PADO01Constraint multiply(PADO01Constraint c) {
        return this;
    }

    @Override
    public PADO01Constraint divide(PADO01Constraint c) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof PADO01Constraint) {
            equal = this.equals((PADO01Constraint) o);
        }
        return equal;
    }

    public boolean equals(PADO01Constraint c) {
        boolean equal = false;
        if (c instanceof PADO01BotConstraint || c.isBottom()) {
            equal = true;
        }
        return equal;
    }

}
