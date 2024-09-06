public class miniExample5 {
    public int example(int n) {
        if (n >= 0) {
            int x = 0;
            while (x < n) {
                x = x + 1;
            }
            return x - n;
        } else {
            throw new RuntimeException("invalid value of n");
        }
    }
}
