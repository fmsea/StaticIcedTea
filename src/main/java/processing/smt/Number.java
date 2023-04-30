package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;

public class Number extends SmtExpression {
    private final long value;

    public Number(long value) {
        super();
        this.value = value;
    }

    public Set<Local> getLocals() {
        return Set.of();
    }

    public String toSmt2() {
        if (this.value < 0) {
            return String.format("(- %d)", this.value * -1);
        } else {
            return String.format("%d", this.value);
        }
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.empty();
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<String> toReachableSmt2(Local id) {
        return Optional.empty();
    }

    public Optional<String> toReachableSmt2(Set<Local> sources) {
        return Optional.empty();
    }

    public boolean containsAll(Set<Local> variables) {
        return variables.size() == 0;
    }

    public int getPredicateCount() {
        return 0;
    }

    public SmtGraph toGraph() {
        return SmtGraph.empty();
    }
}
