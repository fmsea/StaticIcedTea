package abstractinterp.scalar.state;

public class TopConstraint extends Constraint {

    public TopConstraint() {
        super(Integer.MAX_VALUE, PredicateType.Le);
    }

    @Override
    public String toString() {
        return "⟙";
    }

    private Constraint combine(Constraint c) {
        if (c.isBottom()) {
            return c;
        }
        return this;
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

    @Override
    public Constraint modulus(Constraint c) {
        return this.combine(c);
    }
}
