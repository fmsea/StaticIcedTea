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

    public Map<Local, Set<Local>> getConnectedVariables() {
        return Map.of();
    }

    public Optional<Value> getValue(Local id) {
        return Optional.empty();
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return Optional.empty();
    }

    public String toSmt2() {
        return "true";
    }

    public Optional<String> toSmt2(Local id) {
        return Optional.empty();
    }
}
