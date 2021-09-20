package abstractinterp.scalar.state;

import java.util.Optional;

class PADO01TopConstraint extends PADO01Constraint {
    public PADO01TopConstraint() {
        super(Optional.empty(), false);
    }

    @Override
    public String toString() {
        return "⟙";
    }

    private PADO01Constraint combine(PADO01Constraint c) {
        if (c.isBottom()) {
            return c;
        } else {
            return this;
        }
    }

    @Override
    public PADO01Constraint add(PADO01Constraint c) {
        return this.combine(c);
    }

    @Override
    public PADO01Constraint subtract(PADO01Constraint c) {
        return this.combine(c);
    }

    @Override
    public PADO01Constraint multiply(PADO01Constraint c) {
        return this.combine(c);
    }

    @Override
    public PADO01Constraint divide(PADO01Constraint c) {
        return this.combine(c);
    }

}
