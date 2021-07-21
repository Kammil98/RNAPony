package updater;

import models.DBrecord;
import models.Structure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DBUpdaterTest {

    private static DBUpdater updater;

    @AfterAll
    public static void cleanUp(){
        updater.close();
    }

    @BeforeAll
    public static void init(){
        try {
            updater = new DBUpdater();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    void readRecord() {
        DBrecord dBrecord;
        dBrecord = updater.readRecord("1elh 6 A_B 999.99 UUGCCUGGCGGC;AACUGCCAGGCAU; .((((((((((....)))))))))).. " +
                "0;23;21;19;17;15;13;11;9;7;5;0;0;0;0;-5;-7;-9;-11;-13;-15;-17;-19;-21;-23;0;0 0");
        assertEquals("1elh", dBrecord.getId());
        assertEquals(6, dBrecord.getModelNo());
        assertEquals("A_B", dBrecord.getChain());
        assertEquals(999.99, dBrecord.getResol());
        assertEquals("UUGCCUGGCGGC;AACUGCCAGGCAU;", dBrecord.getSeq());
        assertEquals(".((((((((((....))))))))))..", dBrecord.getDot());
        assertEquals("0;23;21;19;17;15;13;11;9;7;5;0;0;0;0;-5;-7;-9;-11;-13;-15;-17;-19;-21;-23;0;0",
                dBrecord.getDotIntervals());
        assertEquals(0, dBrecord.getMaxOrder());
    }

    @Test
    void readStructure() {
        Structure structure;
        structure = updater.readStructure("1elh  [4, 5, 2, 1, 6, 3]");
        assertEquals("1elh", structure.getId());
        assertArrayEquals(new int[]{4, 5, 2, 1, 6, 3}, structure.getModels());
        structure = updater.readStructure("100d  []");
        assertArrayEquals(new int[]{}, structure.getModels());
        structure = updater.readStructure("1ekd  [1]");
        assertArrayEquals(new int[]{1}, structure.getModels());
        structure = updater.readStructure("1ekd  null");
        assertNull(structure.getModels());
    }
}