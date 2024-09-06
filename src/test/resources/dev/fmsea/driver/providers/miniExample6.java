public class miniExample6 {
    public void example() {
        int v1 = 0;
        int v2 = 1;
        while (1 == random()) {
            if (v1 == v2) {
                if (1 == random()) {
                    v2 = v1 + 1;
                } else {
                    v1 = v2 + 1;
                }
            }
        }
    }

    int random() {
        int r;
        if (Math.random() < 0.5) {
            r = 0;
        } else {
            r = 1;
        }
        return r;
    }
}
