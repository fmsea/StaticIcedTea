package dev.fmsea.util;

public class Properties {

    public enum OctagonIncrementalClosureAlgorithm {
        CHAWDHARY,
        SEARCH,
    }

    public static OctagonIncrementalClosureAlgorithm IncrementalClosureAlgorithm = OctagonIncrementalClosureAlgorithm.SEARCH;

    public static boolean OutputMinimumChangedVariables = true;
}
