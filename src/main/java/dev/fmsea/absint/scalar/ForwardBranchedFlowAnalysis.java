package dev.fmsea.absint.scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.Trap;
import soot.Unit;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;

public abstract class ForwardBranchedFlowAnalysis<N extends Unit, A>
        extends ForwardBranchedFlowBasic<N, A> {

    public ForwardBranchedFlowAnalysis(DirectedGraph<N> graph) {
        super(graph);
        // populate order
        unitToAfterFallFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        unitToAfterBranchFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        unitToBeforeFlow = new HashMap<>(graph.size() * 2 + 1, 0.7f);
        order = new PseudoTopologicalOrderer<N>().newList(graph, false);

    }

    @Override
    protected void doAnalysis() {
        // initialize the flows as done in the original implementation
        for (N node : order) {
            unitToBeforeFlow.put(node, newInitialFlow());
            List<A> f = new ArrayList<>();
            unitToAfterFallFlow.put(node, f);
            if (node.fallsThrough()) {
                f.add(newInitialFlow());
            }
            f = new ArrayList<A>();
            unitToAfterBranchFlow.put(node, f);
            if (node.branches()) {
                for (int i = 0; i < node.getUnitBoxes().size(); i++) {
                    A v = newInitialFlow();
                    f.add(v);
                }
            }
            // entry points
            for (N head : graph.getHeads()) {
                unitToBeforeFlow.put(head, entryInitialFlow());
            }

            // traps are treated as entry points
            if (treatTrapHandlersAsEntries()) {
                for (Trap trap : ((UnitGraph) graph).getBody().getTraps()) {
                    Unit hanlder = trap.getHandlerUnit();
                    unitToBeforeFlow.put((N) hanlder, entryInitialFlow());
                }
            }

        }
        super.doAnalysis();
    }

    protected boolean isForward() {
        return true;
    }

    /**
     * Returns the initial flow value for entry/exit graph nodes.
     *
     * This is equal to {@link #newInitialFlow()}
     */
    protected A entryInitialFlow() {
        return newInitialFlow();
    }

    /**
     * Determines whether <code>entryInitialFlow()</code> is applied to trap handlers.
     */
    protected boolean treatTrapHandlersAsEntries() {
        return false;
    }

    @Override
    protected void mergeFlows(N node, A beforeFlow, List<A> preds) {
        basicMergeFlows(node, beforeFlow, preds);
    }

}
