package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Set;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.PredicateType;

public class ConstraintProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Constraint.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        Arbitrary<Integer> bounds = Arbitraries.integers().between(Integer.MIN_VALUE,
                                                                   Integer.MAX_VALUE);
        Arbitrary<PredicateType> predicates = Arbitraries.of(PredicateType.Eq, PredicateType.Ge);
        Arbitrary<Constraint> constraints = Combinators.combine(bounds, predicates)
            .as((b, p) -> {
                    return new Constraint(b, p);
                });
        return Collections.singleton(constraints);
    }
}
