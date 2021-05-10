package abstractinterp.scalar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.util.Chain;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.LoopNestTree;
import soot.toolkits.graph.PseudoTopologicalOrderer;
import soot.toolkits.graph.UnitGraph;
import util.Variables;

import abstractinterp.scalar.state.IntervalBoxState;
import abstractinterp.scalar.state.Interval32Box;

public class IntervalNumerical {
    protected Body b;
    UnitGraph g;
    ForwardBranchedFlowIntervalNumericalBox analysis;

    public IntervalNumerical(Body b, int iterations) {
        this.b = b;
        this.g = new ExceptionalUnitGraph(b);
        // init the analysis
        // the order
        List<Unit> order = new PseudoTopologicalOrderer<Unit>().newList(g, false);
        Map<Unit, IntervalBoxState> unitToBeforeFlow = new HashMap<Unit, IntervalBoxState>();
        Map<Unit, List<IntervalBoxState>> unitToAfterBranchFlow = new HashMap<Unit, List<IntervalBoxState>>();
        Map<Unit, List<IntervalBoxState>> unitToAfterFallFlow = new HashMap<Unit, List<IntervalBoxState>>();
        Set<Unit> wideningNode = new HashSet<Unit>();
        Set<Local> locals = new HashSet<Local>();
        for (Local l : b.getLocals()) {
            locals.add(l);
        }
        // find the head of the loops
        LoopNestTree loopTree = new LoopNestTree(b);
        // can be also used to do the order
        Iterator<Loop> lit = loopTree.descendingIterator();
        while (lit.hasNext()) {
            wideningNode.add(lit.next().getHead());
        }
        analysis = new ForwardBranchedFlowIntervalNumericalBox(g, order, unitToBeforeFlow, unitToAfterBranchFlow,
                unitToAfterFallFlow, wideningNode, iterations, locals);
        // set up the flows

        for (Unit node : order) {
            unitToBeforeFlow.put(node, analysis.newInitialFlow());
            List<IntervalBoxState> f = new ArrayList<IntervalBoxState>();
            unitToAfterFallFlow.put(node, f);
            if (node.fallsThrough()) {
                f.add(analysis.newInitialFlow());
            }
            f = new ArrayList<IntervalBoxState>();
            unitToAfterBranchFlow.put(node, f);
            if (node.branches()) {
                for (int i = 0; i < node.getUnitBoxes().size(); i++) {
                    IntervalBoxState v = analysis.newInitialFlow();
                    f.add(v);
                }
            }
        }
        // entry points
        // for(Unit head : g.getHeads()){
        // System.out.println("head " + head);
        // unitToBeforeFlow.put(head, analysis.entryInitialFlow());
        // }

        // traps are treated as entry points
        if (analysis.treatTrapHandlersAsEntries()) {
            for (Trap trap : ((UnitGraph) g).getBody().getTraps()) {
                Unit hanlder = trap.getHandlerUnit();
                unitToBeforeFlow.put(hanlder, analysis.entryInitialFlow());
            }
        }

    }

    public void runAnalysis() {
        analysis.doAnalysis();

    }

    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        // for each line print out the state
        for (Unit u : g.getBody().getUnits()) {
            sb.append(u);
            sb.append(" ");
            sb.append(u.getClass());
            sb.append(" f->");
            sb.append(analysis.getFallFlowAfter(u));
            sb.append('\n');
            if (u.branches()) {
                sb.append(u);
                sb.append(" b->");
                sb.append(analysis.getBranchFlowAfter(u));
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    public String generateFullSMTFormula() {
        StringBuilder sb = new StringBuilder();
        String methodSignature = this.b.getMethod().getSignature();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            sb.append(stmtCount);
            sb.append(" ");
            sb.append(u);
            sb.append(":");
            sb.append(methodSignature);
            sb.append('\n');
            IntervalBoxState state = analysis.getFallFlowAfter(u);
            sb.append(state.toSMTFormula());
            List<IntervalBoxState> branches = analysis.getBranchFlowAfter(u);
            for (IntervalBoxState branch : branches) {
                sb.append(branch.toSMTFormula());
            }
        }
        return sb.toString();
    }

    public String generateSMTFormulaReport() {
        StringBuilder sb = new StringBuilder();
        String methodSignature = this.b.getMethod().getSignature();
        Set<Unit> outputStmt = this.analysis.getOutputStatements();
        Map<Unit, Set<Value>> changedVariables = this.analysis.getChangedVariables();
        Chain<Local> locals = this.b.getLocals();
        int stmtCount = 0;
        for (Unit u : this.g.getBody().getUnits()) {
            stmtCount++;
            if (outputStmt.contains(u)) {
                sb.append(stmtCount);
                sb.append(" ");
                sb.append(u);
                sb.append(":");
                sb.append(methodSignature);
                sb.append('\n');
                IntervalBoxState state = analysis.getFallFlowAfter(u);
                if (state.isFeasible()) {
                    for (Local l : locals) {
                        if (changedVariables.get(u).contains(l)) {
                            sb.append(l.toString());
                            sb.append("->");
                            Interval32Box box = state.getMap().get(l);
                            sb.append(box.toSMTFormula(l.toString()));
                            sb.append('\n');
                        }
                    }
                    List<IntervalBoxState> branches = analysis.getBranchFlowAfter(u);
                    if (!branches.isEmpty()) {
                        for (IntervalBoxState branch : branches) {
                            if (branch.isFeasible()) {
                                for (Local l : locals) {
                                    if (changedVariables.get(u).contains(l)) {
                                        sb.append(l.toString());
                                        sb.append("f->");
                                        Interval32Box box = state.getMap().get(l);
                                        sb.append(box.toSMTFormula(l.toString()));
                                        sb.append('\n');
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public void report() {
        System.out.print(generateReport());
    }

}
