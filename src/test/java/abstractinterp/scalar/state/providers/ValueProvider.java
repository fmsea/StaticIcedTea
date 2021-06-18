package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import soot.Type;
import soot.IntType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ValueProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Value.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return Collections.singleton(provideValue());
    }

    public static Arbitrary<Value> provideValue() {
        Arbitrary<String> labels = Arbitraries.strings()
                .withChars('a', 'l', 'b', 'i', 'c', '0', '1', '2', '3', '4').ofLength(2);
        Arbitrary<Integer> vals = Arbitraries.integers().between(Integer.MIN_VALUE, Integer.MAX_VALUE);
        Arbitrary<Value> locals = Combinators.combine(labels, vals).as((l, v) -> {
                Type t = IntType.v();
                t.setNumber(v);
                return Jimple.v().newLocal(l, t);
            });
        Arbitrary<Value> values = Arbitraries.frequencyOf(Tuple.of(1, vals.flatMap(x -> Arbitraries.just(IntConstant.v(x)))));
        return Arbitraries.frequencyOf(Tuple.of(1, values),
                                       Tuple.of(1, locals));
    }
}
