package dev.fmsea.util;

import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

public class Streams {

    public static <K, V> Map<K, V> zipToMap(Stream<K> keys, Stream<V> values) {
        Iterator<V> vIter = values.iterator();
        return keys.collect(Collectors.toMap(k -> k, _k -> vIter.next()));
    }
}
