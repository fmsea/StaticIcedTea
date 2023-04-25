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

    public Set<Local> getLocals() {
        return Set.of(identifier);
    }

    public Value getValue() {
        return this.identifier;
    }

    public Optional<Value> getValue(Set<Local> variables) {
        if (variables.contains(this.identifier)) {
            return Optional.of(this.identifier);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getConnectedValue(Local id) {
        if (this.identifier.toString().equals(id.toString())) {
            return Optional.of(identifier);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
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

    public Optional<String> toSmt2(Set<Local> variables) {
        if (variables.contains(this.identifier)) {
            return Optional.of(this.toSmt2());
        } else {
            return Optional.empty();
        }
    }

    public boolean containsAll(Set<Local> variables) {
        return variables.contains(this.identifier) && variables.size() == 1;
    }

    public int getPredicateCount() {
        return 0;
    }

    public SmtGraph toGraph() {
        SmtGraph graph = SmtGraph.empty();
        graph.addEdge(this.identifier, this.identifier, this);
        return graph;
    }
}
