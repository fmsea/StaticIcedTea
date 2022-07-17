package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;

public class Identifier extends SmtExpression {

    private Local identifier;

    public Identifier(Local identifier) {
        this.identifier = identifier;
    }

    public Value getValue() {
        return this.identifier;
    }

    public Optional<Value> getValue(Local id) {
        if (this.identifier.toString().equals(id.toString())) {
            return Optional.of(identifier);
        } else {
            return Optional.empty();
        }
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return Map.of(this.identifier, Set.of());
    }
}
