package abstractinterp.scalar.state;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PADO01ConstraintTest {

    @Test
    void testIsTop() {
        assertAll(() -> assertFalse(PADO01Constraint.BOT().isTop()),
                  () -> assertFalse(PADO01Constraint.of(0).isTop()),
                  () -> assertFalse(PADO01Constraint.of(Integer.MAX_VALUE - 1).isTop()),
                  () -> assertTrue(PADO01Constraint.TOP().isTop()),
                  () -> assertTrue(PADO01Constraint.of(Integer.MAX_VALUE).isTop()),
                  () -> assertTrue(PADO01Constraint.of(Optional.empty()).isTop()));
    }

    @Test
    void testIsBottom() {
        assertAll(() -> assertFalse(PADO01Constraint.TOP().isBottom()),
                  () -> assertFalse(PADO01Constraint.of(0).isBottom()),
                  () -> assertFalse(PADO01Constraint.of(Integer.MIN_VALUE + 1).isBottom()),
                  () -> assertTrue(PADO01Constraint.BOT().isBottom()),
                  () -> assertTrue(PADO01Constraint.of(Integer.MIN_VALUE).isBottom()),
                  () -> assertTrue(PADO01Constraint.of(null, true).isBottom()));
    }

    @Test
    void testMakeBottom() {
        {
            PADO01Constraint x = PADO01Constraint.of(0);
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }

        {
            PADO01Constraint x = PADO01Constraint.TOP();
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }

        {
            PADO01Constraint x = PADO01Constraint.BOT();
            x.makeBottom();
            assertAll(() -> assertTrue(x.isBottom()),
                      () -> assertTrue(x.bound().isEmpty()));
        }
    }

    @Test
    void testAddition() {
        {
            PADO01Constraint x = PADO01Constraint.of(0);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.add(x, y)),
                      () -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.add(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(1);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(2),
                                         PADO01Constraint.add(x, y)),
                      () -> assertEquals(PADO01Constraint.of(2),
                                         PADO01Constraint.add(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(+1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.add(x, y)),
                      () -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.add(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.TOP();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.add(x, y)),
                      () -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.add(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.BOT();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.add(x, y)),
                      () -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.add(y, x)));
        }
    }

    @Test
    void testSubtraction() {
        {
            PADO01Constraint x = PADO01Constraint.of(0);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(-1),
                                         PADO01Constraint.subtract(x, y)),
                      () -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.subtract(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(1);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.subtract(x, y)),
                      () -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.subtract(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(+1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(+2),
                                         PADO01Constraint.subtract(x, y)),
                      () -> assertEquals(PADO01Constraint.of(-2),
                                         PADO01Constraint.subtract(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.TOP();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.subtract(x, y)),
                      () -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.subtract(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.BOT();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.subtract(x, y)),
                      () -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.subtract(y, x)));
        }
    }

    @Test
    void testMultiplication() {
        {
            PADO01Constraint x = PADO01Constraint.of(0);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.multiply(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(1);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.multiply(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(+1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(-1),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.of(-1),
                                         PADO01Constraint.multiply(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(-1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(+1),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.of(+1),
                                         PADO01Constraint.multiply(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.TOP();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.multiply(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.BOT();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.multiply(x, y)),
                      () -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.multiply(y, x)));
        }
    }

    @Test
    void testDivision() {
        {
            PADO01Constraint x = PADO01Constraint.of(0);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.divide(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(1);
            PADO01Constraint y = PADO01Constraint.of(1);
            assertAll(() -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.of(1),
                                         PADO01Constraint.divide(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(+1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(-1),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.of(-1),
                                         PADO01Constraint.divide(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.of(-1);
            PADO01Constraint y = PADO01Constraint.of(-1);
            assertAll(() -> assertEquals(PADO01Constraint.of(+1),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.of(+1),
                                         PADO01Constraint.divide(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.TOP();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.TOP(),
                                         PADO01Constraint.divide(y, x)));
        }

        {
            PADO01Constraint x = PADO01Constraint.BOT();
            PADO01Constraint y = PADO01Constraint.of(3);
            assertAll(() -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.divide(x, y)),
                      () -> assertEquals(PADO01Constraint.BOT(),
                                         PADO01Constraint.divide(y, x)));
        }
    }

    @Test
    void testConstraintAdditionOverflow() {
        PADO01Constraint x = PADO01Constraint.of(Integer.MAX_VALUE - 2);
        PADO01Constraint y = PADO01Constraint.of(3);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.add(x, y)),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.add(y, x)));
    }

    @Test
    void testConstraintSubtractionUnderflow() {
        PADO01Constraint x = PADO01Constraint.of(Integer.MIN_VALUE + 2);
        PADO01Constraint y = PADO01Constraint.of(10);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.subtract(x, y)),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.subtract(y, x)));
    }

    @Test
    void testConstraintMultiplicationOverflow() {
        PADO01Constraint x = PADO01Constraint.of(Integer.MAX_VALUE - 20);
        PADO01Constraint y = PADO01Constraint.of(2);
        assertAll(() -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.multiply(x, y)),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.multiply(y, x)));
    }

    @Test
    void testBottomConstraintsAreBottom() {
        assertAll(() -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.add(PADO01Constraint.TOP(),
                                                          PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.add(PADO01Constraint.BOT(),
                                                          PADO01Constraint.TOP())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.subtract(PADO01Constraint.TOP(),
                                                               PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.subtract(PADO01Constraint.BOT(),
                                                               PADO01Constraint.TOP())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.multiply(PADO01Constraint.TOP(),
                                                               PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.multiply(PADO01Constraint.BOT(),
                                                               PADO01Constraint.TOP())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.divide(PADO01Constraint.TOP(),
                                                             PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.divide(PADO01Constraint.BOT(),
                                                             PADO01Constraint.TOP())));
    }

    @Test
    void testCompares() {
        assertAll(() -> assertEquals(-1,
                                     PADO01Constraint.compare(PADO01Constraint.of(0),
                                                              PADO01Constraint.of(1))),
                  () -> assertEquals(+0,
                                     PADO01Constraint.compare(PADO01Constraint.of(0),
                                                              PADO01Constraint.of(0))),
                  () -> assertEquals(+1,
                                     PADO01Constraint.compare(PADO01Constraint.of(1),
                                                              PADO01Constraint.of(0))),
                  () -> assertEquals(+1,
                                     PADO01Constraint.compare(PADO01Constraint.of(-1),
                                                              PADO01Constraint.of(-2))),
                  () -> assertEquals(-1,
                                     PADO01Constraint.compare(PADO01Constraint.of(-2),
                                                              PADO01Constraint.of(-1))),
                  () -> assertEquals(+0,
                                     PADO01Constraint.compare(PADO01Constraint.BOT(),
                                                              PADO01Constraint.BOT())),
                  () -> assertEquals(+0,
                                     PADO01Constraint.compare(PADO01Constraint.TOP(),
                                                              PADO01Constraint.TOP())),
                  () -> assertEquals(-1,
                                     PADO01Constraint.compare(PADO01Constraint.BOT(),
                                                              PADO01Constraint.TOP())),
                  () -> assertEquals(+1,
                                     PADO01Constraint.compare(PADO01Constraint.TOP(),
                                                              PADO01Constraint.BOT())),
                  () -> assertEquals(-1,
                                     PADO01Constraint.compare(PADO01Constraint.BOT(),
                                                              PADO01Constraint.of(0))),
                  () -> assertEquals(+1,
                                     PADO01Constraint.compare(PADO01Constraint.of(0),
                                                              PADO01Constraint.BOT())),
                  () -> assertEquals(-1,
                                     PADO01Constraint.compare(PADO01Constraint.of(0),
                                                              PADO01Constraint.TOP())),
                  () -> assertEquals(+1,
                                     PADO01Constraint.compare(PADO01Constraint.TOP(),
                                                              PADO01Constraint.of(0))));
    }

    @Test
    void testMax() {
        assertAll(() -> assertEquals(PADO01Constraint.of(1),
                                     PADO01Constraint.max(PADO01Constraint.of(0),
                                                          PADO01Constraint.of(1))),
                  () -> assertEquals(PADO01Constraint.of(1),
                                     PADO01Constraint.max(PADO01Constraint.of(1),
                                                          PADO01Constraint.of(0))),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.max(PADO01Constraint.TOP(),
                                                          PADO01Constraint.of(1))),
                  () -> assertEquals(PADO01Constraint.TOP(),
                                     PADO01Constraint.max(PADO01Constraint.of(1),
                                                          PADO01Constraint.TOP())),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     PADO01Constraint.max(PADO01Constraint.of(0),
                                                          PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     PADO01Constraint.max(PADO01Constraint.BOT(),
                                                          PADO01Constraint.of(0))));
    }

    @Test
    void testMin() {
        assertAll(() -> assertEquals(PADO01Constraint.of(0),
                                     PADO01Constraint.min(PADO01Constraint.of(0),
                                                          PADO01Constraint.of(1))),
                  () -> assertEquals(PADO01Constraint.of(0),
                                     PADO01Constraint.min(PADO01Constraint.of(1),
                                                          PADO01Constraint.of(0))),
                  () -> assertEquals(PADO01Constraint.of(1),
                                     PADO01Constraint.min(PADO01Constraint.TOP(),
                                                          PADO01Constraint.of(1))),
                  () -> assertEquals(PADO01Constraint.of(1),
                                     PADO01Constraint.min(PADO01Constraint.of(1),
                                                          PADO01Constraint.TOP())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.min(PADO01Constraint.of(0),
                                                          PADO01Constraint.BOT())),
                  () -> assertEquals(PADO01Constraint.BOT(),
                                     PADO01Constraint.min(PADO01Constraint.BOT(),
                                                          PADO01Constraint.of(0))));
    }

    @Test
    void testToString() {
        assertAll(() -> assertEquals("⟘", PADO01Constraint.BOT().toString()),
                  () -> assertEquals("⟙", PADO01Constraint.TOP().toString()),
                  () -> assertEquals("0", PADO01Constraint.of(0).toString()));
    }
}
