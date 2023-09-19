package tadr;

import abstractinterp.scalar.state.Interval32Box;

public class Value extends TADR {
    public final Interval32Box number;

    public Value(Interval32Box number) {
        this.number = number;
    }

    public String toSmt() {
        if (this.number.isSingleton() && this.number.lowerBoundOrElse() < 0) {
            return String.format("(- %s)", this.number.lowerBoundOrElse() * -1);
        } else {
            return this.number.toString();
        }
    }

    public <R> R accept(TADR.Visitor<R> visitor) {
        return visitor.visitValue(this);
    }
}
