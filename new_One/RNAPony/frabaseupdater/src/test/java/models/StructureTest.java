package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructureTest {

    @Test
    void valueOf() {
        Structure structure;
        structure = Structure.valueOf("1elh  [4, 5, 2, 1, 6, 3]");
        assertEquals("1elh", structure.getId());
        assertArrayEquals(new int[]{4, 5, 2, 1, 6, 3}, structure.getModels());
        structure = Structure.valueOf("100d  []");
        assertArrayEquals(new int[]{}, structure.getModels());
        structure = Structure.valueOf("1ekd  [1]");
        assertArrayEquals(new int[]{1}, structure.getModels());
        structure = Structure.valueOf("1ekd  null");
        assertNull(structure.getModels());
    }
}