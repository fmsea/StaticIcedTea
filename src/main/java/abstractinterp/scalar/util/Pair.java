package abstractinterp.scalar.util;

public class Pair<S, T> {
    private S fst;
    private T snd;

    protected Pair(S fst, T snd) {
        this.fst = fst;
        this.snd = snd;
    }

    public static <S, T> Pair<S, T> of(S fst, T snd) {
        return new Pair<>(fst, snd);
    }

    public S fst() {
        return this.fst;
    }

    public T snd() {
        return this.snd;
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;
        if (o != null && o instanceof Pair) {
            equal = this.equals((Pair<S, T>) o);
        }
        return equal;
    }

    public boolean equals(Pair<S, T> o) {
        return (o != null &&
                this.fst.equals(o.fst) &&
                this.snd.equals(o.snd));
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.fst.hashCode();
        result = prime * result + this.snd.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)",
                             this.fst.toString(),
                             this.snd.toString());
    }
}
