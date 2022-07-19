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
        return "false";
    }
}
