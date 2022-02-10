package driver;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import abstractinterp.scalar.IntegerAnalysis;
import abstractinterp.scalar.state.MaxDifferenceBoundedState;
import abstractinterp.scalar.state.factory.MaxDifferenceBoundedStateFactory;
import driver.util.SootInitialization;
import processing.Smt2Reader;
import processing.util.FlowSet;
import util.AnalysisTimer;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxDBSAnalysisRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaxDBSAnalysisRunner.class);

    private final String className;
    private final int methodId;
    private final Path outputResultsPath;
    private final SootMethod sootMethod;
    private final Body body;
    private final IntegerAnalysis<MaxDifferenceBoundedState> analysis;
    private final boolean fullReport;

    public MaxDBSAnalysisRunner(String className,
                                   int methodId,
                                   Path outputResultsPath,
                                   boolean fullReport) {
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.fullReport = fullReport;
        this.analysis = new IntegerAnalysis<>(this.body, 2, new MaxDifferenceBoundedStateFactory());
    }

    public void run() {
        LOGGER.info("Analyzing Method {} in {}", this.sootMethod.getName(), this.className);
        LOGGER.debug("Soot Method Body\n:{}", this.body);
        AnalysisTimer.time((s) -> analysis.runAnalysis());
        if (this.fullReport) {
            AnalysisTimer.time((s) -> {
                    System.out.println(analysis.generateSMTReportFull());
                        },
                "reporting smt took {} ms");
        } else {
            analysis.report();
        }
    }
}
