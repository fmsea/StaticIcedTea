package dev.fmsea.processing.smt;

import java.util.Collections;
import java.util.List;

public abstract class ConnectiveSmtExpression extends SmtExpression {
    public final List<SmtExpression> expressions;

    public ConnectiveSmtExpression(List<SmtExpression> expressions) {
        super();
        this.expressions = Collections.unmodifiableList(expressions);
    }

    public ConnectiveSmtExpression() {
        this(List.of());
    }
}
