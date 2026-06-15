package dev.fmsea.absint;

public enum ConstraintType {
    UNBOUND,
    CONSTANT,
    INTERVAL,
    ZONAL,
    OCTAGONAL;

    public String toString() {
        switch (this) {
            case CONSTANT:
                return "CONSTANT";
            case INTERVAL:
                return "INTERVAL";
            case ZONAL:
                return "ZONAL";
            case OCTAGONAL:
                return "OCTAGONAL";
            case UNBOUND:
            default:
                return "UNBOUND";
        }
    }
}
