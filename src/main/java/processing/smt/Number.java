package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.jimple.IntConstant;

public class Number extends SmtExpression {
    private final int value;

    public Number(int value) {
        super();
        this.value = value;
    }

    public Value getValue() {
        return IntConstant.v(this.value);
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<Value> getConnectedValue(Local id) {
        return Optional.empty();
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return Optional.empty();
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
