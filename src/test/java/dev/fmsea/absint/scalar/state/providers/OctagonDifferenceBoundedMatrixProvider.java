package dev.fmsea.absint.scalar.state.providers;

import java.util.Random;
import java.util.Set;

import dev.fmsea.absint.scalar.state.Constraint;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrix;
import dev.fmsea.absint.scalar.state.OctagonDifferenceBoundedMatrixBuilder;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

public class OctagonDifferenceBoundedMatrixProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(OctagonDifferenceBoundedMatrix.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return ProviderUtils.provideSetOf(this::provideDBM);
    }

    public Arbitrary<OctagonDifferenceBoundedMatrix> provideDBM() {
        Arbitrary<OctagonDifferenceBoundedMatrix> matrix = Arbitraries.create(() -> {
                return OctagonDifferenceBoundedMatrixProvider.sample();
            });
        return matrix;
    }

    public static OctagonDifferenceBoundedMatrix sample() {
        Random r = new Random(1234567890);
        int N = 2 * (r.nextInt(8) + 1);
        return OctagonDifferenceBoundedMatrixProvider.sample(N);
    }

    public static OctagonDifferenceBoundedMatrix sample(int N) {
        return sample(N, 0.5, 1234567890);
    }

    public static OctagonDifferenceBoundedMatrix sample(int N, double density, long seed) {
        Random r = new Random(seed);
        OctagonDifferenceBoundedMatrixBuilder mb = new OctagonDifferenceBoundedMatrixBuilder(N, true);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++)  {
                if (i == j) {
                    mb.setConstraint(i, j, Constraint.of(0));
                } else if (r.nextDouble() < density) {
                    Constraint c = ConstraintProvider.sample();
                    mb.setConstraint(i, j, c);
                    mb.setConstraint(j ^ 1, i ^ 1, c.copy());
                }
            }
        }
        return mb.build();
    }

    public static OctagonDifferenceBoundedMatrix sampleMatrix(int N) {
        return sample(N, 0.5, 1234567890);
    }
}
