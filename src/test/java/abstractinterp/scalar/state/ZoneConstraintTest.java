package abstractinterp.scalar.state;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ZoneConstraintTest {

    @Test
    void testIsTop() {
        assertAll(() -> assertFalse(ZoneConstraint.BOT().isTop()),
                  () -> assertFalse(ZoneConstraint.of(0).isTop()),
                  () -> assertFalse(ZoneConstraint.of(Integer.MAX_VALUE - 1).isTop()),
                  () -> assertTrue(ZoneConstraint.TOP().isTop()),
                  () -> assertFalse(ZoneConstraint.of(Integer.MAX_VALUE).isTop()),
                  () -> assertTrue(ZoneConstraint.of(Optional.empty()).isTop()));
    }

    @Test
    void testIsBottom() {
        assertAll(() -> assertFalse(ZoneConstraint.TOP().isBottom()),
                  () -> assertFalse(ZoneConstraint.of(0).isBottom()),
                  () -> assertFalse(ZoneConstraint.of(Integer.MIN_VALUE + 1).isBottom()),
                  () -> assertTrue(ZoneConstraint.BOT().isBottom()),
                  () -> assertFalse(ZoneConstraint.of(Integer.MIN_VALUE).isBottom()),
                  () -> assertTrue(ZoneConstraint.of(null, true).isBottom()));
    }

    @Test
    void testMakeBottom() {
        {
            ZoneConstraint x = ZoneConstraint.of(0);
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }

        {
            ZoneConstraint x = ZoneConstraint.TOP();
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }

        {
            ZoneConstraint x = ZoneConstraint.BOT();
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }
    }

    @Test
    void testAddition() {
        {
            ZoneConstraint x = ZoneConstraint.of(0);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.add(x, y)),
                      () -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.add(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(1);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(2),
                                         ZoneConstraint.add(x, y)),
                      () -> assertEquals(ZoneConstraint.of(2),
                                         ZoneConstraint.add(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(+1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.add(x, y)),
                      () -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.add(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.TOP();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.add(x, y)),
                      () -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.add(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.BOT();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.add(x, y)),
                      () -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.add(y, x)));
        }
    }

    @Test
    void testSubtraction() {
        {
            ZoneConstraint x = ZoneConstraint.of(0);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(-1),
                                         ZoneConstraint.subtract(x, y)),
                      () -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.subtract(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(1);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.subtract(x, y)),
                      () -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.subtract(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(+1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(+2),
                                         ZoneConstraint.subtract(x, y)),
                      () -> assertEquals(ZoneConstraint.of(-2),
                                         ZoneConstraint.subtract(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.TOP();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.subtract(x, y)),
                      () -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.subtract(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.BOT();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.subtract(x, y)),
                      () -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.subtract(y, x)));
        }
    }

    @Test
    void testMultiplication() {
        {
            ZoneConstraint x = ZoneConstraint.of(0);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.multiply(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(1);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.multiply(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(+1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(-1),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.of(-1),
                                         ZoneConstraint.multiply(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(-1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(+1),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.of(+1),
                                         ZoneConstraint.multiply(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.TOP();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.multiply(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.BOT();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.multiply(x, y)),
                      () -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.multiply(y, x)));
        }
    }

    @Test
    void testDivision() {
        {
            ZoneConstraint x = ZoneConstraint.of(0);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(0),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.divide(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(1);
            ZoneConstraint y = ZoneConstraint.of(1);
            assertAll(() -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.of(1),
                                         ZoneConstraint.divide(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(+1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(-1),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.of(-1),
                                         ZoneConstraint.divide(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.of(-1);
            ZoneConstraint y = ZoneConstraint.of(-1);
            assertAll(() -> assertEquals(ZoneConstraint.of(+1),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.of(+1),
                                         ZoneConstraint.divide(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.TOP();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.TOP(),
                                         ZoneConstraint.divide(y, x)));
        }

        {
            ZoneConstraint x = ZoneConstraint.BOT();
            ZoneConstraint y = ZoneConstraint.of(3);
            assertAll(() -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.divide(x, y)),
                      () -> assertEquals(ZoneConstraint.BOT(),
                                         ZoneConstraint.divide(y, x)));
        }
    }

    @Test
    void testConstraintAdditionOverflow() {
        ZoneConstraint x = ZoneConstraint.of(Integer.MAX_VALUE - 2);
        ZoneConstraint y = ZoneConstraint.of(3);
        assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.add(x, y)),
                  () -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.add(y, x)));
    }

    @Test
    void testConstraintSubtractionUnderflow() {
        ZoneConstraint x = ZoneConstraint.of(Integer.MIN_VALUE + 2);
        ZoneConstraint y = ZoneConstraint.of(10);
        assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.subtract(x, y)),
                  () -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.subtract(y, x)));
    }

    @Test
    void testConstraintMultiplicationOverflow() {
        ZoneConstraint x = ZoneConstraint.of(Integer.MAX_VALUE - 20);
        ZoneConstraint y = ZoneConstraint.of(2);
        assertAll(() -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.multiply(x, y)),
                  () -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.multiply(y, x)));
    }

    @Test
    void testBottomConstraintsAreBottom() {
        assertAll(() -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.add(ZoneConstraint.TOP(),
                                                          ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.add(ZoneConstraint.BOT(),
                                                          ZoneConstraint.TOP())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.subtract(ZoneConstraint.TOP(),
                                                               ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.subtract(ZoneConstraint.BOT(),
                                                               ZoneConstraint.TOP())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.multiply(ZoneConstraint.TOP(),
                                                               ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.multiply(ZoneConstraint.BOT(),
                                                               ZoneConstraint.TOP())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.divide(ZoneConstraint.TOP(),
                                                             ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.divide(ZoneConstraint.BOT(),
                                                             ZoneConstraint.TOP())));
    }

    @Test
    void testCompares() {
        assertAll(() -> assertEquals(-1,
                                     ZoneConstraint.compare(ZoneConstraint.of(0),
                                                              ZoneConstraint.of(1))),
                  () -> assertEquals(+0,
                                     ZoneConstraint.compare(ZoneConstraint.of(0),
                                                              ZoneConstraint.of(0))),
                  () -> assertEquals(+1,
                                     ZoneConstraint.compare(ZoneConstraint.of(1),
                                                              ZoneConstraint.of(0))),
                  () -> assertEquals(+1,
                                     ZoneConstraint.compare(ZoneConstraint.of(-1),
                                                              ZoneConstraint.of(-2))),
                  () -> assertEquals(-1,
                                     ZoneConstraint.compare(ZoneConstraint.of(-2),
                                                              ZoneConstraint.of(-1))),
                  () -> assertEquals(+0,
                                     ZoneConstraint.compare(ZoneConstraint.BOT(),
                                                              ZoneConstraint.BOT())),
                  () -> assertEquals(+0,
                                     ZoneConstraint.compare(ZoneConstraint.TOP(),
                                                              ZoneConstraint.TOP())),
                  () -> assertEquals(-1,
                                     ZoneConstraint.compare(ZoneConstraint.BOT(),
                                                              ZoneConstraint.TOP())),
                  () -> assertEquals(+1,
                                     ZoneConstraint.compare(ZoneConstraint.TOP(),
                                                              ZoneConstraint.BOT())),
                  () -> assertEquals(-1,
                                     ZoneConstraint.compare(ZoneConstraint.BOT(),
                                                              ZoneConstraint.of(0))),
                  () -> assertEquals(+1,
                                     ZoneConstraint.compare(ZoneConstraint.of(0),
                                                              ZoneConstraint.BOT())),
                  () -> assertEquals(-1,
                                     ZoneConstraint.compare(ZoneConstraint.of(0),
                                                              ZoneConstraint.TOP())),
                  () -> assertEquals(+1,
                                     ZoneConstraint.compare(ZoneConstraint.TOP(),
                                                              ZoneConstraint.of(0))));
    }

    @Test
    void testMax() {
        assertAll(() -> assertEquals(ZoneConstraint.of(1),
                                     ZoneConstraint.max(ZoneConstraint.of(0),
                                                          ZoneConstraint.of(1))),
                  () -> assertEquals(ZoneConstraint.of(1),
                                     ZoneConstraint.max(ZoneConstraint.of(1),
                                                          ZoneConstraint.of(0))),
                  () -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.max(ZoneConstraint.TOP(),
                                                          ZoneConstraint.of(1))),
                  () -> assertEquals(ZoneConstraint.TOP(),
                                     ZoneConstraint.max(ZoneConstraint.of(1),
                                                          ZoneConstraint.TOP())),
                  () -> assertEquals(ZoneConstraint.of(0),
                                     ZoneConstraint.max(ZoneConstraint.of(0),
                                                          ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.of(0),
                                     ZoneConstraint.max(ZoneConstraint.BOT(),
                                                          ZoneConstraint.of(0))));
    }

    @Test
    void testMin() {
        assertAll(() -> assertEquals(ZoneConstraint.of(0),
                                     ZoneConstraint.min(ZoneConstraint.of(0),
                                                          ZoneConstraint.of(1))),
                  () -> assertEquals(ZoneConstraint.of(0),
                                     ZoneConstraint.min(ZoneConstraint.of(1),
                                                          ZoneConstraint.of(0))),
                  () -> assertEquals(ZoneConstraint.of(1),
                                     ZoneConstraint.min(ZoneConstraint.TOP(),
                                                          ZoneConstraint.of(1))),
                  () -> assertEquals(ZoneConstraint.of(1),
                                     ZoneConstraint.min(ZoneConstraint.of(1),
                                                          ZoneConstraint.TOP())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.min(ZoneConstraint.of(0),
                                                          ZoneConstraint.BOT())),
                  () -> assertEquals(ZoneConstraint.BOT(),
                                     ZoneConstraint.min(ZoneConstraint.BOT(),
                                                          ZoneConstraint.of(0))));
    }

    @Test
    void testToString() {
        assertAll(() -> assertEquals("⟘", ZoneConstraint.BOT().toString()),
                  () -> assertEquals("⟙", ZoneConstraint.TOP().toString()),
                  () -> assertEquals("0", ZoneConstraint.of(0).toString()));
    }
}
