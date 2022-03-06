package functional;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ContextTest {

    @Test
    void contextStartupTest() {
        assertTrue(true);
    }
}
