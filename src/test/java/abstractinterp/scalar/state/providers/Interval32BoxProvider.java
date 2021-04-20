package abstractinterp.scalar.state.providers;


import abstractinterp.scalar.state.Interval32Box;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Set;

public class Interval32BoxProvider implements ArbitraryProvider {

    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Interval32Box.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        Random r = new Random();
        int partition = r.nextInt(max);
        Arbitrary<Integer> lower = Arbitraries.integers().between(min, partition);
        Arbitrary<Integer> upper = Arbitraries.integers().between(partition, max);
        return Collections.singleton(Combinators.combine(lower, upper).as(Interval32Box::new));
    }
}
