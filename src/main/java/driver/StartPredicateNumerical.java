package driver;

import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Scene;
import soot.toolkits.graph.ExceptionalUnitGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import disjoint.analysis.ValueAnalysis;
import disjoint.domain.Domain;
import disjoint.domain.reader.DomainReader;
import util.AnalysisTimer;


/**
 * Driver predicate/disjoint analysis
 * Make sure to have two folders in this path: invariants and time
 * args[0] is the class name
 * args[1] is the class path for class
 * args[2] is the method id in this class
 * args[3] is the domain file for the analysis
 * args[4] is whether to run symbolic analysis (if args[4] == "sY")
 *
 */
public class StartPredicateNumerical {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartPredicateNumerical.class);

    public static void main(String[] args) {
        String className;
        String classPath;
        Path domainFile;
        int methodId;
        boolean symbolic;
        if (args.length >= 5) {
            className = args[0];
            classPath = Paths.get(args[1]).toAbsolutePath().toString();
            methodId = Integer.parseInt(args[2]);
            domainFile = Paths.get(args[3]).toAbsolutePath();
            symbolic = "sY".equals(args[4]);
        } else {
            className = "test.Example1M";
            classPath = Paths.get("artifacts/").toAbsolutePath().toString();
            domainFile = Paths.get("ExperimentData/domains/dom3.txt").toAbsolutePath();
            methodId = 6;
            symbolic = true;
        }

        Scene.v().setSootClassPath(Scene.v().getSootClassPath() +
                                   File.pathSeparator +
                                   System.getProperty("java.class.path") +
                                   File.pathSeparator +
                                   classPath);

        SootClass sClass = Scene.v().loadClassAndSupport(className);
        sClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        SootMethod m = sClass.getMethods().get(methodId);

        Body b = m.retrieveActiveBody();

        LOGGER.info("Running disjoint analysis for {}_{} (domain={}, symbolic={})",
                    className,
                    methodId,
                    domainFile.getFileName().toString(),
                    symbolic);

        DomainReader domainReader = new DomainReader(domainFile.toString());
        List<Domain> domains = domainReader.getReadDomains();
        LOGGER.info("Domains provided:\n{}", domains);
        ValueAnalysis analysis = new ValueAnalysis(new ExceptionalUnitGraph(b), domains, symbolic);
        AnalysisTimer.time((s) -> analysis.start());
        analysis.report();
    }
}
