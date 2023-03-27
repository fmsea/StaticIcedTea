package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;

public class OrSmtExpression extends ConnectiveSmtExpression {

    public OrSmtExpression() {
        super();
    }

    public OrSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    private static Value combinator(Value a, Value b) {
        return Grimp.v().newOrExpr(a, b);
    }

    public Value getValue() {
        return this.getValue(OrSmtExpression::combinator);
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return this.getValue(variables, OrSmtExpression::combinator);
    }

    public Optional<Value> getConnectedValue(Local id) {
        return this.getConnectedValue(id, OrSmtExpression::combinator);
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return this.getConnectedValue(variables, OrSmtExpression::combinator);
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return this.getReachableValue(sources, OrSmtExpression::combinator);
    }

    @Override
    public String toSmt2() {
        return this.toSmt2(OrSmtExpression::combinator);
    }
}
