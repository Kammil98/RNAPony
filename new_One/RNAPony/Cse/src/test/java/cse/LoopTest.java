package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;


class LoopTest{
    private static CseFileChecker cseFileChecker;
    @BeforeAll
    static void setUp(){
        String dbPath = Objects.requireNonNull(HairpinTest.class.getResource("/cse.txt")).getPath();
        cseFileChecker = new CseFileChecker(new Loop(dbPath, 0, false));
    }

    @Test
    void bulge() {
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("bulge.dot", "bulge.txt", "bulge.txt");
    }

    @Test
    void bulge1(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("bulge1.dot", "bulge1.txt", "bulge1.txt");
    }

    @Test
    void ur15_spinka(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("ur15_spinka.dot", "ur15_spinka.txt", "ur15_spinka.txt");
    }

    @Test
    void dinucl_steps(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("dinucl_steps.dot", "dinucl_steps.txt", "dinucl_steps.txt");
    }

    @Test
    void ur4_L1_0(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("ur4_L1.dot", "ur4_L1_0.txt", "ur4_L1_0.txt");
    }

    @Test
    void ur4_L1_1(){
        cseFileChecker.getCse().setInsertion(1);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("ur4_L1.dot", "ur4_L1_1.txt", "ur4_L1_1.txt");
    }

    @Test
    void ur4_L2_0(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("ur4_L2.dot", "ur4_L2_0.txt", "ur4_L2_0.txt");
    }

    @Test
    void ur4_L2_1(){
        cseFileChecker.getCse().setInsertion(1);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(false);
        cseFileChecker.checkFile("ur4_L2.dot", "ur4_L2_1.txt", "ur4_L2_1.txt");
    }

    @Test
    void bp(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(true);
        cseFileChecker.checkFile("bp.dot", "bp.txt", "bp.txt");
    }

    @Test
    void bps2_4(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(true);
        cseFileChecker.checkFile("bps2_4.dot", "bps2_4.txt", "bps2_4.txt");
    }

    @Test
    void bps3_4_10_20(){
        cseFileChecker.getCse().setInsertion(0);
        ((Loop)cseFileChecker.getCse()).setOpenLoop(true);
        cseFileChecker.checkFile("bps3_4_10_20.dot", "bps3_4_10_20.txt", "bps3_4_10_20.txt");
    }
}