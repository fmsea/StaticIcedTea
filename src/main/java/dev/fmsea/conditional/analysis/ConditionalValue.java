package dev.fmsea.conditional.analysis;

import java.util.List;
import java.util.Map;

import dev.fmsea.conditional.scalar.ConditionalInfo;
import dev.fmsea.disjoint.domain.Domain;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class ConditionalValue extends BodyTransformer {

    List<Domain> domains;
    int methodId = 16;
    String condition = "";

    // later can pass the domain info there is no
    // need for it to be initialized here

    public ConditionalValue(List<Domain> setDomains, int methodId, String condition) {
        super();
        domains = setDomains;
        this.methodId = methodId;
        this.condition = condition;
    }

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        String methodName = b.getMethod().getName();
        if (b.getMethod().getDeclaringClass().getMethods().get(methodId).equals(b.getMethod())) {
            // System.out.println("method " + methodName);
            // if method's does not have a single local int variable
            // the skip it
            boolean hasIntLocals = false;
            for (Local l : b.getLocals()) {
                if (ConditionalValueAnalysis.isAnyIntType(l)) {
                    hasIntLocals = true;
                    break;// at least one local var is an int
                }
            }
            if (!methodName.equals("<clinit>") && hasIntLocals) {
                // System.out.println(b.getMethod().getDeclaringClass() + "\t" +
                // b.getMethod().getDeclaringClass().getMethods().indexOf(b.getMethod()) + "\t"
                // + b.getMethod());
                System.out.println("analyzing " + b.getMethod().getSignature());
                System.gc();
                // covert to Eric's format the actual line number of the conditional stmt
                // change for the format 5t19f instead of 1t3f, where 1 is the first cond stmt
                // for unit 5 and 3 is the third cond stmt for unit 19
                String[] branches = condition.split(",");
                String conditions = "";
                int countOfCond = 0;
                int countOfStmt = 1;
                for (Unit u : b.getUnits()) {
                    // System.out.println(countOfStmt + " " + u);
                    if (u instanceof IfStmt) {
                        countOfCond++;
                        for (String c : branches) {
                            if (c.endsWith("f")) {
                                String id = c.split("f")[0];
                                if (id.equals(Integer.toString(countOfCond))) {
                                    conditions += countOfStmt + "f";
                                    // System.out.println(u);
                                }
                            } else if (c.endsWith("t")) {
                                String id = c.split("t")[0];
                                if (id.equals(Integer.toString(countOfCond))) {
                                    conditions += countOfStmt + "t";
                                    // System.out.println(u);
                                }
                            } else {
                                if (!c.isEmpty()) {
                                    System.out.println("unknown codition " + c);
                                }
                            }
                        }
                    }
                    countOfStmt++;
                }
                ConditionalValueAnalysis va = new ConditionalValueAnalysis(new ExceptionalUnitGraph(b), domains,
                        ConditionalInfo.createFlowInfoList(conditions));
                System.out.println("done init " + b.getMethod().getSignature());
                va.start();
                System.out.println("done fixed-point " + b.getMethod().getSignature());
                va.report();
                System.out.println("done reporting " + b.getMethod().getSignature());
            }

        }
    }
}
