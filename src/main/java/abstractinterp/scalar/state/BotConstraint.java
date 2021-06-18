package abstractinterp.scalar.state;

public class BotConstraint extends Constraint {

    public BotConstraint() {
        super(Integer.MIN_VALUE, PredicateType.Le, true);
    }

    @Override
    public String toString() {
        return "⟘";
    }

    @Override
    public Constraint add(Constraint c) {
        // do nothing, we're already bottom
        return this;
    }

    @Override
    public Constraint subtract(Constraint c) {
        // do nothing, we're already bottom
        return this;
    }

    @Override
    public Constraint multiply(Constraint c) {
        // do nothing, we're already bottom
        return this;
    }

    @Override
    public Constraint divide(Constraint c) {
        // do nothing, we're already bottom
        return this;
    }

    @Override
    public Constraint modulus(Constraint c) {
        // do nothing, we're already bottom
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
