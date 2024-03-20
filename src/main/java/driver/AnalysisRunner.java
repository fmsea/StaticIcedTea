package driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.IntegerAnalysis;
import abstractinterp.scalar.state.State;
import driver.util.OrdererFactory;
import driver.util.SootInitialization;
import solver.SolverFactory;
import soot.Body;
import soot.SootMethod;
import util.AnalysisTimer;
import util.Configuration;

public class AnalysisRunner<S extends State>  implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisRunner.class);
    private final String className;
    private final int methodId;
    private final Path outputResultsPath;
    private final SootMethod sootMethod;
    private final Body body;
    private final IntegerAnalysis<S> analysis;
    private final boolean outputStateReports;
    private final Set<Integer> widenSteps;

    public AnalysisRunner(AnalysisOptions options) {
        this.className = options.className;
        this.methodId = options.methodId;
        this.outputResultsPath = options.outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.widenSteps = options.widenSteps.orElse(Set.of());
        this.analysis = new IntegerAnalysis<>(
            SolverFactory.getSolver(),
            this.body,
            options.widenIterations,
            options.stateType,
            this.widenSteps,
            OrdererFactory.get(options.orderer));
        this.outputStateReports = options.outputStateReports;
    }

    public void run() {
        LOGGER.info("Analyzing Method {} in {}", this.sootMethod.getName(), this.className);
        LOGGER.debug("Soot Method Body\n:{}", this.body);
        // Time Analysis
        AnalysisTimer.time((s) -> this.analysis.runAnalysis());
        // Time Reporting
        if (this.outputStateReports) {
            AnalysisTimer.time((s) -> {
                    this.report();
                },
                "analysis report took {} ms");
        } else {
            LOGGER.info("Skipping report generation...");
        }
    }

    protected void report() {
        File fullSmt = Path.of(this.outputResultsPath.toString(),
                               String.format("%s_%d.smt.out", this.className, this.methodId)).toFile();
        File dir = this.outputResultsPath.toFile();
        dir.mkdirs();

        try (FileWriter fw = new FileWriter(fullSmt);
             BufferedWriter buf = new BufferedWriter(fw)) {
            this.analysis.writeReport(buf);
            buf.flush();
            fullSmt.setReadOnly();
        } catch (IOException ex) {
            LOGGER.error("Unable to write full SMT output file: {}", ex.getMessage());
        }

        Configuration.getEnvBoolean("DFA_EXPORT_GRAPH_STATES").ifPresent(export -> {
                if (export) {
                    Path graphOutputDir = Path.of(this.outputResultsPath.toString(),
                                                  String.format("%s_%d",
                                                                this.className,
                                                                this.methodId));
                    this.analysis.generateGraphOutputs(graphOutputDir);
                }
            });
    }
}
