package processing.smt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;

import solver.SolverWrapper;
import solver.SolverFactory;

public class GtSmtExpression extends BinopSmtExpression {

    public GtSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newGtExpr(this.left.getValue(), this.right.getValue());
    }

    public Map<Local, Set<Local>> getReachableVariables() {
        Map<Local, Set<Local>> reachableVariables = new HashMap<>();
        Set<Local> leftLocals = this.left.getLocals();
        Set<Local> rightLocals = this.right.getLocals();
        leftLocals.forEach(l -> {
                Set<Local> reachable = new HashSet<>();
                reachable.add(l);
                reachableVariables.put(l, reachable);
            });
        rightLocals.forEach(r -> {
                Set<Local> reachable = new HashSet<>();
                reachable.add(r);
                reachableVariables.put(r, reachable);
            });
        rightLocals.stream().forEach(l -> {
                reachableVariables.merge(l, leftLocals, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        return reachableVariables;
    }
}
