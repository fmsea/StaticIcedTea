package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Set;

import abstractinterp.scalar.state.Constraint;

public class ConstraintProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Constraint.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        Arbitrary<Integer> bounds = Arbitraries.integers().between(Integer.MIN_VALUE,
                                                                   Integer.MAX_VALUE);

        return Collections.singleton(bounds.map(b -> Constraint.of(b)));
    }
}
