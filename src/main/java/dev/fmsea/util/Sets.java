package dev.fmsea.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sets {

    public static <T> Set<T> union(Set<T> left, Set<T> right) {
        return Stream.concat(left.stream(),
                             right.stream())
            .collect(Collectors.toSet());
    }

    public static <T> Set<T> difference(Set<T> left, Set<T> right) {
        return left.stream().filter(e -> !right.contains(e)).collect(Collectors.toSet());
    }

    public static <T> boolean equal(Set<T> left, Set<T> right) {
        return left.containsAll(right) && right.containsAll(left);
    }

    public static <T> boolean subset(Set<T> left, Set<T> right) {
        return right.containsAll(left);
    }
}
