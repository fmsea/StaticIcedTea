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

public class EqSmtExpression extends BinopSmtExpression {

    public EqSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public Value getValue() {
        return Grimp.v().newEqExpr(this.left.getValue(), this.right.getValue());
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
        leftLocals.stream().forEach(l -> {
                reachableVariables.merge(l, rightLocals, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        rightLocals.stream().forEach(r -> {
                reachableVariables.merge(r, leftLocals, (a, b) -> {
                        a.addAll(b);
                        return a;
                    });
            });
        return reachableVariables;
    }
}
