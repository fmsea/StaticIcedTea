package processing.smt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import soot.Local;
import soot.Value;
import soot.jimple.BinopExpr;

import solver.SolverWrapper;
import solver.SolverFactory;

public abstract class BinopSmtExpression extends SmtExpression {
    protected final SmtExpression left;
    protected final SmtExpression right;

    public BinopSmtExpression(SmtExpression left, SmtExpression right) {
        super();
        this.left = left;
        this.right = right;
    }

    public Set<Local> getLocals() {
        return Stream.concat(this.left.getLocals().stream(),
                             this.right.getLocals().stream()).collect(Collectors.toSet());
    }

    public Optional<Value> getValue(Local id) {
        Set<String> locals = this.getLocals().stream().map(s -> s.toString()).collect(Collectors.toSet());
        if (locals.contains(id.toString())) {
            return Optional.of(this.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getValue(Set<Local> variables) {
        if (this.getLocals().containsAll(variables)) {
            return Optional.of(this.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        Map<Local, Set<Local>> reachableVariables = this.getReachableVariables();
        if (reachableVariables.keySet().containsAll(sources)) {
            return Optional.of(this.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Map<Local, Set<Local>> getConnectedVariables() {
        Map<Local, Set<Local>> connectedVariables = new HashMap<>();
        Set<Local> leftLocals = this.left.getLocals();
        Set<Local> rightLocals = this.right.getLocals();
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
        return this.getConnectedVariables();
    }

    public String toSmt2() {
        SolverWrapper solver = SolverFactory.getSolver();
        return solver.smt2(this.getValue());
    }

    public boolean containsAll(Set<Local> variables) {
        return this.getLocals().containsAll(variables);
    }

    public int getPredicateCount() {
        return 0;
    }
}
