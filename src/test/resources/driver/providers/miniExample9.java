package test;

public class miniExample9 {
    public long example(long n) {
        long p0 = 0;
        long p1 = 1;
        long p = 0;

        for (long i = 0; i < n; i++) {
            p = p1 + p0;
            p0 = p1;
            p1 = p;
        }

        return p;
    }
}
