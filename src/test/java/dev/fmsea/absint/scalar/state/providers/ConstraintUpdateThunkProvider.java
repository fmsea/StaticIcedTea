package dev.fmsea.absint.scalar.state.providers;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.ConstraintUpdateThunk;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

public class ConstraintUpdateThunkProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(ConstraintUpdateThunk.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return ProviderUtils.provideSetOf(this::provide);
    }

    public Arbitrary<ConstraintUpdateThunk> provide() {
        Arbitrary<ConstraintUpdateThunk> thunks = Arbitraries.create(() -> {
            return ConstraintUpdateThunkProvider.sample();
            });
        return thunks;
    }

    public static ConstraintUpdateThunk sample() {
        Random r = new Random();
        int i = r.nextInt(200);
        int j = r.nextInt(200);
        int c = r.nextInt();
        return ConstraintUpdateThunk.of(i, j, Constraint.of(c));
    }

    public static Set<ConstraintUpdateThunk> sample(int N) {
        return sample(N, 1234567890);
    }

    public static Set<ConstraintUpdateThunk> sample(int N, long seed) {
        Random r = new Random(seed);
        return IntStream.range(0, N / 2)
            .boxed()
            .flatMap(k -> {
            int s = r.nextInt(200);
            int t = r.nextInt(200);
            int c = r.nextInt();
            return Stream.of(
                ConstraintUpdateThunk.of(s, t, Constraint.of(c)),
                ConstraintUpdateThunk.of(t ^ 1, s ^ 1, Constraint.of(c)));
        }).collect(Collectors.toSet());
    }
}
