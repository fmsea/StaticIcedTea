package dev.fmsea.processing.smt;

import java.util.Set;

import soot.Local;

public class LtSmtExpression extends BinopSmtExpression {

    public LtSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
    }

    public String toSmt2() {
        return String.format("(< %s %s)",
                             this.left.toSmt2(),
                             this.right.toSmt2());
    }

    @Override
    public SmtGraph toGraph() {
        SmtGraph graph = super.toGraph();
        Set<Local> leftLocals = this.left.getLocals();
        Set<Local> rightLocals = this.right.getLocals();
        leftLocals.forEach(l -> {
                graph.addEdge(l, l, this);
                rightLocals.forEach(r -> graph.addEdge(l, r, this));
            });
        rightLocals.forEach(r -> graph.addEdge(r, r, this));
        return graph;
    }
}
