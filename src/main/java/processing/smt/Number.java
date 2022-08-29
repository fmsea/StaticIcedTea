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

    public Optional<Value> getValue(Local id) {
        return Optional.empty();
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return Optional.empty();
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return Map.of();
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        return Map.of();
    }

    public String toSmt2() {
        return String.format("%d", this.value);
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.empty();
    }
}
