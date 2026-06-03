package dev.fmsea.inference.rewrite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.inference.InvariantExpression;
import dev.fmsea.inference.rewrite.rules.RewriteRule;

public abstract class Rewriter {
    protected static final Logger LOG = LoggerFactory.getLogger(Rewriter.class);
    private static final int REWRITE_DEPTH = 10;
    private final List<RewriteRule> rules;

    public Rewriter(Collection<RewriteRule> rules) {
        List<RewriteRule> sortedRules = new ArrayList<>(rules);
        sortedRules.sort(Comparator.comparing(RewriteRule::rewritePhase)
            .thenComparingInt(RewriteRule::rewritePriority));
        this.rules = Collections.unmodifiableList(sortedRules);
    }

    public Stream<InvariantExpression> rewrite(InvariantExpression expr) {
        Set<InvariantExpression> canonicalForms = new HashSet<>();
        Queue<InvariantExpression> worklist = new LinkedList<>();
        worklist.add(expr);

        int count = 0;

        while (!worklist.isEmpty()) {
            InvariantExpression curr = worklist.poll();
            boolean wasRewritten = false;

            for (RewriteRule rule : rules) {
                try {
                    LOG.debug("Rewriting {} with {} [count={}]", curr, rule, count);
                    InvariantExpression rewritten = curr.accept(rule);
                    LOG.trace("rewritten != curr: {} != {}", rewritten, curr);
                    if (rewritten != curr) {
                        wasRewritten = true;
                        rewritten.asConjuncts().forEach(worklist::add);
                        break;
                    }
                } catch (StackOverflowError ex) {
                    LOG.error("Encountered stack overflow while rewriting {}: {}", curr, ex.getMessage());
                    LOG.trace("Stack Trace: {}",
                        Stream.of(ex.getStackTrace())
                            .map(StackTraceElement::toString)
                            .collect(Collectors.joining("\n")));
                }
            }

            if (!wasRewritten) {
                canonicalForms.add(curr);
            }
            count++;

            if (count > REWRITE_DEPTH) {
                LOG.error("Rewriting blew its stack limit");
                LOG.debug("Attempting to rewrite {} with current queue {}", expr, worklist);
                throw new RuntimeException("Rewriting blew its rewrite stack limit");
            }
        }

        return canonicalForms.stream();
    }
}
