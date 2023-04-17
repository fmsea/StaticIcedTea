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

    public String toSmt2() {
        return String.format("(= %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }

    public SmtGraph toGraph() {
        SmtGraph graph = super.toGraph();
        Set<Local> leftLocals = this.left.getLocals();
        Set<Local> rightLocals = this.right.getLocals();
        leftLocals.forEach(l -> {
                graph.addEdge(l, l);
                rightLocals.forEach(r -> graph.addEdge(l, r));
            });
        rightLocals.forEach(r -> {
                graph.addEdge(r, r);
                leftLocals.forEach(l -> graph.addEdge(r, l));
            });
        return graph;
    }
}
