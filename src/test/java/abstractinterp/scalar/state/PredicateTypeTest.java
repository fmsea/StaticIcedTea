package abstractinterp.scalar.state;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PredicateTypeTest {

    @Test
    void testPredicateTypeRotation() {
        Assertions.assertEquals(PredicateType.Invalid, PredicateType.Invalid.rotate());
        Assertions.assertEquals(PredicateType.Ne, PredicateType.Eq.rotate());
        Assertions.assertEquals(PredicateType.Eq, PredicateType.Ne.rotate());
        Assertions.assertEquals(PredicateType.Le, PredicateType.Gt.rotate());
        Assertions.assertEquals(PredicateType.Lt, PredicateType.Ge.rotate());
        Assertions.assertEquals(PredicateType.Ge, PredicateType.Lt.rotate());
        Assertions.assertEquals(PredicateType.Gt, PredicateType.Le.rotate());
    }

    @Test
    void testPredicateTypeNegation() {
        Assertions.assertEquals(PredicateType.Invalid, PredicateType.Invalid.negate());
        Assertions.assertEquals(PredicateType.Le, PredicateType.Ge.negate());
        Assertions.assertEquals(PredicateType.Ge, PredicateType.Le.negate());
        Assertions.assertEquals(PredicateType.Lt, PredicateType.Gt.negate());
        Assertions.assertEquals(PredicateType.Gt, PredicateType.Lt.negate());
        Assertions.assertEquals(PredicateType.Eq, PredicateType.Eq.negate());
        Assertions.assertEquals(PredicateType.Ne, PredicateType.Ne.negate());
    }
}
