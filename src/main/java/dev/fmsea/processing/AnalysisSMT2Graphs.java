package dev.fmsea.processing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.processing.smt.SmtExpression;
import dev.fmsea.processing.smt.SmtExpressionReader;
import soot.Local;

public class AnalysisSMT2Graphs {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisSMT2Graphs.class);

    public static void exprsToGraphs(Reader reader,
                                     Path outputPrefix) throws IOException {
        AnalysisSMTReport report = Smt2Reader.parse(reader);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        for (String statement : report.statements()) {
            Optional<SmtExpression> f = report.getFallThrough(statement)
                .map(SmtExpressionReader::parse);
            Optional<SmtExpression> b = report.getBranchOut(statement)
                .map(SmtExpressionReader::parse);

            Set<Local> fallDeltaV = report.getFallChangedVariables(statement).orElse(Set.of());
            Set<Local> branchDeltaV = report.getBranchChangedVariables(statement).orElse(Set.of());
            f.ifPresent(expr -> {
                    executor.submit(new Runnable() {
                            @Override public void run() {
                                exprToGraph(expr,
                                            fallDeltaV,
                                            Path.of(outputPrefix.toString(),
                                                    String.format("%s-fall.dot",
                                                                  statement.substring(0, 15))));
                            }});
                });
            b.ifPresent(expr -> {
                    executor.submit(new Runnable() {
                            @Override public void run() {
                                exprToGraph(expr,
                                            branchDeltaV,
                                            Path.of(outputPrefix.toString(),
                                                    String.format("%s-branch.dot",
                                                                  statement.substring(0, 15))));
                            }});
                });

        }

        try {
            executor.shutdown();
            if (!executor.awaitTermination(600, TimeUnit.SECONDS)) {
                LOGGER.error("Failed to finish converting graphs within allotted time");
            }
        } catch (InterruptedException ex) {
            LOGGER.error("executor interrupted: {}", ex);
        }
    }

    public static void exprToGraph(SmtExpression expr, Set<Local> deltaV, Path outputFile) {
        try (Writer writer = new FileWriter(outputFile.toFile());
             BufferedWriter buf = new BufferedWriter(writer)) {
            expr.toGraph().toDot(deltaV, buf);
            buf.flush();
        } catch (IOException ex) {
            LOGGER.error("Unable to convert expression={} to graph", expr);
            LOGGER.trace(Stream.of(ex.getStackTrace())
                         .map(StackTraceElement::toString)
                         .collect(Collectors.joining("\n")));
        }
    }
}
