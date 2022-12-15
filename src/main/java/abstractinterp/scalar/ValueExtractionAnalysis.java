package abstractinterp.scalar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import abstractinterp.scalar.state.BinaryOperatorType;
import abstractinterp.scalar.state.PredicateType;
import abstractinterp.scalar.state.PredicateValuePair;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.Body;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.LongConstant;
import soot.jimple.NumericConstant;
import soot.jimple.internal.JNegExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValueExtractionAnalysis {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueExtractionAnalysis.class);
    protected Body b;
    UnitGraph g;
    ValueExtractionForwardBranchedFlowAnalysis analysis;

    public ValueExtractionAnalysis(Body b) {
        this.b = b;
        this.g = new ExceptionalUnitGraph(b);

        this.analysis = new ValueExtractionForwardBranchedFlowAnalysis(this.g);
    }

    public void runAnalysis() {
        analysis.doAnalysis();
    }

    public String reportPredicateSet() {
        return this.g.getBody().getUnits().stream()
            .flatMap(u -> this.analysis.getFallFlowAfter(u).stream())
            .collect(Collectors.toSet())
            .toString();
    }
}
