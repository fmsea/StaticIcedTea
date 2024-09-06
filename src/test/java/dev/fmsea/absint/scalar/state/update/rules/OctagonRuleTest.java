package dev.fmsea.absint.scalar.state.update.rules;

import java.util.Map;

import dev.fmsea.common.Locals;
import dev.fmsea.util.Pair;
import soot.Local;

public abstract class OctagonRuleTest {

    protected static Pair<Integer, Integer> lookup(Local l) {
        Map<Local, Pair<Integer, Integer>> locals =
            Map.of(Locals.get("x1"), Pair.of(0, 1),
                Locals.get("x2"), Pair.of(2, 3),
                Locals.get("x3"), Pair.of(4, 5),
                Locals.get("x4"), Pair.of(6, 7));
        return locals.getOrDefault(l, Pair.of(-1, -1));
    }
}
