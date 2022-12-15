package abstractinterp.scalar.state;

import abstractinterp.scalar.util.Pair;

public class PredicateValuePair extends Pair<PredicateType, Long> {

    private PredicateValuePair(PredicateType type, Long value) {
        super(type, value);
    }

    public static PredicateValuePair of(PredicateType type, Long value) {
        return new PredicateValuePair(type, value);
    }

    /** Create a predicate value pair using equality for predicate
     *
     * This is a kind of default constructor for pairs since we expect to use
     * equal predicates often.
     */
    public static PredicateValuePair of(Long value) {
        return PredicateValuePair.of(PredicateType.Eq, value);
    }

    public PredicateType predicate() {
        return this.fst();
    }

    public Long value() {
        return this.snd();
    }
}
