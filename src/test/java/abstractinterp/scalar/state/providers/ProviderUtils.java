package abstractinterp.scalar.state.providers;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.jqwik.api.Arbitrary;

public class ProviderUtils {

    private static final int DEFAULT_QUANTITY = 100;

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
