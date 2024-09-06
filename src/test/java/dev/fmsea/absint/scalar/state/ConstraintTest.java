package dev.fmsea.absint.scalar.state;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ConstraintTest {

    @Test
    void testIsTop() {
        assertAll(() -> assertFalse(Constraint.BOT().isTop()),
                  () -> assertFalse(Constraint.of(0).isTop()),
                  () -> assertFalse(Constraint.of(Integer.MAX_VALUE - 1).isTop()),
                  () -> assertTrue(Constraint.TOP().isTop()),
                  () -> assertFalse(Constraint.of(Integer.MAX_VALUE).isTop()),
                  () -> assertTrue(Constraint.of(Optional.empty()).isTop()));
    }

    @Test
    void testIsBottom() {
        assertAll(() -> assertFalse(Constraint.TOP().isBottom()),
                  () -> assertFalse(Constraint.of(0).isBottom()),
                  () -> assertFalse(Constraint.of(Integer.MIN_VALUE + 1).isBottom()),
                  () -> assertTrue(Constraint.BOT().isBottom()),
                  () -> assertFalse(Constraint.of(Integer.MIN_VALUE).isBottom()),
                  () -> assertTrue(Constraint.of(null, true).isBottom()));
    }

    @Test
    void testAddition() {
        {
            Constraint x = Constraint.of(0);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(1),
                                         Constraint.add(x, y)),
                      () -> assertEquals(Constraint.of(1),
                                         Constraint.add(y, x)));
        }

        {
            Constraint x = Constraint.of(1);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(2),
                                         Constraint.add(x, y)),
                      () -> assertEquals(Constraint.of(2),
                                         Constraint.add(y, x)));
        }

        {
            Constraint x = Constraint.of(+1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         Constraint.add(x, y)),
                      () -> assertEquals(Constraint.of(0),
                                         Constraint.add(y, x)));
        }

        {
            Constraint x = Constraint.TOP();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.TOP(),
                                         Constraint.add(x, y)),
                      () -> assertEquals(Constraint.TOP(),
                                         Constraint.add(y, x)));
        }

        {
            Constraint x = Constraint.BOT();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.BOT(),
                                         Constraint.add(x, y)),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.add(y, x)));
        }
    }

    @Test
    void testSubtraction() {
        {
            Constraint x = Constraint.of(0);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         Constraint.subtract(x, y)),
                      () -> assertEquals(Constraint.of(1),
                                         Constraint.subtract(y, x)));
        }

        {
            Constraint x = Constraint.of(1);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         Constraint.subtract(x, y)),
                      () -> assertEquals(Constraint.of(0),
                                         Constraint.subtract(y, x)));
        }

        {
            Constraint x = Constraint.of(+1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(+2),
                                         Constraint.subtract(x, y)),
                      () -> assertEquals(Constraint.of(-2),
                                         Constraint.subtract(y, x)));
        }

        {
            Constraint x = Constraint.TOP();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.TOP(),
                                         Constraint.subtract(x, y)),
                      () -> assertEquals(Constraint.TOP(),
                                         Constraint.subtract(y, x)));
        }

        {
            Constraint x = Constraint.BOT();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.BOT(),
                                         Constraint.subtract(x, y)),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.subtract(y, x)));
        }
    }

    @Test
    void testMultiplication() {
        {
            Constraint x = Constraint.of(0);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.of(0),
                                         Constraint.multiply(y, x)));
        }

        {
            Constraint x = Constraint.of(1);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(1),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.of(1),
                                         Constraint.multiply(y, x)));
        }

        {
            Constraint x = Constraint.of(+1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.of(-1),
                                         Constraint.multiply(y, x)));
        }

        {
            Constraint x = Constraint.of(-1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(+1),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.of(+1),
                                         Constraint.multiply(y, x)));
        }

        {
            Constraint x = Constraint.TOP();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.TOP(),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.TOP(),
                                         Constraint.multiply(y, x)));
        }

        {
            Constraint x = Constraint.BOT();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.BOT(),
                                         Constraint.multiply(x, y)),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.multiply(y, x)));
        }
    }

    @Test
    void testDivision() {
        {
            Constraint x = Constraint.of(0);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(0),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.TOP(),
                                         Constraint.divide(y, x)));
        }

        {
            Constraint x = Constraint.of(1);
            Constraint y = Constraint.of(1);
            assertAll(() -> assertEquals(Constraint.of(1),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.of(1),
                                         Constraint.divide(y, x)));
        }

        {
            Constraint x = Constraint.of(+1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(-1),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.of(-1),
                                         Constraint.divide(y, x)));
        }

        {
            Constraint x = Constraint.of(-1);
            Constraint y = Constraint.of(-1);
            assertAll(() -> assertEquals(Constraint.of(+1),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.of(+1),
                                         Constraint.divide(y, x)));
        }

        {
            Constraint x = Constraint.TOP();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.TOP(),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.TOP(),
                                         Constraint.divide(y, x)));
        }

        {
            Constraint x = Constraint.BOT();
            Constraint y = Constraint.of(3);
            assertAll(() -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(x, y)),
                      () -> assertEquals(Constraint.BOT(),
                                         Constraint.divide(y, x)));
        }
    }

    @Test
    void testConstraintAdditionOverflow() {
        Constraint x = Constraint.of(Integer.MAX_VALUE - 2);
        Constraint y = Constraint.of(3);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     Constraint.add(x, y)),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.add(y, x)));
    }

    @Test
    void testConstraintSubtractionUnderflow() {
        Constraint x = Constraint.of(Integer.MIN_VALUE + 2);
        Constraint y = Constraint.of(10);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     Constraint.subtract(x, y)),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.subtract(y, x)));
    }

    @Test
    void testConstraintMultiplicationOverflow() {
        Constraint x = Constraint.of(Integer.MAX_VALUE - 20);
        Constraint y = Constraint.of(2);
        assertAll(() -> assertEquals(Constraint.TOP(),
                                     Constraint.multiply(x, y)),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.multiply(y, x)));
    }

    @Test
    void testBottomConstraintsAreBottom() {
        assertAll(() -> assertEquals(Constraint.BOT(),
                                     Constraint.add(Constraint.TOP(),
                                                    Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.add(Constraint.BOT(),
                                                    Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.subtract(Constraint.TOP(),
                                                         Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.subtract(Constraint.BOT(),
                                                         Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.multiply(Constraint.TOP(),
                                                         Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.multiply(Constraint.BOT(),
                                                         Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.divide(Constraint.TOP(),
                                                       Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.divide(Constraint.BOT(),
                                                       Constraint.TOP())));
    }

    @Test
    void testCompares() {
        assertAll(() -> assertEquals(-1,
                                     Constraint.compare(Constraint.of(0),
                                                        Constraint.of(1))),
                  () -> assertEquals(+0,
                                     Constraint.compare(Constraint.of(0),
                                                        Constraint.of(0))),
                  () -> assertEquals(+1,
                                     Constraint.compare(Constraint.of(1),
                                                        Constraint.of(0))),
                  () -> assertEquals(+1,
                                     Constraint.compare(Constraint.of(-1),
                                                        Constraint.of(-2))),
                  () -> assertEquals(-1,
                                     Constraint.compare(Constraint.of(-2),
                                                        Constraint.of(-1))),
                  () -> assertEquals(+0,
                                     Constraint.compare(Constraint.BOT(),
                                                        Constraint.BOT())),
                  () -> assertEquals(+0,
                                     Constraint.compare(Constraint.TOP(),
                                                        Constraint.TOP())),
                  () -> assertEquals(-1,
                                     Constraint.compare(Constraint.BOT(),
                                                        Constraint.TOP())),
                  () -> assertEquals(+1,
                                     Constraint.compare(Constraint.TOP(),
                                                        Constraint.BOT())),
                  () -> assertEquals(-1,
                                     Constraint.compare(Constraint.BOT(),
                                                        Constraint.of(0))),
                  () -> assertEquals(+1,
                                     Constraint.compare(Constraint.of(0),
                                                        Constraint.BOT())),
                  () -> assertEquals(-1,
                                     Constraint.compare(Constraint.of(0),
                                                        Constraint.TOP())),
                  () -> assertEquals(+1,
                                     Constraint.compare(Constraint.TOP(),
                                                        Constraint.of(0))));
    }

    @Test
    void testMax() {
        assertAll(() -> assertEquals(Constraint.of(1),
                                     Constraint.max(Constraint.of(0),
                                                    Constraint.of(1))),
                  () -> assertEquals(Constraint.of(1),
                                     Constraint.max(Constraint.of(1),
                                                    Constraint.of(0))),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.max(Constraint.TOP(),
                                                    Constraint.of(1))),
                  () -> assertEquals(Constraint.TOP(),
                                     Constraint.max(Constraint.of(1),
                                                    Constraint.TOP())),
                  () -> assertEquals(Constraint.of(0),
                                     Constraint.max(Constraint.of(0),
                                                    Constraint.BOT())),
                  () -> assertEquals(Constraint.of(0),
                                     Constraint.max(Constraint.BOT(),
                                                    Constraint.of(0))));
    }

    @Test
    void testMin() {
        assertAll(() -> assertEquals(Constraint.of(0),
                                     Constraint.min(Constraint.of(0),
                                                    Constraint.of(1))),
                  () -> assertEquals(Constraint.of(0),
                                     Constraint.min(Constraint.of(1),
                                                    Constraint.of(0))),
                  () -> assertEquals(Constraint.of(1),
                                     Constraint.min(Constraint.TOP(),
                                                    Constraint.of(1))),
                  () -> assertEquals(Constraint.of(1),
                                     Constraint.min(Constraint.of(1),
                                                    Constraint.TOP())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.min(Constraint.of(0),
                                                    Constraint.BOT())),
                  () -> assertEquals(Constraint.BOT(),
                                     Constraint.min(Constraint.BOT(),
                                                    Constraint.of(0))));
    }

    @Test
    void testMultipleMin() {
        assertEquals(Constraint.of(0),
                     Constraint.min(IntStream.range(0, 100)
                                    .boxed().map(i -> Constraint.of(i))));
    }

    @Test
    void testToString() {
        assertAll(() -> assertEquals("⟘", Constraint.BOT().toString()),
                  () -> assertEquals("⟙", Constraint.TOP().toString()),
                  () -> assertEquals("0", Constraint.of(0).toString()));
    }
}
