package dev.fmsea.processing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.ConstraintType;
import dev.fmsea.absint.scalar.state.SimpleOctagonState;
import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.ZonotopalInferenceParser;
import dev.fmsea.inference.rewrite.DefaultRewriter;
import dev.fmsea.inference.rewrite.Rewriter;
import dev.fmsea.inference.updates.TadrRewriter;
import dev.fmsea.inference.visitors.ExtractLocalsVisitor;
import dev.fmsea.util.Sets;
import soot.Local;

public class InferDomains {
    private static final Logger LOG = LoggerFactory.getLogger(InferDomains.class);
    private static final Pattern PRGM_SEP = Pattern.compile("([\\d]+(\t| {4})\\w+.\\w+)(\t| {4})(?<expressions>.*)?");
    private static final Pattern EXPR_SEP = Pattern.compile("\t| {4}");
    private static Rewriter rewriter = new DefaultRewriter();

    public static void inferDomains(BufferedReader reader, BufferedWriter writer) {
        reader.lines()
            .forEach(line -> processLine(line, writer));
    }

    private static void processLine(String line, BufferedWriter writer) {
        try {
            LOG.debug("Processing line: {}", line);
            writer.write(line);
            writer.write("\n");
            Matcher matcher = PRGM_SEP.matcher(line);
            if (!matcher.matches()) {
                return;
            }
            String prgm_exprs = matcher.group("expressions");
            if (prgm_exprs == null) { return; }
            LOG.trace("Parsing Program exprs: {}", prgm_exprs);
            Set<InvariantExpression> invs = parseInvariants(prgm_exprs)
                .stream()
                .flatMap(inv -> rewriter.rewrite(inv))
                .collect(Collectors.toSet());
            SimpleOctagonState octag = from(invs);
            Map<ConstraintType, Set<Local>> types = octag.queryConstraintTypes();
            LOG.trace("output map: {}", types);

            types.entrySet()
                .stream()
                .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                .forEach(kv -> kv.getValue()
                    .stream()
                    .map(Local::toString)
                    .sorted()
                    .forEach(local -> {
                try {
                    writer.write(String.format("%s\t%s\n",
                        local,
                        kv.getKey().toString()));
                } catch (IOException ex) {
                    LOG.error("Encountered an IO error while processing the octagon: {}", ex.getMessage());
                }
            }));

            writer.flush();
        } catch (IOException ex) {
            LOG.error("Encountered an IO error while processing line, {}: {}", line, ex.getMessage());
            LOG.trace(Stream.of(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n")));
        }
    }

    public static Set<InvariantExpression> parseInvariants(String expressions) {
        if (expressions.length() == 0) {
            return Set.of();
        }
        return EXPR_SEP.splitAsStream(expressions)
            .peek(expr -> LOG.trace("parsing expression {}", expr))
            .map(expr -> ZonotopalInferenceParser.parseExpr(expr))
            .collect(Collectors.toSet());
    }

    public static SimpleOctagonState from(Set<InvariantExpression> expressions) {
        Set<Local> locals = expressions.stream()
            .map(expr -> expr.accept(new ExtractLocalsVisitor()))
            .reduce(Sets::union)
            .orElse(Set.of());

        SimpleOctagonState state = new SimpleOctagonState(locals, true);

        expressions.stream()
            .peek(expr -> LOG.trace("rewriting {} to tadr", expr))
            .map(expr -> expr.accept(new TadrRewriter()))
            .peek(tadr -> LOG.trace("rewrote invariant {}", tadr))
            .forEach(tadr -> state.update(tadr, state));

        state.reduce();

        return state;
    }
}
