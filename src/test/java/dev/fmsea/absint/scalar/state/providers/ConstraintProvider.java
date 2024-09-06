package dev.fmsea.absint.scalar.state.providers;

import java.util.Set;

import dev.fmsea.absint.scalar.state.Constraint;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

public class ConstraintProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Constraint.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return provideConstraints();
    }

    public static Arbitrary<Constraint> provideConstraint() {
        Arbitrary<Integer> bounds = Arbitraries.integers().between(Integer.MIN_VALUE,
                                                                   Integer.MAX_VALUE);
        return bounds.map(b -> Constraint.of(b));
    }

    public static Set<Arbitrary<?>> provideConstraints() {
        return ProviderUtils.provideSetOf(ConstraintProvider::provideConstraint);
    }

    public static Constraint sample() {
        return provideConstraint().sample();
    }
}
