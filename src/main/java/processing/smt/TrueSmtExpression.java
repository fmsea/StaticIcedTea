package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;

public class TrueSmtExpression extends SmtExpression {

    public TrueSmtExpression() {
        super();
    }

    public Value getValue() {
        return Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0));
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
        return "true";
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.empty();
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
