package driver;

import processing.MethodStatsAnalysis;
import driver.util.SootInitialization;
import util.AnalysisTimer;
import soot.Body;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodStatsRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodStatsRunner.class);

    private final String className;
    private final int methodId;
    private final SootMethod sootMethod;
    private final Body sootBody;
    private final MethodStatsAnalysis analysis;

    public MethodStatsRunner(String className, int methodId) {
        LOGGER.info("[className = {}, methodId = {}]", className, methodId);
        this.className = className;
        this.methodId = methodId;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.sootBody = this.sootMethod.retrieveActiveBody();
        this.analysis = new MethodStatsAnalysis(this.sootBody);
    }

    public void run() {
        LOGGER.info("Analyzing Method {} in {}", this.sootMethod.getName(), this.className);
        LOGGER.debug("Soot Method Body\n:{}", this.sootBody);
        AnalysisTimer.time((s) -> analysis.runAnalysis());
        System.out.println(analysis.report());
    }
}
