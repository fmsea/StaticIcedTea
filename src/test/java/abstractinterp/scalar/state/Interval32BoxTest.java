package abstractinterp.scalar.state;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Interval32BoxTest {

    @Test
    void testInterval32BoxClone() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box newTop = new Interval32Box(top);
        Assertions.assertTrue(newTop.isTop());
    }

    @Test
    void testInterval32BoxCloneWhenNull() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box newBot = new Interval32Box(bot);
        Assertions.assertTrue(newBot.isBottom());
    }

    @Test
    void testIsBottom() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(bot.isBottom());
        Assertions.assertFalse(top.isBottom());
        Assertions.assertFalse(max.isBottom());
        Interval32Box box = new Interval32Box(1, 0);
        Assertions.assertTrue(box.isBottom());
    }

    @Test
    void testIsTop() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(top.isTop());
        Assertions.assertFalse(bot.isTop());
        Assertions.assertFalse(max.isTop());
    }

    @Test
    void testIsLowerBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isLowerBounded());
        Assertions.assertFalse(top.isLowerBounded());
        Assertions.assertTrue(max.isLowerBounded());
        Interval32Box box = new Interval32Box(null, 1);
        Assertions.assertFalse(box.isLowerBounded());
    }

    @Test
    void testIsUpperBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isUpperBounded());
        Assertions.assertFalse(top.isUpperBounded());
        Assertions.assertTrue(max.isUpperBounded());
        Interval32Box box = new Interval32Box(1, null);
        Assertions.assertFalse(box.isUpperBounded());
    }

    @Test
    void testIsBounded() {
        Interval32Box top = Interval32Box.TOP();
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isBounded());
        Assertions.assertFalse(top.isBounded());
        Assertions.assertTrue(max.isBounded());
        Interval32Box box = new Interval32Box(1, null);
        Assertions.assertFalse(box.isBounded());
        box = new Interval32Box(0, 1);
        Assertions.assertTrue(box.isBounded());
    }

    @Test
    void testIsValid() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertFalse(bot.isValid());
        Assertions.assertTrue(top.isValid());
        Assertions.assertTrue(max.isValid());
        Interval32Box box = new Interval32Box(0, 1);
        Assertions.assertTrue(box.isValid());
    }

    @Test
    void testBottomDoesNotContainPoints() {
        Assertions.assertFalse(Interval32Box.BOT().containsIntegerPoint());
    }

    @Test
    void testTopContainsIntegerPoints() {
        Assertions.assertTrue(Interval32Box.TOP().containsIntegerPoint());
    }

    @Test
    void testMaxContainsIntegerPoints() {
        Assertions.assertTrue(Interval32Box.MAX().containsIntegerPoint());
    }

    @Test
    void testMinAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.minAssign(top);
        Assertions.assertFalse(bot.isLowerBounded());
        Assertions.assertFalse(bot.isUpperBounded());
        bot.minAssign(max);
        Assertions.assertTrue(bot.isLowerBounded());
        Assertions.assertFalse(bot.isUpperBounded());
        Interval32Box x = new Interval32Box(0, null);
        Interval32Box y = new Interval32Box(-1, null);
        x.minAssign(y);
        Assertions.assertEquals(-1, x.lowerBound());
    }

    @Test
    void testMaxAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.maxAssign(top);
        Assertions.assertFalse(bot.isUpperBounded());
        Assertions.assertFalse(bot.isLowerBounded());
        bot.maxAssign(max);
        Assertions.assertTrue(bot.isUpperBounded());
        Assertions.assertFalse(bot.isLowerBounded());
        Interval32Box x = new Interval32Box(null, 0);
        Interval32Box y = new Interval32Box(null, 1);
        x.maxAssign(y);
        Assertions.assertEquals(1, x.upperBound());
    }

    @Test
    void testUpperBoundAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box ano_bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        bot.upperBoundAssign(ano_bot);
        Assertions.assertTrue(bot.isBottom());
        ano_bot.upperBoundAssign(top);
        Assertions.assertTrue(ano_bot.isTop());
        Interval32Box x = new Interval32Box(0, 1);
        Interval32Box y = new Interval32Box(1, 2);
        x.upperBoundAssign(y);
        Assertions.assertEquals(0, x.lowerBound());
        Assertions.assertEquals(2, x.upperBound());
        x = new Interval32Box(0, 1);
        y = new Interval32Box(-1, 2);
        x.upperBoundAssign(y);
        Assertions.assertEquals(-1, x.lowerBound());
        Assertions.assertEquals(2, x.upperBound());
    }

    @Test
    void testWideningAssign() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box a = new Interval32Box(null, 0);
        Interval32Box b = new Interval32Box(0, null);
        bot.wideningAssign(top);
        Assertions.assertTrue(bot.isTop());
        top.wideningAssign(a);
        Assertions.assertTrue(top.isTop());
        a.wideningAssign(b);
        Assertions.assertEquals(0, a.lowerBound());
        Assertions.assertEquals(0, a.upperBound());
        bot = Interval32Box.BOT();
        a = new Interval32Box(1, 10);
        bot.wideningAssign(a);
        Assertions.assertEquals(1, bot.lowerBound());
        Assertions.assertEquals(10, bot.upperBound());
        b = new Interval32Box(1, 11);
        a.wideningAssign(b);
        Assertions.assertEquals(1, a.lowerBound());
        Assertions.assertEquals(Integer.MAX_VALUE, a.upperBound());
        b = new Interval32Box(0, 12);
        a.wideningAssign(b);
        Assertions.assertEquals(Integer.MIN_VALUE, a.lowerBound());
        Assertions.assertEquals(Integer.MAX_VALUE, a.upperBound());
    }

    @Test
    void testNegate() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        bot.negate();
        Assertions.assertTrue(bot.isBottom());
        top.negate();
        Assertions.assertTrue(top.isTop());
        max.negate();
        Assertions.assertTrue(max.isBottom());
        Assertions.assertEquals(-1 * Integer.MAX_VALUE, max.lowerBound());
        Assertions.assertEquals(-1 * Integer.MIN_VALUE, max.upperBound());
        Interval32Box box = new Interval32Box(0, 1);
        box.negate();
        Assertions.assertEquals(-1, box.lowerBound());
        Assertions.assertEquals(0, box.upperBound());
        box = new Interval32Box(-5, 1);
        box.negate();
        Assertions.assertEquals(-1, box.lowerBound());
        Assertions.assertEquals(5, box.upperBound());
    }

    @Test
    void testToString() {
        Interval32Box bot = Interval32Box.BOT();
        Interval32Box top = Interval32Box.TOP();
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertEquals("⟘", bot.toString());
        Assertions.assertEquals("⟙", top.toString());
        Assertions.assertEquals("[-2147483648, 2147483647]", max.toString());
        Interval32Box box = new Interval32Box(-4, 16);
        Assertions.assertEquals("[-4, 16]", box.toString());
        box = new Interval32Box(1, 1);
        Assertions.assertEquals("1", box.toString());
    }

    @Test
    void testEquals() {
        Interval32Box bot = Interval32Box.BOT();
        Assertions.assertFalse(bot.equals(null));
        Assertions.assertTrue(bot.equals(Interval32Box.BOT()));
        Interval32Box top = Interval32Box.TOP();
        Assertions.assertTrue(top.equals(Interval32Box.TOP()));
        Assertions.assertFalse(top.equals(bot));
        Interval32Box max = Interval32Box.MAX();
        Assertions.assertTrue(max.equals(Interval32Box.MAX()));
        Assertions.assertFalse(max.equals(top));
        Assertions.assertFalse(max.equals(bot));
    }
}
