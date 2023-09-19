package tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import abstractinterp.scalar.state.Interval32Box;
import soot.Local;
import tadr.TADR;

public abstract class RewriteRule {

    protected static Logger LOGGER = LoggerFactory.getLogger(RewriteRule.class);

    public abstract boolean canRewrite(TADR expr);

    public static RewriteRule compose(RewriteRule fst, RewriteRule snd) {
        return and(fst, snd);
    }

    public static RewriteRule and(RewriteRule fst, RewriteRule snd) {
        return new RewriteRule() {
            public boolean canRewrite(TADR expr) {
                return fst.canRewrite(expr) && snd.canRewrite(expr);
            }

            public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
                return snd.rewrite(fst.rewrite(expr, lookup), lookup);
            }

            @Override
            public String toString() {
                return String.format("%s ∧ %s", fst, snd);
            }
        };
    }

    public static RewriteRule or(RewriteRule fst, RewriteRule snd) {
        return new RewriteRule() {
            public boolean canRewrite(TADR expr) {
                return fst.canRewrite(expr) || snd.canRewrite(expr);
            }

            public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
                return Stream.concat(
                    fst.canRewrite(expr) ? fst.rewrite(expr, lookup) : Stream.of(),
                    snd.canRewrite(expr) ? snd.rewrite(expr, lookup) : Stream.of());
            }

            @Override
            public String toString() {
                return String.format("%s ∨ %s", fst, snd);
            }
        };
    }

    public Stream<TADR> rewrite(Stream<TADR> exprs, Function<Local, Interval32Box> lookup) {
        return exprs.filter(this::canRewrite).flatMap(expr -> rewrite(expr, lookup));
    }

    public abstract Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup);
}
