package abstractinterp.scalar.state;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class PredicateTypeTest {

    @Test
    void testPredicateTypeRotation() {
        assertAll(() -> assertEquals(PredicateType.Invalid, PredicateType.Invalid.rotate()),
                  () -> assertEquals(PredicateType.Ne, PredicateType.Eq.rotate()),
                  () -> assertEquals(PredicateType.Eq, PredicateType.Ne.rotate()),
                  () -> assertEquals(PredicateType.Le, PredicateType.Gt.rotate()),
                  () -> assertEquals(PredicateType.Lt, PredicateType.Ge.rotate()),
                  () -> assertEquals(PredicateType.Ge, PredicateType.Lt.rotate()),
                  () -> assertEquals(PredicateType.Gt, PredicateType.Le.rotate()));
    }

    @Test
    void testPredicateTypeNegation() {
        assertAll(() -> assertEquals(PredicateType.Invalid, PredicateType.Invalid.negate()),
                  () -> assertEquals(PredicateType.Le, PredicateType.Ge.negate()),
                  () -> assertEquals(PredicateType.Ge, PredicateType.Le.negate()),
                  () -> assertEquals(PredicateType.Lt, PredicateType.Gt.negate()),
                  () -> assertEquals(PredicateType.Gt, PredicateType.Lt.negate()),
                  () -> assertEquals(PredicateType.Eq, PredicateType.Eq.negate()),
                  () -> assertEquals(PredicateType.Ne, PredicateType.Ne.negate()));
    }

    @Test
    void testPredicatePreorderEquality() {
        assertAll(() -> assertEquals(PredicateType.Eq,
                                     PredicateType.minimum(PredicateType.Eq,
                                                           PredicateType.Eq)),
                  () -> assertEquals(PredicateType.Ne,
                                     PredicateType.minimum(PredicateType.Ne,
                                                           PredicateType.Ne)),
                  () -> assertEquals(PredicateType.Le,
                                     PredicateType.minimum(PredicateType.Le,
                                                           PredicateType.Le)),
                  () -> assertEquals(PredicateType.Lt,
                                     PredicateType.minimum(PredicateType.Lt,
                                                           PredicateType.Lt)),
                  () -> assertEquals(PredicateType.Ge,
                                     PredicateType.minimum(PredicateType.Ge,
                                                           PredicateType.Ge)),
                  () -> assertEquals(PredicateType.Gt,
                                     PredicateType.minimum(PredicateType.Gt,
                                                           PredicateType.Gt)));
    }

    @Test
    void testPredicatePreorderLeLt() {
        assertAll(() -> assertEquals(PredicateType.Lt,
                                     PredicateType.minimum(PredicateType.Le,
                                                           PredicateType.Lt)),
                  () -> assertEquals(PredicateType.Lt,
                                     PredicateType.minimum(PredicateType.Lt,
                                                           PredicateType.Le)));
    }

    @Test
    void testPredicatePreorderGeGt() {
        assertAll(() -> assertEquals(PredicateType.Gt,
                                     PredicateType.minimum(PredicateType.Ge,
                                                           PredicateType.Gt)),
                  () -> assertEquals(PredicateType.Gt,
                                     PredicateType.minimum(PredicateType.Gt,
                                                           PredicateType.Ge)));
    }

    @Test
    void testPredicatePreorderEqLe() {
        assertAll(() -> assertEquals(PredicateType.Eq,
                                     PredicateType.minimum(PredicateType.Eq,
                                                           PredicateType.Le)),
                  () -> assertEquals(PredicateType.Eq,
                                     PredicateType.minimum(PredicateType.Le,
                                                           PredicateType.Eq)));
    }

    @Test
    void testPredicatePreorderEqGe() {
        assertAll(() -> assertEquals(PredicateType.Eq,
                                     PredicateType.minimum(PredicateType.Eq,
                                                           PredicateType.Ge)),
                  () -> assertEquals(PredicateType.Eq,
                                     PredicateType.minimum(PredicateType.Ge,
                                                           PredicateType.Eq)));
    }

    @Test
    void testPredicateParitalOrderEqual() {
        assertAll(() -> assertEquals(0, PredicateType.compare(PredicateType.Eq,
                                                              PredicateType.Eq)),
                  () -> assertEquals(0, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Ne)),
                  () -> assertEquals(0, PredicateType.compare(PredicateType.Le,
                                                              PredicateType.Le)),
                  () -> assertEquals(0, PredicateType.compare(PredicateType.Lt,
                                                              PredicateType.Lt)),
                  () -> assertEquals(0, PredicateType.compare(PredicateType.Ge,
                                                              PredicateType.Ge)),
                  () -> assertEquals(0, PredicateType.compare(PredicateType.Gt,
                                                              PredicateType.Gt)));
    }

    @Test
    void testPredicatePartialOrderEqLe() {
        assertAll(() -> assertEquals(-1, PredicateType.compare(PredicateType.Eq,
                                                               PredicateType.Le)),
                  () -> assertEquals(+1, PredicateType.compare(PredicateType.Le,
                                                               PredicateType.Eq)));
    }

    @Test
    void testPredicatePartialOrderEqGe() {
        assertAll(() -> assertEquals(-1, PredicateType.compare(PredicateType.Eq,
                                                               PredicateType.Ge)),
                  () -> assertEquals(+1, PredicateType.compare(PredicateType.Ge,
                                                               PredicateType.Eq)));
    }

    @Test
    void testPredicateParitalOrderLeLt() {
        assertAll(() -> assertEquals(-1, PredicateType.compare(PredicateType.Lt,
                                                               PredicateType.Le)),
                  () -> assertEquals(+1, PredicateType.compare(PredicateType.Le,
                                                               PredicateType.Lt)));
    }

    @Test
    void testPredicatePartialOrderGeGt() {
        assertAll(() -> assertEquals(-1, PredicateType.compare(PredicateType.Gt,
                                                               PredicateType.Ge)),
                  () -> assertEquals(+1, PredicateType.compare(PredicateType.Ge,
                                                               PredicateType.Gt)));
    }

    @Test
    void testPredicatePartialOrderIncomp() {
        assertAll(() -> assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                              PredicateType.Lt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                              PredicateType.Eq)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                              PredicateType.Gt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                              PredicateType.Eq)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                              PredicateType.Ne)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Eq)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Lt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Le)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Gt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ne,
                                                              PredicateType.Ge)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                              PredicateType.Ne)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Le,
                                                              PredicateType.Ne)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                              PredicateType.Ne)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ge,
                                                              PredicateType.Ne)),
                  // Invalid comparisons
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Eq,
                                                              PredicateType.Invalid)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                              PredicateType.Eq)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                              PredicateType.Lt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                              PredicateType.Le)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                              PredicateType.Gt)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Invalid,
                                                              PredicateType.Ge)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Lt,
                                                              PredicateType.Invalid)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Le,
                                                              PredicateType.Invalid)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Gt,
                                                              PredicateType.Invalid)),
                  () -> assertEquals(2, PredicateType.compare(PredicateType.Ge,
                                                              PredicateType.Invalid)));
    }
}
