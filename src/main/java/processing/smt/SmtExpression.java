package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;

public abstract class SmtExpression {

    public abstract Value getValue();

    /** Return Value which are connected to the local `id`.
     *
     * If `id` is not in the expression, then result shall be empty.
     */
    public abstract Optional<Value> getValue(Local id);

    @Override
    public String toString() {
        return this.getValue().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SmtExpression) {
            return this.equals((SmtExpression) o);
        } else {
            return false;
        }
    }

    public boolean equals(SmtExpression o) {
        return this.getValue().equivTo(o.getValue());
    }

    public abstract Map<Local, Set<Local>> getConnectedVariables();

    public boolean contains(Local identifier) {
        return ValueToMap.getLocals(this.getValue()).contains(identifier);
    }
}
