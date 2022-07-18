package util;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ConfigurationTest {

    @Test
    void testGetBoolean() {
        Optional<Boolean> result = Configuration.getBoolean("OutputMinimizedZoneStates");
        assertAll(() -> assertTrue(result.isPresent()),
                  () -> assertEquals(Optional.of(true), result));
    }
}
