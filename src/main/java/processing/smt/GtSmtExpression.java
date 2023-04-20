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

    public String toSmt2() {
        return String.format("(> %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }

    public SmtGraph toGraph() {
        SmtGraph graph = super.toGraph();
        Set<Local> leftLocals = this.left.getLocals();
        Set<Local> rightLocals = this.right.getLocals();
        leftLocals.forEach(l -> graph.addEdge(l, l, this));
        rightLocals.forEach(r -> {
                graph.addEdge(r, r, this);
                leftLocals.forEach(l -> graph.addEdge(r, l, this));
            });
        return graph;
    }
}
