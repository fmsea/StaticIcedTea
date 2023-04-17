package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.IntType;
import soot.grimp.Grimp;

public class AndSmtExpression extends ConnectiveSmtExpression {

    public AndSmtExpression() {
        super();
    }

    public AndSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    private static Value combinator(Value a, Value b) {
        return Grimp.v().newAndExpr(a, b);
    }

    public Value getValue() {
        return this.getValue(AndSmtExpression::combinator);
    }

    public Optional<Value> getValue(Set<Local> variables) {
        return this.getValue(variables, AndSmtExpression::combinator);
    }

    public Optional<Value> getConnectedValue(Local id) {
        return this.getConnectedValue(Set.of(id), AndSmtExpression::combinator);
    }

    public Optional<Value> getConnectedValue(Set<Local> variables) {
        return this.getConnectedValue(variables, AndSmtExpression::combinator);
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        return this.getReachableValue(sources, AndSmtExpression::combinator);
    }

    @Override
    public String toSmt2() {
        return this.toSmt2("and");
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.toSmt2(variables, "and");
    }
}
