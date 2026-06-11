package dev.fmsea.util;

import java.io.IOException;
import java.util.function.Consumer;

@FunctionalInterface
public interface IOThrowableConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrows(elem);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    void acceptThrows(T elem) throws IOException;
}
