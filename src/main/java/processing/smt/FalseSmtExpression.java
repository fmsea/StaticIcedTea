package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

public class FalseSmtExpression extends SmtExpression {

    public FalseSmtExpression() {
        super();
    }

    public Value getValue() {
        return Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(1));
    }

    @Override
    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    @Override
    public Optional<Value> getConnectedValue(Local id) {
        return Optional.empty();
    }

    @Override
    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return Optional.empty();
    }

    @Override
    public Optional<Value> getReachableValue(Set<Local> sources) {
        return Optional.empty();
    }

    @Override
    public String toSmt2() {
        return "false";
    }

    @Override
    public Optional<String> toSmt2(Local id) {
        return Optional.of("false");
    }

    @Override
    public Optional<String> toSmt2(Set<Local> variables) {
        return Optional.of("false");
    }

    @Override
    public Optional<String> toReachableSmt2(Local id) {
        return Optional.of("false");
    }

    @Override
    public Optional<String> toReachableSmt2(Set<Local> sources) {
        return Optional.of("false");
    }


    public boolean containsAll(Set<Local> variables) {
        return variables.size() == 0;
    }

    public int getPredicateCount() {
        return 1;
    }

    public SmtGraph toGraph() {
        return SmtGraph.empty();
    }
}
