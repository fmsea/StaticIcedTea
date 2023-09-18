package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitrary;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import soot.Local;

import abstractinterp.scalar.state.Constraint;
import abstractinterp.scalar.state.ZoneDifferenceBoundedMatrix;

public class ProviderUtils {

    private static final int DEFAULT_QUANTITY = 1000;

    public static Set<Arbitrary<?>> provideSetOf(Supplier<Arbitrary<?>> supplier) {
        return provideSetOf(DEFAULT_QUANTITY, supplier);
    }

    public static Set<Arbitrary<?>> provideSetOf(int quantity, Supplier<Arbitrary<?>> supplier) {
        Stream<Arbitrary<?>> ms = IntStream.range(0, quantity).mapToObj((i) -> {
                return supplier.get();
            });
        return ms.collect(Collectors.toSet());
    }
}
