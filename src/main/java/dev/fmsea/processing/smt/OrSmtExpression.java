package dev.fmsea.processing.smt;

import java.util.List;

public class OrSmtExpression extends ConnectiveSmtExpression {

    public OrSmtExpression() {
        super();
    }

    public OrSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitOrExpr(this);
    }
}
