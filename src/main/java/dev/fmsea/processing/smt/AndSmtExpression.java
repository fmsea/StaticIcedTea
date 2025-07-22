package dev.fmsea.processing.smt;

import java.util.List;

public class AndSmtExpression extends ConnectiveSmtExpression {

    public AndSmtExpression() {
        super();
    }

    public AndSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    public <R> R accept(SmtExpression.Visitor<R> visitor) {
        return visitor.visitAndExpr(this);
    }
}
