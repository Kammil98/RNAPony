package cse;

import org.junit.jupiter.api.Test;



class LoopTest extends CseFileChecker {
    private Loop loop;

    @Test
    void bulge() {
        loop = new Loop("bulge.dot", "cse.txt", 0, false);
        checkFile("bulge.txt", "bulge.txt", loop);
    }

    @Test
    void bulge1(){
        loop = new Loop("bulge1.dot", "cse.txt", 0, false);
        checkFile("bulge1.txt", "bulge1.txt", loop);
    }

    @Test
    void ur15_spinka(){
        loop = new Loop("ur15_spinka.dot", "cse.txt", 0, false);
        checkFile("ur15_spinka.txt", "ur15_spinka.txt", loop);
    }

    @Test
    void dinucl_steps(){
        loop = new Loop("dinucl_steps.dot", "cse.txt", 0, false);
        checkFile("dinucl_steps.txt", "dinucl_steps.txt", loop);
    }

    @Test
    void ur4_L1_0(){
        loop = new Loop("ur4_L1.dot", "cse.txt", 0, false);
        checkFile("ur4_L1_0.txt", "ur4_L1_0.txt", loop);
    }

    @Test
    void ur4_L1_1(){
        loop = new Loop("ur4_L1.dot", "cse.txt", 1, false);
        checkFile("ur4_L1_1.txt", "ur4_L1_1.txt", loop);
    }

    @Test
    void ur4_L2_0(){
        loop = new Loop("ur4_L2.dot", "cse.txt", 0, false);
        checkFile("ur4_L2_0.txt", "ur4_L2_0.txt", loop);
    }

    @Test
    void ur4_L2_1(){
        loop = new Loop("ur4_L2.dot", "cse.txt", 1, false);
        checkFile("ur4_L2_1.txt", "ur4_L2_1.txt", loop);
    }

    @Test
    void bp(){
        loop = new Loop("bp.dot", "cse.txt", 0, true);
        checkFile("bp.txt", "bp.txt", loop);
    }

    @Test
    void bps2_4(){
        loop = new Loop("bps2_4.dot", "cse.txt", 0, true);
        checkFile("bps2_4.txt", "bps2_4.txt", loop);
    }

    @Test
    void bps3_4_10_20(){
        loop = new Loop("bps3_4_10_20.dot", "cse.txt", 0, true);
        checkFile("bps3_4_10_20.txt", "bps3_4_10_20.txt", loop);
    }
}