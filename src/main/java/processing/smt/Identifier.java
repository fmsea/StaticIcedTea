package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;

public class Identifier extends SmtExpression {

    private Local identifier;

    public Identifier(Local identifier) {
        super();
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

    public Optional<Value> getValue(Set<Local> variables) {
        if (variables.contains(this.identifier)) {
            return Optional.of(this.identifier);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        if (sources.contains(this.identifier)) {
            return Optional.of(this.identifier);
        } else {
            return Optional.empty();
        }
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return Map.of(this.identifier, Set.of());
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        return Map.of(this.identifier, Set.of(this.identifier));
    }

    public String toSmt2() {
        return this.identifier.toString();
    }

    public Optional<String> toSmt2(Local id) {
        if (this.identifier.toString().equals(id.toString())) {
            return Optional.of(this.identifier.toString());
        } else {
            return Optional.empty();
        }
    }
}
