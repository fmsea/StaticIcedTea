package driver;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import abstractinterp.scalar.IntegerAnalysis;
import abstractinterp.scalar.state.State;
import abstractinterp.scalar.state.factory.StateFactory;
import driver.util.OrdererFactory;
import driver.util.SootInitialization;
import solver.SolverFactory;
import util.AnalysisTimer;
import util.Configuration;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.Orderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AnalysisRunner<S extends State>  implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisRunner.class);
    private final String className;
    private final int methodId;
    private final Path outputResultsPath;
    private final SootMethod sootMethod;
    private final Body body;
    private final IntegerAnalysis<S> analysis;
    private final boolean outputStateReports;
    private final Set<Integer> widenSteps;

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory) {
        this(className, methodId, outputResultsPath, factory, true);
    }

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory,
                          boolean outputStateReports) {
        this(className, methodId, outputResultsPath, factory, outputStateReports, 2);
    }

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory,
                          boolean outputStateReports,
                          int widenIterations) {
        this(className, methodId, outputResultsPath, factory, outputStateReports, widenIterations, Set.of());
    }

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory,
                          boolean outputStateReports,
                          int widenIterations,
                          Set<Integer> widenSteps) {
        this(className,
             methodId,
             outputResultsPath,
             factory,
             outputStateReports,
             widenIterations,
             widenSteps,
             OrdererFactory.pseudoTopological());
    }

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory,
                          boolean outputStateReports,
                          int widenIterations,
                          Set<Integer> widenSteps,
                          Orderer<Unit> orderer) {
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.widenSteps = widenSteps == null ? Set.of() : widenSteps;
        this.analysis = new IntegerAnalysis<>(SolverFactory.getSolver(),
                                              this.body,
                                              widenIterations,
                                              factory,
                                              this.widenSteps,
                                              orderer);
        this.outputStateReports = outputStateReports;
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
