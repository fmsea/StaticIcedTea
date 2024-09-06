package dev.fmsea.absint.scalar.state.update.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.fmsea.absint.scalar.state.ConstraintThunk;
import soot.Local;
import dev.fmsea.tadr.TADR;
import dev.fmsea.util.Pair;

public abstract class OctagonUpdateRule {

    protected static final Logger LOGGER = LoggerFactory.getLogger(OctagonUpdateRule.class);

    public abstract boolean canUpdate(TADR expr);

    public abstract Stream<ConstraintThunk> update(TADR expr, Function<Local, Pair<Integer, Integer>> indexer);

    protected abstract Stream<ConstraintThunk> makeUpdate(Integer bound, Pair<Integer, Integer> s, Pair<Integer, Integer> t);
}
