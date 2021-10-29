package driver;

import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import disjoint.analysis.ValueAnalysis;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import driver.util.SootInitialization;
import util.AnalysisTimer;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.graph.ExceptionalUnitGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PredicateAnalysisRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PredicateAnalysisRunner.class);

    private final String className;
    private final int methodId;
    private final Path classpath;
    private final Path outputResultsPath;
    private final File domainFile;
    private final boolean symbolic;
    private final SootMethod sootMethod;
    private final Body body;
    private final ValueAnalysis analysis;
    private final boolean fullReport;

    public PredicateAnalysisRunner(String className,
                                   int methodId,
                                   Path classpath,
                                   Path outputResultsPath,
                                   File domainFile,
                                   boolean symbolic,
                                   boolean fullReport) {
        this.className = className;
        this.methodId = methodId;
        this.classpath = classpath;
        this.outputResultsPath = outputResultsPath;
        this.domainFile = domainFile;
        this.symbolic = symbolic;
        this.sootMethod = SootInitialization.initializeSoot(className,
                                                            methodId,
                                                            classpath.toAbsolutePath().toString());
        this.body = this.sootMethod.retrieveActiveBody();
        this.fullReport = fullReport;
        LOGGER.debug("Reading {} for domains", domainFile);
        DomainReader domainReader = new DomainReader(domainFile.toString());
        List<Domain> domains = domainReader.getReadDomains();
        LOGGER.info("Domains provided:\n{}", domains);
        this.analysis = new ValueAnalysis(new ExceptionalUnitGraph(this.body), domains, symbolic);
    }

    public void run() {
        LOGGER.info("Analyzing Method {} in {}", this.sootMethod.getName(), this.className);
        LOGGER.debug("Soot Method Body\n:{}", this.body);
        AnalysisTimer.time((s) -> analysis.start());
        if (this.fullReport) {
            analysis.reportFullSMT();
        } else {
            analysis.report();
        }
    }
}
