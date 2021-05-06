package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import soot.Local;
import soot.jimple.Jimple;
import soot.Type;
import soot.IntType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocalProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Local.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return Collections.singleton(provideLocal());
    }

    public static Arbitrary<Local> provideLocal() {
        Arbitrary<String> labels = Arbitraries.strings()
                .withChars('a', 'l', 'b', 'i', 'c', '0', '1', '2', '3', '4').ofLength(2);
        Arbitrary<Integer> vals = Arbitraries.integers().between(Integer.MIN_VALUE, Integer.MAX_VALUE);
        return Combinators.combine(labels, vals).as((l, v) -> {
                Type t = IntType.v();
                t.setNumber(v);
                return Jimple.v().newLocal(l, t);
            });
    }

    public static Set<Local> generateLocals(int size) {
        Set<Local> locals = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            locals.add(generateLocal("l" + i));
        }
        return locals;
    }

    public static Local generateLocal(String name) {
        return Jimple.v().newLocal(name, IntType.v());
    }
}
