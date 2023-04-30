package processing.smt;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import soot.Local;

public class NegSmtExpression extends SmtExpression {
    private SmtExpression expr;

    public NegSmtExpression(SmtExpression expr) {
        super();
        this.expr = expr;
    }

    @Override
    public Set<Local> getLocals() {
        return this.expr.getLocals();
    }

    @Override
    public boolean containsAll(Set<Local> variables) {
        return this.expr.containsAll(variables);
    }

    public String toSmt2() {
        return String.format("(- %s)", this.expr.toSmt2());
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.expr.toSmt2(variables).map(smt -> String.format("(- %s)", smt));
    }

    public int getPredicateCount() {
        return this.expr.getPredicateCount();
    }

    public SmtGraph toGraph() {
        return this.expr.toGraph();
    }
}
