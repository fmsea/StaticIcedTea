package driver;

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
import driver.util.SootInitialization;
import util.AnalysisTimer;
import util.Configuration;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
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

    public AnalysisRunner(String className,
                          int methodId,
                          Path outputResultsPath,
                          StateFactory<S> factory) {
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.analysis = new IntegerAnalysis<>(this.body, 2, factory);
    }

    public void run() {
        LOGGER.info("Analyzing Method {} in {}", this.sootMethod.getName(), this.className);
        LOGGER.debug("Soot Method Body\n:{}", this.body);
        // Time Analysis
        AnalysisTimer.time((s) -> this.analysis.runAnalysis());
        // Time Reporting
        AnalysisTimer.time((s) -> {
                this.report();
            },
            "analysis report took {} ms");
    }

    protected void report() {
        File changed = Path.of(this.outputResultsPath.toString(),
                               String.format("%s_%d.changed.out", this.className, this.methodId)).toFile();
        File fullSmt = Path.of(this.outputResultsPath.toString(),
                               String.format("%s_%d.smt.out", this.className, this.methodId)).toFile();
        File dir = this.outputResultsPath.toFile();
        dir.mkdirs();

        try (FileWriter fw = new FileWriter(changed)) {
            fw.write(this.analysis.generateSMTReport());
            fw.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to write changed output file: {}", ex.getMessage());
        }

        try (FileWriter fw = new FileWriter(fullSmt)) {
            fw.write(this.analysis.generateSMTReportFull());
            fw.flush();
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
