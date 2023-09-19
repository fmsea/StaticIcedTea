package tadr.rewrite.rules;

import java.util.Set;
import java.util.stream.Stream;

import tadr.TADR;

public abstract class ReassignmentFilter {

    protected Set<TADR> exprs;

    public ReassignmentFilter(Set<TADR> exprs) {
        this.exprs = exprs;
    }

    public abstract Stream<TADR> filter();
}
