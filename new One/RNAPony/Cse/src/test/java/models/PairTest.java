package models;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PairTest {

    private static Pair pair1;

    @BeforeAll
    static void setup(){
        pair1 = new Pair(5, 10);
    }

    @Test
    void isPointBetweenInclusive() {
        assertFalse(pair1.isPointBetweenInclusive(3));
        assertFalse(pair1.isPointBetweenInclusive(13));
        assertTrue(pair1.isPointBetweenInclusive(5));
        assertTrue(pair1.isPointBetweenInclusive(10));
        assertTrue(pair1.isPointBetweenInclusive(8));
    }

    @Test
    void isPairInsideExclusive() {
        assertFalse(pair1.isInsideGivenPair(new Pair(1, 2)));
        assertFalse(pair1.isInsideGivenPair(new Pair(1, 5)));
        assertFalse(pair1.isInsideGivenPair(new Pair(5, 6)));
        assertFalse(pair1.isInsideGivenPair(new Pair(1, 6)));
        assertFalse(pair1.isInsideGivenPair(new Pair(6, 7)));
        assertFalse(pair1.isInsideGivenPair(new Pair(7, 10)));
        assertFalse(pair1.isInsideGivenPair(new Pair(10, 15)));
        assertFalse(pair1.isInsideGivenPair(new Pair(7, 15)));
        assertFalse(pair1.isInsideGivenPair(new Pair(11, 15)));
        assertFalse(pair1.isInsideGivenPair(new Pair(5, 10)));
        assertTrue(pair1.isInsideGivenPair(new Pair(4, 11)));
    }
}