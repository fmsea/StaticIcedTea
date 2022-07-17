package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.IntConstant;

public class NotSmtExpression extends SmtExpression {

    private SmtExpression expr;

    public NotSmtExpression(SmtExpression expr) {
        this.expr = expr;
    }

    public Value getValue() {
        return Grimp.v().newNeExpr(this.expr.getValue(), IntConstant.v(1));
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return this.expr.getConnectedVariables();
    }

    public Optional<Value> getValue(Local id) {
        return this.expr.getValue(id)
            .map(v -> Grimp.v().newNeExpr(v, IntConstant.v(1)));
    }
}
