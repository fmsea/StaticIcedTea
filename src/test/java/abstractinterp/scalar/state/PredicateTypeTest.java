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

    @Test
    void testPredicatePreorderEquality() {
        Assertions.assertEquals(PredicateType.Eq, PredicateType.minimum(PredicateType.Eq,
                                                                        PredicateType.Eq));
        Assertions.assertEquals(PredicateType.Ne, PredicateType.minimum(PredicateType.Ne,
                                                                        PredicateType.Ne));
        Assertions.assertEquals(PredicateType.Le, PredicateType.minimum(PredicateType.Le,
                                                                        PredicateType.Le));
        Assertions.assertEquals(PredicateType.Lt, PredicateType.minimum(PredicateType.Lt,
                                                                        PredicateType.Lt));
        Assertions.assertEquals(PredicateType.Ge, PredicateType.minimum(PredicateType.Ge,
                                                                        PredicateType.Ge));
        Assertions.assertEquals(PredicateType.Gt, PredicateType.minimum(PredicateType.Gt,
                                                                        PredicateType.Gt));
    }

    @Test
    void testPredicatePreorderLeLt() {
        Assertions.assertEquals(PredicateType.Lt, PredicateType.minimum(PredicateType.Le,
                                                                        PredicateType.Lt));
        Assertions.assertEquals(PredicateType.Lt, PredicateType.minimum(PredicateType.Lt,
                                                                        PredicateType.Le));
    }

    @Test
    void testPredicatePreorderGeGt() {
        Assertions.assertEquals(PredicateType.Gt, PredicateType.minimum(PredicateType.Ge,
                                                                        PredicateType.Gt));
        Assertions.assertEquals(PredicateType.Gt, PredicateType.minimum(PredicateType.Gt,
                                                                        PredicateType.Ge));
    }

    @Test
    void testPredicatePreorderEqLe() {
        Assertions.assertEquals(PredicateType.Eq, PredicateType.minimum(PredicateType.Eq,
                                                                        PredicateType.Le));
        Assertions.assertEquals(PredicateType.Eq, PredicateType.minimum(PredicateType.Le,
                                                                        PredicateType.Eq));
    }

    @Test
    void testPredicatePreorderEqGe() {
        Assertions.assertEquals(PredicateType.Eq, PredicateType.minimum(PredicateType.Eq,
                                                                        PredicateType.Ge));
        Assertions.assertEquals(PredicateType.Eq, PredicateType.minimum(PredicateType.Ge,
                                                                        PredicateType.Eq));
    }

    @Test
    void testPredicateParitalOrderEqual() {
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Eq,
                                                         PredicateType.Eq));
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Ne));
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Le,
                                                         PredicateType.Le));
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Lt,
                                                         PredicateType.Lt));
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Ge,
                                                         PredicateType.Ge));
        Assertions.assertEquals(0, PredicateType.compare(PredicateType.Gt,
                                                         PredicateType.Gt));
    }

    @Test
    void testPredicatePartialOrderEqLe() {
        Assertions.assertEquals(-1, PredicateType.compare(PredicateType.Eq,
                                                          PredicateType.Le));
        Assertions.assertEquals(+1, PredicateType.compare(PredicateType.Le,
                                                          PredicateType.Eq));
    }

    @Test
    void testPredicatePartialOrderEqGe() {
        Assertions.assertEquals(-1, PredicateType.compare(PredicateType.Eq,
                                                          PredicateType.Ge));
        Assertions.assertEquals(+1, PredicateType.compare(PredicateType.Ge,
                                                          PredicateType.Eq));
    }

    @Test
    void testPredicateParitalOrderLeLt() {
        Assertions.assertEquals(-1, PredicateType.compare(PredicateType.Lt,
                                                          PredicateType.Le));
        Assertions.assertEquals(+1, PredicateType.compare(PredicateType.Le,
                                                          PredicateType.Lt));
    }

    @Test
    void testPredicatePartialOrderGeGt() {
        Assertions.assertEquals(-1, PredicateType.compare(PredicateType.Gt,
                                                          PredicateType.Ge));
        Assertions.assertEquals(+1, PredicateType.compare(PredicateType.Ge,
                                                          PredicateType.Gt));
    }

    @Test
    void testPredicatePartialOrderIncomp() {
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                         PredicateType.Lt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                         PredicateType.Eq));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                         PredicateType.Gt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                         PredicateType.Eq));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                         PredicateType.Ne));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Eq));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Lt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Le));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Gt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                         PredicateType.Ge));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                         PredicateType.Ne));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Le,
                                                         PredicateType.Ne));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                         PredicateType.Ne));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ge,
                                                         PredicateType.Ne));
        // Invalid comparisons
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                         PredicateType.Invalid));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                         PredicateType.Eq));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                         PredicateType.Lt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                         PredicateType.Le));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                         PredicateType.Gt));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                         PredicateType.Ge));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                         PredicateType.Invalid));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Le,
                                                         PredicateType.Invalid));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                         PredicateType.Invalid));
        Assertions.assertEquals(2, PredicateType.compare(PredicateType.Ge,
                                                         PredicateType.Invalid));
    }
}
