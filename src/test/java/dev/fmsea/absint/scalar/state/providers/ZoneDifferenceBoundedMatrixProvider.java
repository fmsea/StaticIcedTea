package dev.fmsea.absint.scalar.state.providers;

import java.util.Random;
import java.util.Set;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.ZoneDifferenceBoundedMatrix;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import soot.Local;

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
