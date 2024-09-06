package dev.fmsea.processing.smt;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import soot.Local;

public class AndSmtExpression extends ConnectiveSmtExpression {

    public AndSmtExpression() {
        super();
    }

    public AndSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    @Override
    public String toSmt2() {
        return this.toSmt2("and");
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.toSmt2(variables, "and");
    }
}
