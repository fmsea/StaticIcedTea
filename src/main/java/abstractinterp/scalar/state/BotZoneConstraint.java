package abstractinterp.scalar.state;

import java.util.Optional;

class BotZoneConstraint extends ZoneConstraint {
    public BotZoneConstraint() {
        super(Optional.empty(), true);
    }

    @Override
    public String toString() {
        return "⟘";
    }

    private ZoneConstraint combine(ZoneConstraint c) {
        return this;
    }

    @Override
    public ZoneConstraint add(ZoneConstraint c) {
        return this;
    }

    @Override
    public ZoneConstraint subtract(ZoneConstraint c) {
        return this;
    }

    @Override
    public ZoneConstraint multiply(ZoneConstraint c) {
        return this;
    }

    @Override
    public ZoneConstraint divide(ZoneConstraint c) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof ZoneConstraint) {
            equal = this.equals((ZoneConstraint) o);
        }
        return equal;
    }

    public boolean equals(ZoneConstraint c) {
        boolean equal = false;
        if (c instanceof BotZoneConstraint || c.isBottom()) {
            equal = true;
        }
        return equal;
    }

}
