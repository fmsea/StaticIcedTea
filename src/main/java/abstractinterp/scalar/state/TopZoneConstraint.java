package abstractinterp.scalar.state;

import java.util.Optional;

class TopZoneConstraint extends ZoneConstraint {
    public TopZoneConstraint() {
        super(Optional.empty(), false);
    }

    @Override
    public String toString() {
        return "⟙";
    }

    private ZoneConstraint combine(ZoneConstraint c) {
        if (c.isBottom()) {
            return c;
        } else {
            return this;
        }
    }

    @Override
    public ZoneConstraint add(ZoneConstraint c) {
        return this.combine(c);
    }

    @Override
    public ZoneConstraint subtract(ZoneConstraint c) {
        return this.combine(c);
    }

    @Override
    public ZoneConstraint multiply(ZoneConstraint c) {
        return this.combine(c);
    }

    @Override
    public ZoneConstraint divide(ZoneConstraint c) {
        return this.combine(c);
    }

}
