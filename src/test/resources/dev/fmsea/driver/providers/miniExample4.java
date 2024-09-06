public class miniExample4 {
    public int example(int n) {
        int p0 = 0;
        int p1 = 1;

        for (int i = 2; i < n; i++) {
            int p = p0 + p1;
            p0 = p1;
            p1 = p;
        }
        return p1;
    }
}
