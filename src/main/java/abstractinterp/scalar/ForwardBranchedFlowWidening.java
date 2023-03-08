package abstractinterp.scalar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.Optional;

import soot.Unit;
import soot.toolkits.graph.DirectedGraph;

import abstractinterp.scalar.state.State;
import abstractinterp.scalar.util.Pair;
/**
 * Widening also asks about after how many
 * iterations apply widening and also a list
 * of widening nodes
 * @author elenasherman
 *
 * @param <N>
 * @param <A>
 */
public abstract class ForwardBranchedFlowWidening<N extends Unit, A extends State>
    extends ForwardBranchedFlowBasic<N, A> {

    // This number should be negative.
    protected final int MAX_WIDENING_ITERATIONS = -10;

    Set<N> wideningNodes;
    /**
     * Count of merging performed by a widening node
     */
    private final int defaultIterationCount;
    Map<N, Pair<Integer, TreeSet<Integer>>> itersCount;


    public ForwardBranchedFlowWidening(DirectedGraph<N> graph,
                                       List<N> order,
                                       Map<N, A> unitToBeforeFlow,
                                       Map<N, List<A>> unitToAfterBranchFlow,
                                       Map<N, List<A>> unitToAfterFallFlow,
                                       Set<N> wideningNodes,
                                       Set<Integer> wideningSteps,
                                       int iters) {
        super(graph);
        this.order = order;
        this.unitToAfterBranchFlow = unitToAfterBranchFlow;
        this.unitToAfterFallFlow = unitToAfterFallFlow;
        this.unitToBeforeFlow = unitToBeforeFlow;
        this.wideningNodes = wideningNodes;
        this.defaultIterationCount = iters + 1;
        this.itersCount = new HashMap<>();
        for (N n : wideningNodes) {
            TreeSet<Integer> steps = new TreeSet<>();
            wideningSteps.forEach(s -> steps.add(s));
            itersCount.put(n, Pair.of(this.defaultIterationCount, steps));
        }
    }

    @Override
    protected void mergeFlows(N node, A beforeFlow, List<A> preds){

        if (wideningNodes.contains(node)) {
            //copy beforeFlow
            A prevBeforeFlow = newInitialFlow(); //initFlows
            copy(beforeFlow, prevBeforeFlow);
            //do your own merge done in a particular order
            // if you want or just call super
            //x_in is the first element of preds list
            //x_back are the rest one in the list
            basicMergeFlows(node, beforeFlow, preds);
            //if different
            LOGGER.trace("previous before flow: {}", prevBeforeFlow);
            LOGGER.trace("current before flow: {}", beforeFlow);
            if (!prevBeforeFlow.equals(beforeFlow)) {
                //check the count
                Pair<Integer, TreeSet<Integer>> iterSteps = this.itersCount.get(node);
                int mergeCounts = iterSteps.fst();
                TreeSet<Integer> steps = iterSteps.snd();
                if (mergeCounts < MAX_WIDENING_ITERATIONS) {
                    throw new RuntimeException(String.format("Widening did not work [iterations=%d, node=%s]",
                                                             mergeCounts * -1,
                                                             node));
                } else if (mergeCounts <= 0) {
                    if (steps.isEmpty()) {
                        widen(prevBeforeFlow, beforeFlow);
                    } else {
                        Integer step = steps.first();
                        steps.remove(step);
                        widen(prevBeforeFlow, beforeFlow, Optional.of(step));
                    }
                }
                mergeCounts--;
                itersCount.put(node, Pair.of(mergeCounts, steps));
            }

        } else {
            //if node is not a widening node call regular merge
            basicMergeFlows(node, beforeFlow, preds);
        }
    }

    /**
     * widens beforeFlow with prevBeforeFlow and writes
     * the results back to prevBeforeFlow
     * Widening definition
     * m_ij ▽ n_ij = { m_ij if n_ij ≤ m_ij else +∞ }
     * where `prevBeforeFlow` is `m` and `beforeFlow` is `n`.
     *
     * http://dx.doi.org/10.1007/3-540-44978-7_10
     *
     * @param beforeFlow
     * @param prevBefore
     * @return widened flow
     */
    protected abstract void widen(A prevBeforeFlow, A beforeFlow);

    /**
     * widens beforeFlow with prevBeforeFlow using an optional stepping.
     *
     *
     * @param prevBeforeFlow
     * @param beforeFlow
     * @param step
     */
    protected abstract void widen(A prevBeforeFlow, A beforeFlow, Optional<Integer> step);

}
