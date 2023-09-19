package tadr.rewrite.rules;

import java.util.Set;
import java.util.stream.Stream;

import tadr.TADR;

public class OctagonReassignmentFilter extends ReassignmentFilter {
    public OctagonReassignmentFilter(Set<TADR> exprs) {
        super(exprs);
    }

    public Stream<TADR> filter() {
        return exprs
            .stream()
            .filter(e -> !e.accept(new ReassignmentIntervalFilter()));
    }
}
