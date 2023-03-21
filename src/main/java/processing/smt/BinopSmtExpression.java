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
        Set<Integer> locals = this.getLocals().stream().map(s -> s.equivHashCode()).collect(Collectors.toSet());
        if (locals.contains(id.equivHashCode())) {
            return Optional.of(this.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getValue(Set<Local> variables) {
        Set<Local> locals = this.getLocals();
        boolean containsSomeVariables = variables.stream()
            .map(v -> locals.contains(v))
            .reduce((a, b) -> a || b)
            .orElse(false);
        if (containsSomeVariables) {
            return Optional.of(this.getValue());
        } else {
            return Optional.empty();
        }
    }

    public Optional<Value> getReachableValue(Set<Local> sources) {
        Map<Local, Set<Local>> reachableVariables = this.getReachableVariables();
        Set<Local> locals = this.getLocals();
        Set<Value> values = new HashSet<>();
        sources.forEach(s -> {
                Set<Local> reachable = reachableVariables.get(s);
                    if (reachable != null &&
                        reachable.containsAll(locals) &&
                        locals.containsAll(reachable)) {
                        values.add(this.getValue());
                    }
            });
        return values.stream().findFirst();
    }

    public boolean containsAll(Set<Local> variables) {
        return this.getLocals().containsAll(variables);
    }

    public int getPredicateCount() {
        return 0;
    }

    public SmtGraph toGraph() {
        return SmtGraph.union(this.left.toGraph(),
                              this.right.toGraph());
    }
}
