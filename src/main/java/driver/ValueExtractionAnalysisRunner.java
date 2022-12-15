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

import abstractinterp.scalar.ValueExtractionAnalysis;
import driver.util.SootInitialization;
import util.AnalysisTimer;
import util.Configuration;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueExtractionAnalysisRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueExtractionAnalysisRunner.class);
    private final String className;
    private final int methodId;
    private final Path outputResultsPath;
    private final SootMethod sootMethod;
    private final Body body;
    private final ValueExtractionAnalysis analysis;

    public ValueExtractionAnalysisRunner(String className,
                                         int methodId,
                                         Path outputResultsPath) {
        this.className = className;
        this.methodId = methodId;
        this.outputResultsPath = outputResultsPath;
        this.sootMethod = SootInitialization.getSootMethod(className, methodId);
        this.body = this.sootMethod.retrieveActiveBody();
        this.analysis = new ValueExtractionAnalysis(this.body);
    }

    public void run() {
        LOGGER.info("Extracting predicates from method {} in {}", this.sootMethod.getName(), this.className);
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
        File predicates = Path.of(this.outputResultsPath.toString(),
                               String.format("%s_%d.predicates.out", this.className, this.methodId)).toFile();
        File dir = this.outputResultsPath.toFile();
        dir.mkdirs();

        try (FileWriter fw = new FileWriter(predicates);
             BufferedWriter buf = new BufferedWriter(fw)) {
            buf.write(this.analysis.reportPredicateSet());
            buf.write("\n");
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to write changed output file: {}", ex.getMessage());
        }
    }
}
