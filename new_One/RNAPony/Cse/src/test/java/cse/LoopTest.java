package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;



class LoopTest extends CseFileChecker {
    private static Loop loop;
    @BeforeAll
    static void setUp(){
        loop = new Loop("cse.txt", 0, false);
    }

    @Test
    void bulge() {
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("bulge.dot", "bulge.txt", "bulge.txt", loop);
    }

    @Test
    void bulge1(){
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("bulge1.dot", "bulge1.txt", "bulge1.txt", loop);
    }

    @Test
    void ur15_spinka(){
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("ur15_spinka.dot", "ur15_spinka.txt", "ur15_spinka.txt", loop);
    }

    @Test
    void dinucl_steps(){
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("dinucl_steps.dot", "dinucl_steps.txt", "dinucl_steps.txt", loop);
    }

    @Test
    void ur4_L1_0(){
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("ur4_L1.dot", "ur4_L1_0.txt", "ur4_L1_0.txt", loop);
    }

    @Test
    void ur4_L1_1(){
        loop.setInsertion(1);
        loop.setOpenLoop(false);
        checkFile("ur4_L1.dot", "ur4_L1_1.txt", "ur4_L1_1.txt", loop);
    }

    @Test
    void ur4_L2_0(){
        loop.setInsertion(0);
        loop.setOpenLoop(false);
        checkFile("ur4_L2.dot", "ur4_L2_0.txt", "ur4_L2_0.txt", loop);
    }

    @Test
    void ur4_L2_1(){
        loop.setInsertion(1);
        loop.setOpenLoop(false);
        checkFile("ur4_L2.dot", "ur4_L2_1.txt", "ur4_L2_1.txt", loop);
    }

    @Test
    void bp(){
        loop.setInsertion(0);
        loop.setOpenLoop(true);
        checkFile("bp.dot", "bp.txt", "bp.txt", loop);
    }

    @Test
    void bps2_4(){
        loop.setInsertion(0);
        loop.setOpenLoop(true);
        checkFile("bps2_4.dot", "bps2_4.txt", "bps2_4.txt", loop);
    }

    @Test
    void bps3_4_10_20(){
        loop.setInsertion(0);
        loop.setOpenLoop(true);
        checkFile("bps3_4_10_20.dot", "bps3_4_10_20.txt", "bps3_4_10_20.txt", loop);
    }
}