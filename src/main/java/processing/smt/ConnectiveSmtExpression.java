package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.jimple.Jimple;
import soot.IntType;
import soot.grimp.Grimp;

import processing.Locals;

public abstract class ConnectiveSmtExpression extends SmtExpression {
    private List<SmtExpression> expressions;

    public ConnectiveSmtExpression(List<SmtExpression> expressions) {
        this.expressions = expressions;
    }

    public ConnectiveSmtExpression() {
        this(new ArrayList<>());
    }

    public List<SmtExpression> getExpressions() {
        return Collections.unmodifiableList(this.expressions);
    }

    public void addExpression(SmtExpression expr) {
        this.expressions.add(expr);
    }

    protected Value getValue(BinaryOperator<Value> combinator) {
        return this.expressions.stream()
            .map(e -> e.getValue())
            .reduce(combinator)
            .get();
    }

    protected Optional<Value> getValue(Local id, BinaryOperator<Value> combinator) {
        Set<Local> variables = new HashSet<>();
        variables.addAll(this.getConnectedVariables().getOrDefault(id, Set.of()));
        variables.add(id);

        return variables.stream()
            .map(v -> this.expressions.stream().map(e -> e.getValue(v)))
            .flatMap(s -> s.map(v -> v))
            .filter(o -> o.isPresent())
            .map(o -> o.get())
            .collect(Collectors.toSet())
            .stream()
            .sorted((a, b) -> a.toString().compareTo(b.toString()))
            .reduce(combinator);
    }

    private static Set<Local> mergeSets(Set<Local> v1, Set<Local> v2) {
        return Stream.concat(v1.stream(),
                             v2.stream())
            .collect(Collectors.toSet());
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        return this.expressions.stream()
            .map(expr -> expr.getConnectedVariables())
            .reduce((a, b) -> {
                    return Stream.concat(a.entrySet().stream(),
                                         b.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                                  Map.Entry::getValue,
                                                  ConnectiveSmtExpression::mergeSets));
                })
            .get();
    }
}
