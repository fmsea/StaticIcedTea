package dev.fmsea.absint.scalar.state.providers;

import dev.fmsea.absint.scalar.state.PredicateType;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

public class PredicateTypeProvider implements ArbitraryProvider {

    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(PredicateType.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return Collections.singleton(Arbitraries.of(PredicateType.Eq,
                                                    PredicateType.Ne,
                                                    PredicateType.Le,
                                                    PredicateType.Gt,
                                                    PredicateType.Ge,
                                                    PredicateType.Lt));
    }
}
