package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import soot.Local;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ZoneDifferenceBoundedMatrix;

public class ZoneDifferenceBoundedMatrixProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(ZoneDifferenceBoundedMatrix.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        // some how, we're going to create arbitrary DBM's... this will be fun.
        return ProviderUtils.provideSetOf(this::provideDBM);
    }

    public Arbitrary<ZoneDifferenceBoundedMatrix> provideDBM() {
        Random r = new Random();
        Arbitrary<ZoneDifferenceBoundedMatrix> matrix = Arbitraries.create(() -> sample(16, 0.5));
        return matrix;
    }

    public static ZoneDifferenceBoundedMatrix sample(int N, double density) {
        return sample(N, density, System.currentTimeMillis());
    }

    public static ZoneDifferenceBoundedMatrix sample(int N, double density, long seed) {
        Random r = new Random(seed);
        Set<Local> ls = LocalProvider.generateLocals(N);
        ZoneDifferenceBoundedMatrix m = new ZoneDifferenceBoundedMatrix(ls, true);
        for (Local s : ls) {
            for (Local t : ls) {
                if (!s.equals(t) && r.nextDouble() < density) {
                    Constraint c = ConstraintProvider.sample();
                    m.setConstraint(s, t, c);
                }
            }
        }
        return m;
    }
}
