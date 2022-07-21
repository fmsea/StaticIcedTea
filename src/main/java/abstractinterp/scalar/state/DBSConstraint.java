package abstractinterp.scalar.state;

import java.util.Optional;

public class DBSConstraint {

    private Optional<Integer> bound;
    private boolean bottom = false;

    protected DBSConstraint(int bound) {
        this(Optional.of(bound));
    }

    protected DBSConstraint(Optional<Integer> bound) {
        this(bound, false);
    }

    protected DBSConstraint(Optional<Integer> bound, boolean bottom) {
        if (bound != null) {
            this.bound = bound;
        } else {
            this.bound = Optional.empty();
        }
        this.bottom = bottom;
    }


    public static DBSConstraint from(int bound) {
        return new DBSConstraint(bound);
    }

    public static DBSConstraint from(Optional<Integer> bound) {
        return new DBSConstraint(bound);
    }

    public static DBSConstraint from(Optional<Integer> bound, boolean bottom) {
        return new DBSConstraint(bound, bottom);
    }

    public static DBSConstraint from(ZoneConstraint constraint) {
        return new DBSConstraint(constraint.bound(), constraint.isBottom());
    }

    @Override
    public String toString() {
        if (this.bottom) {
            return "⟘";
        } else {
            return this.bound.map(b -> String.format("%d", b)).orElse("⟙");
        }
    }
}
