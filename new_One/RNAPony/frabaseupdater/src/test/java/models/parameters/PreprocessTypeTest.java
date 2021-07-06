package models.parameters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreprocessTypeTest {

    @Test
    void testEquals() {
        PreprocessType type = PreprocessType.ALL;
        assertTrue(type.equals("all"));
        type = PreprocessType.FIRST;
        assertTrue(type.equals("first"));
    }

    @Test
    void testToString() {
        PreprocessType type = PreprocessType.ALL;
        assertEquals("all", type.toString());
        type = PreprocessType.FIRST;
        assertEquals("first", type.toString());
        assertNotEquals("First", type.toString());
    }
}