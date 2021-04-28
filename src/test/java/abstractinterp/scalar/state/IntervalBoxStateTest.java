package abstractinterp.scalar.state;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IntervalBoxStateTest {

    @Test
    void transferBinaryReturnsTopWhenUnbounded() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = new Interval32Box(1, null);
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 6);
        Assertions.assertEquals(Interval32Box.TOP(), z);
        z = IntervalBoxState.transferBinary(y, x, (byte)5);
        Assertions.assertEquals(Interval32Box.TOP(), z);
    }

    @Test
    void transferConditionReturnsTopWhenUnbounded() {
        Interval32Box x = new Interval32Box(null, 1);
        Interval32Box y = new Interval32Box(1, null);
        List<Interval32Box> zs = IntervalBoxState.transferCond(x, y, (byte) 6);
        for (Interval32Box b : zs) {
            Assertions.assertEquals(Interval32Box.TOP(), b);
        }
        zs = IntervalBoxState.transferCond(y, x, (byte) 5);
        for (Interval32Box b : zs) {
            Assertions.assertEquals(Interval32Box.TOP(), b);
        }
    }

    @Test
    void transferBinaryDivisionDivideByZero() {
        Interval32Box x = new Interval32Box(-4, 3);
        Interval32Box y = new Interval32Box(0, 0);
        Interval32Box z = IntervalBoxState.transferBinary(x, y, (byte) 3);
        Assertions.assertTrue(z.isMax());
    }
}
