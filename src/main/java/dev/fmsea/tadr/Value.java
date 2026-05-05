package dev.fmsea.tadr;

import dev.fmsea.absint.scalar.state.Interval32Box;

public class Value extends TADR {
    public final Interval32Box number;

    public Value(Interval32Box number) {
        this.number = number;
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visit(this);
    }
}
