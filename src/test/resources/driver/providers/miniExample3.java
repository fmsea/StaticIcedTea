public class miniExample3 {
    public int example(int n) {
        int p0 = 0;
        int p1 = 1;
        int p2 = 1;

        for (int i = 3; i < n; i++) {
            int p = p2 + p1 + p0;
            p0 = p1;
            p1 = p2;
            p2 = p;
        }
        return p2;
    }
}
