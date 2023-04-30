package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;

public class NotSmtExpression extends SmtExpression {

    private SmtExpression expr;

    public NotSmtExpression(SmtExpression expr) {
        super();
        this.expr = expr;
    }

    public Set<Local> getLocals() {
        return this.expr.getLocals();
    }

    public String toSmt2() {
        return String.format("(not %s)", this.expr.toSmt2());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.expr.toSmt2(variables).map(smt -> String.format("(not %s)", smt));
    }

    @Override
    public boolean containsAll(Set<Local> variables) {
        return this.expr.containsAll(variables);
    }

    public int getPredicateCount() {
        return this.expr.getPredicateCount();
    }

    public SmtGraph toGraph() {
        return this.expr.toGraph();
    }
}
