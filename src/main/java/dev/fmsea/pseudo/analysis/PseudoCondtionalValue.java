package dev.fmsea.pseudo.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.fmsea.disjoint.domain.Domain;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

public class PseudoCondtionalValue extends BodyTransformer {
    List<Domain> domains;
    boolean symbolicOn;
    Map<IfStmt, Boolean> include;
    String[] condition;
    int methodId;
    String conditionsStr;

    /**
     * Initializes the analysis with the parameters.
     * 
     * @param domain     - abstract domain
     * @param symbolicOn - will be always yes for now
     * @param conditions - the set of branches to be excluded.
     * @param methodId
     */
    public PseudoCondtionalValue(List<Domain> domains, boolean symbolicOn, String conditions, String methodId) {
        super();
        this.domains = domains;
        this.symbolicOn = symbolicOn;
        this.condition = conditions.split(",");
        this.include = new HashMap<IfStmt, Boolean>();
        this.methodId = Integer.parseInt(methodId);
    }

    @Override
    protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
        // get the method body

        // simple filtering of a relevant method for tcas.
        // if(methodName.equals("getNextBits")){
        if (b.getMethod().getDeclaringClass().getMethods().get(methodId).equals(b.getMethod())) {
            String methodName = b.getMethod().getName();
            System.out.println("M " + methodName);
            // construct dot
            CFGToDotGraph cfgToDot = new CFGToDotGraph();
            DotGraph dotGraph = cfgToDot.drawCFG(new ExceptionalUnitGraph(b), b);
            dotGraph.plot("main2.dot");
            // //get BFS nodes order?
            int countOfCond = 0;
            for (Unit u : b.getUnits()) {
                // check if u is a conditional statement
                if (u instanceof IfStmt) {
                    countOfCond++;
                    // System.out.println(countOfCond +"\t" + u);
                    // check if this condition should be excluded
                    // split on t or f
                    for (String c : condition) {
                        if (c.endsWith("f")) {
                            String id = c.split("f")[0];
                            if (id.equals(Integer.toString(countOfCond))) {
                                // add to the map if ids match
                                include.put((IfStmt) u, false);
                            }
                        } else if (c.endsWith("t")) {
                            String id = c.split("t")[0];
                            if (id.equals(Integer.toString(countOfCond))) {
                                // add to the map if ids match
                                include.put((IfStmt) u, true);
                            }
                        } else {
                            if (!c.isEmpty()) {
                                System.out.println("unknown codition " + c);
                            }
                        }
                    }

                }

            }
            System.out.println("Exclude map " + include);
            // start the analysis
            PseudoConditionalValueAnalysis pa = new PseudoConditionalValueAnalysis(new ExceptionalUnitGraph(b), domains,
                    include);
            pa.start();
            pa.report();
        }

    }

}
