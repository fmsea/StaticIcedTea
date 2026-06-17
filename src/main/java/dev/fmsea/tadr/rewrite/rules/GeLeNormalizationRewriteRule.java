package dev.fmsea.tadr.rewrite.rules;

import java.util.function.Function;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Interval32Box;
import dev.fmsea.tadr.BinaryOp;
import dev.fmsea.tadr.GeCmp;
import dev.fmsea.tadr.LeCmp;
import dev.fmsea.tadr.TADR;
import dev.fmsea.tadr.Value;
import dev.fmsea.tadr.Variable;
import soot.Local;

public class GeLeNormalizationRewriteRule extends RewriteRule {

    public boolean canRewrite(TADR expr) {
        if (expr instanceof GeCmp || expr instanceof LeCmp) {
            BinaryOp cmp = (BinaryOp)expr;
            return (cmp.left instanceof Value && cmp.right instanceof Variable);
        }
        return false;
    }

    public Stream<TADR> rewrite(TADR expr, Function<Local, Interval32Box> lookup) {
        if (expr instanceof GeCmp) {
            return rewrite((GeCmp)expr, lookup);
        } else if (expr instanceof LeCmp) {
            return rewrite((LeCmp)expr, lookup);
        } else {
            return Stream.of();
        }
    }

    public Stream<TADR> rewrite(GeCmp expr, Function<Local, Interval32Box> lookup) {
        Value left = (Value)expr.left;
        Variable right = (Variable)expr.right;
        return Stream.of(TADR.newLeExpr(right, left));
    }

    public Stream<TADR> rewrite(LeCmp expr, Function<Local, Interval32Box> lookup) {
        Value left = (Value)expr.left;
        Variable right = (Variable)expr.right;
        return Stream.of(TADR.newGeExpr(right, left));
    }

    @Override
    public String toString() {
        return "Interval normalization";
    }
}
