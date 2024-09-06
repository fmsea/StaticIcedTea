package dev.fmsea.absint.scalar.state.update;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.absint.scalar.state.update.rules.OctagonUpdateRule;
import soot.Local;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.rewrite.OctagonRewriter;
import dev.fmsea.tadr.rewrite.Rewriter;
import dev.fmsea.util.Pair;

public abstract class OctagonUpdater {

    private final static Logger LOGGER = LoggerFactory.getLogger(OctagonUpdater.class);
    private final Set<OctagonUpdateRule> rules;
    private final Rewriter rewriter = new OctagonRewriter();

    protected OctagonUpdater(Set<OctagonUpdateRule> rules) {
        this.rules = rules;
    }

    public Stream<ConstraintThunk> update(TADR expr,
        Function<Local, Interval32Box> env,
        Function<Local, Pair<Integer, Integer>> indexer) {
        return rewriter.rewrite(expr, env)
            .peek(e -> LOGGER.trace("rewrote expr {} to {}", expr, e))
            .flatMap(e -> update(e, indexer))
            .peek(t -> LOGGER.trace("created thunk {}", t));
    }

    protected Stream<ConstraintThunk> update(Stream<TADR> exprs, Function<Local, Pair<Integer, Integer>> indexer) {
        return exprs.flatMap(expr -> update(expr, indexer));
    }

    protected Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer) {
        if (rules.stream().noneMatch(r -> r.canUpdate(expr))) {
            LOGGER.error("An expression was not matched to a rule... {}", expr);
        }
        return rules
            .stream()
            .filter(r -> r.canUpdate(expr))
            .peek(r -> LOGGER.trace("Translating {} into a thunk using {}", expr, r))
            .flatMap(r -> r.update(expr, indexer));
    }
}
