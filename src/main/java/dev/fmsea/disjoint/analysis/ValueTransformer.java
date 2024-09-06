package dev.fmsea.disjoint.analysis;

import java.util.List;
import java.util.Map;

import dev.fmsea.disjoint.domain.Domain;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootClass;
import soot.toolkits.graph.ExceptionalUnitGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueTransformer extends BodyTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValueTransformer.class);

    List<Domain> domains;
    boolean symbolicOn;
    int methodId = 16;

    // later can pass the domain info there is no
    // need for it to be initialized here

    public ValueTransformer(List<Domain> setDomains, int methodId, boolean symbolicOn) {
        super();
        this.domains = setDomains;
        this.symbolicOn = symbolicOn;
        this.methodId = methodId;
    }

    @Override
    protected void internalTransform(Body b, String phaseName, Map options) {
        String methodName = b.getMethod().getName();
        if (b.getMethod().getDeclaringClass().getMethods().get(methodId).equals(b.getMethod())) {
            LOGGER.debug("method: {}", methodName);
            // if method's does not have a single local int variable
            // the skip it
            boolean hasIntLocals = false;
            for (Local l : b.getLocals()) {
                if (ValueAnalysis.isAnyIntType(l)) {
                    hasIntLocals = true;
                    break;// at least one local var is an int
                }
            }
            if (!methodName.equals("<clinit>") && hasIntLocals) {
                LOGGER.debug("{}\t{}\t{}",
                        b.getMethod().getDeclaringClass(),
                        b.getMethod().getDeclaringClass().getMethods().indexOf(b.getMethod()),
                        b.getMethod());
                LOGGER.info("analyzing {}", b.getMethod().getSignature());
                System.gc();
                ValueAnalysis va = new ValueAnalysis(new ExceptionalUnitGraph(b), domains, symbolicOn);
                LOGGER.info("done init {}", b.getMethod().getSignature());
                va.start();
                LOGGER.info("done fixed-point {}", b.getMethod().getSignature());
                va.report();
                LOGGER.info("done reporting {}", b.getMethod().getSignature());
            }
        }
    }
}
