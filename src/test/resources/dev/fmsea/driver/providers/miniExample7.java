public class miniExample7 {
    public void example(int y) {
        int x = 0;
        if (x < y) {
            System.err.println("true");
        } else {
            System.err.println("false");
        }
        int w = 0;
        while (w < y) {
            if (x == w) {
                System.err.println("inner true?");
            }
            System.err.println("post loop");
            w++;
        }
    }
}
