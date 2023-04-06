package driver.util;

import soot.Unit;
import soot.toolkits.graph.Orderer;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.SlowPseudoTopologicalOrderer;
import conditional.scalar.ConditionalTopologicalOrderer;

public class OrdererFactory {

    public static Orderer<Unit> pseudoTopological() {
        return new PseudoTopologicalOrderer<>();
    }

    public static Orderer<Unit> slowPseudoTopological() {
        return new SlowPseudoTopologicalOrderer<>();
    }

    public static Orderer<Unit> conditionalTopological() {
        return new ConditionalTopologicalOrderer<>();
    }

    public static Orderer<Unit> get(OrdererType type) {
        switch (type) {
        case ConditionalTopological:
            return conditionalTopological();
        case SlowPseudoTopological:
            return slowPseudoTopological();
        case PseudoTopological:
        default:
            return pseudoTopological();
        }
    }
}
