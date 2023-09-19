package tadr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import abstractinterp.scalar.state.Interval32Box;
import soot.jimple.NullConstant;

public class TADRTest {

    @Test
    public void testTADRCanEvaluateNull() {
        assertEquals(TADR.newValue(Interval32Box.TOP()), TADR.from(null));
    }

    @Test
    public void testTADRCanEvalutateNullConstants() {
        assertEquals(TADR.newValue(Interval32Box.TOP()), TADR.from(NullConstant.v()));
    }

}
