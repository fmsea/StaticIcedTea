package dev.fmsea.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.IntegerAnalysis;
import dev.fmsea.absint.scalar.IntegerAnalysisBuilder;
import dev.fmsea.driver.util.OrdererFactory;
import dev.fmsea.driver.util.SootInitialization;
import dev.fmsea.solver.SolverFactory;
import dev.fmsea.util.AnalysisTimer;
import dev.fmsea.util.Configuration;
import soot.Body;
import soot.SootMethod;

public class AnalysisRunner  implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisRunner.class);
    protected final String className;
    protected final int methodId;
    protected final Path outputResultsPath;
    protected final SootMethod sootMethod;
    protected final Body body;
    protected final IntegerAnalysis analysis;
    protected final boolean outputStateReports;
    protected final boolean outputConstraintTypes;
    protected final Set<Integer> widenSteps;

    public AnalysisRunner(AnalysisOptions options) {
        this.className = options.className;
        this.methodId = options.methodId;
        this.outputResultsPath = options.outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.widenSteps = options.widenSteps.orElse(Set.of());
        this.outputConstraintTypes = options.outputConstraintTypes;
        this.outputStateReports = options.outputStateReports;
        this.analysis = new IntegerAnalysisBuilder()
            .withSolver(SolverFactory.getSolver())
            .withBody(this.body)
            .withIterations(options.widenIterations)
            .withWidenSteps(options.widenSteps)
            .withReducedOutput(options.reduceOutput)
            .withType(options.stateType)
            .withOrderer(OrdererFactory.get(options.orderer))
            .withTelemetry(options.telemetry)
            .withReportInflow(this.outputConstraintTypes)
            .build();
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

        if (this.outputConstraintTypes) {
            AnalysisTimer.time((s) -> {
                reportConstraintTypes();
            }, "constraint types report took {} ms");
        } else {
            LOGGER.info("Skipping constraint types report...");
        }
    }

    protected String reportBasename() {
        return String.format("%s_%d", this.className, this.methodId);
    }


    protected void report() {
        File fullSmt = Path.of(this.outputResultsPath.toString(),
            String.format("%s.smt.out", this.reportBasename())).toFile();
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

    protected void reportConstraintTypes() {
        File constraintTypes = Path.of(this.outputResultsPath.toString(),
            String.format("%s.constraint.types", this.reportBasename())).toFile();
        File dir = this.outputResultsPath.toFile();
        dir.mkdirs();

        try (FileWriter fw = new FileWriter(constraintTypes);
             BufferedWriter buf = new BufferedWriter(fw)) {
            this.analysis.reportConstraintTypes(buf);
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to write constraint type report to file: {}", ex.getMessage());
            LOGGER.trace("Stack trace: {}",
                Stream.of(ex.getStackTrace()).map(st -> st.toString()).collect(Collectors.joining("\n")));
        }
    }
}
