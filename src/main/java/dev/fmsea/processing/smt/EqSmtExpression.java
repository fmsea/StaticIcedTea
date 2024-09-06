package dev.fmsea.processing.smt;

import java.util.Set;

import soot.Local;

public class EqSmtExpression extends BinopSmtExpression {

    public EqSmtExpression(SmtExpression left, SmtExpression right) {
        super(left, right);
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
                graph.addEdge(l, l, this);
                rightLocals.forEach(r -> graph.addEdge(l, r, this));
            });
        rightLocals.forEach(r -> {
                graph.addEdge(r, r, this);
                leftLocals.forEach(l -> graph.addEdge(r, l, this));
            });
        return graph;
    }
}
