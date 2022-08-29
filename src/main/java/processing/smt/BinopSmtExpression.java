package processing.smt;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;
import soot.jimple.EqExpr;

import solver.SolverWrapper;
import solver.SolverFactory;

public class BinopSmtExpression extends SmtExpression {
    private final BinopExpr expression;

    public BinopSmtExpression(BinopExpr expression) {
        super();
        this.expression = expression;
    }

    public Value getValue() {
        return this.expression;
    }

    public Optional<Value> getValue(Local id) {
        Set<String> expressionLocals = ValueToMap.getLocals(this.expression)
            .stream()
            .map(l -> l.toString())
            .collect(Collectors.toSet());
        if (expressionLocals.contains(id.toString())) {
            return Optional.of(this.expression);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getValue(Set<Local> variables) {
        if (this.getLocals().containsAll(variables)) {
            return Optional.of(this.expression);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        Map<Local, Set<Local>> reachableVariables = this.getReachableVariables();
        if (reachableVariables.keySet().containsAll(sources)) {
            return Optional.of(this.expression);
        } else {
            return Optional.empty();
        }
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        Map<Local, Set<Local>> connectedVariables = new HashMap<>();
        Value left = this.expression.getOp1();
        Value right = this.expression.getOp2();
        Set<Local> leftLocals = ValueToMap.getLocals(left);
        Set<Local> rightLocals = ValueToMap.getLocals(right);
        leftLocals.stream().forEach(l -> {
                connectedVariables.merge(l, rightLocals, (a , b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        rightLocals.stream().forEach(r -> {
                connectedVariables.merge(r, leftLocals, (a , b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        return connectedVariables;
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        Map<Local, Set<Local>> reachableVariables = new HashMap<>();
        Value left = this.expression.getOp1();
        Value right = this.expression.getOp2();
        Set<Local> leftLocals = ValueToMap.getLocals(left);
        Set<Local> rightLocals = ValueToMap.getLocals(right);
        leftLocals.stream().forEach(l -> {
                reachableVariables.merge(l, rightLocals, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        if (this.expression instanceof EqExpr) {
            rightLocals.stream().forEach(r -> {
                    reachableVariables.merge(r, leftLocals, (a, b) -> {
                            a.addAll(b);
                            return a;
                        });
                });
        }
        return reachableVariables;
    }

    public String toSmt2() {
        SolverWrapper solver = SolverFactory.getSolver();
        return solver.smt2(this.expression);
    }
}
