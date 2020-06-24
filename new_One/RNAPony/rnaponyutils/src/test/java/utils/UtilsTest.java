package utils;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void createArray() {
        String sequence = "GCUGGGCGCAGG;GCUGGGCGCAGG;CCUGACGGUACAGC;CCUGACGGUACAGC;";
        ArrayList<String> actual = Utils.createArray(sequence, ";");
        ArrayList<String> expected = new ArrayList<>();
        expected.add("GCUGGGCGCAGG");
        expected.add("GCUGGGCGCAGG");
        expected.add("CCUGACGGUACAGC");
        expected.add("CCUGACGGUACAGC");
        assertArrayEquals(expected.toArray(), actual.toArray());

        expected.clear();
        expected.add("GCCAGGAUGUAGGCUUAGAAGCAGCCAUCAUUUAAAGAAAGCGUAAUAGCUCACUGGU");
        sequence = "GCCAGGAUGUAGGCUUAGAAGCAGCCAUCAUUUAAAGAAAGCGUAAUAGCUCACUGGU;";
        actual = Utils.createArray(sequence, ";");
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    void createArrayInt() {
        String sequence = "0;0;0;0;0;0;15;13;11;0;19;21;19;0;0;0;0;10;7;" +
                "-11;-13;-15;0;0;0;-7;0;-10;0;-19;0;-19;-21;0;0;0";
        Integer[] expected = {0,0,0,0,0,0,15,13,11,0,19,21,19,0,0,0,0,10,7,
                -11,-13,-15,0,0,0,-7,0,-10,0,-19,0,-19,-21,0,0,0};
        ArrayList<Integer> actual = Utils.createArrayInt(sequence, ";");
        assertArrayEquals(expected, actual.toArray());
    }

}