package cse;

import csemodels.Pair;
import models.Sequence;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CSETest {

    private CSE cse;

    @BeforeEach
    public void init(){
        cse = new Hairpin("hairpin.dot", "cse.txt", 0);
    }

    @Test
    void readMpSeq() {
        Sequence expectedSeq = new Sequence();
        String msg = "Inapropriate reading hairpion.dot file";
        expectedSeq.setName("hairpin");
        expectedSeq.setSeq("CAGCGUCAAGCCCCGGCUUGCUG");
        expectedSeq.setTop("((((....((.[[[[[)).))))");
        cse.readMpSeq("hairpin.dot");
        assertEquals(expectedSeq.getName(), cse.getSourceSequence().getName(), msg);
        assertEquals(expectedSeq.getSeq(), cse.getSourceSequence().getSeq(), msg);
        assertEquals(expectedSeq.getTop(), cse.getSourceSequence().getTop(), msg);
    }

    @Test
    void readDataBase() {
        Sequence expectedSeq0, expectedSeq4;
        String msg;
        expectedSeq0 = new Sequence("157d", "A_B", 2.38d, "CGCGAAUUAGCG;CGCGAAUUAGCG;",
                "(((.((((.(((.))).)))).))).", "24;22;20;0;16;14;12;10;0;6;4;2;0;-2;-4;-6;0;-10;-12;" +
                "-14;-16;0;-20;-22;-24;0",0);
        expectedSeq4 = new Sequence("1a3m", "A_B", 999.99d, "GGCGUCACACCUUC;GGGUGAAGUCGCC;",
                "((((.(.((((.....))))..).)))).", "27;25;23;21;0;17;0;12;10;8;6;0;0;0;0;0;-6;-8;-10;-12;0;0;" +
                "-17;0;-21;-23;-25;-27;0",0);
        cse.readDataBase("cse.txt");
        msg = "Inapropriate reading first sequence from cse.txt file";
        assertEquals(expectedSeq0.toString(), cse.getSequences().get(0).toString(), msg);
        msg = "Inapropriate reading nth sequence from cse.txt file";
        assertEquals(expectedSeq4.toString(), cse.getSequences().get(4).toString(), msg);
    }

    @Test
    void isOk() {
        ArrayList<Pair> pairs = new ArrayList<>();
        Pair checkedPair = new Pair(5, 10);
        Pair collisionPair;
        pairs.add(new Pair(1, 2));pairs.add(new Pair(11, 12));
        String msg;

        assertTrue(cse.isOk(pairs, checkedPair));
        collisionPair = new Pair(1, 5);
        pairs.add(collisionPair);
        msg = "Mistake for collisionPair = (1, 5)";
        assertFalse(cse.isOk(pairs, checkedPair), msg);
        pairs.remove(collisionPair);

        collisionPair = new Pair(10, 11);
        pairs.add(collisionPair);
        msg = "Mistake for collisionPair = (10, 11)";
        assertFalse(cse.isOk(pairs, checkedPair), msg);
        pairs.remove(collisionPair);

        collisionPair = new Pair(1, 15);
        pairs.add(collisionPair);
        msg = "Mistake for collisionPair = (1, 15)";
        assertFalse(cse.isOk(pairs, checkedPair), msg);
        pairs.remove(collisionPair);

        collisionPair = new Pair(6, 9);
        pairs.add(collisionPair);
        msg = "Mistake for collisionPair = (6, 9)";
        assertFalse(cse.isOk(pairs, checkedPair), msg);
        pairs.remove(collisionPair);
    }

    @Test
    void createArray() {
        String sequence = "GCUGGGCGCAGG;GCUGGGCGCAGG;CCUGACGGUACAGC;CCUGACGGUACAGC;";
        ArrayList<String> actual = cse.createArray(sequence);
        ArrayList<String> expected = new ArrayList<>();
        expected.add("GCUGGGCGCAGG");
        expected.add("GCUGGGCGCAGG");
        expected.add("CCUGACGGUACAGC");
        expected.add("CCUGACGGUACAGC");
        assertArrayEquals(expected.toArray(), actual.toArray());

        expected.clear();
        expected.add("GCCAGGAUGUAGGCUUAGAAGCAGCCAUCAUUUAAAGAAAGCGUAAUAGCUCACUGGU");
        sequence = "GCCAGGAUGUAGGCUUAGAAGCAGCCAUCAUUUAAAGAAAGCGUAAUAGCUCACUGGU;";
        actual = cse.createArray(sequence);
        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    void createArrayInt() {
        String sequence = "0;0;0;0;0;0;15;13;11;0;19;21;19;0;0;0;0;10;7;" +
                "-11;-13;-15;0;0;0;-7;0;-10;0;-19;0;-19;-21;0;0;0";
        Integer[] expected = {0,0,0,0,0,0,15,13,11,0,19,21,19,0,0,0,0,10,7,
                -11,-13,-15,0,0,0,-7,0,-10,0,-19,0,-19,-21,0,0,0};
        ArrayList<Integer> actual = cse.createArrayInt(sequence);
        assertArrayEquals(expected, actual.toArray());
    }
}