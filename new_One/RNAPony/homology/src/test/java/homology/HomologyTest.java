package homology;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HomologyTest{
    private static HomologyFileChecker homologyFileChecker;
    @BeforeAll
    static void setUp(){
        homologyFileChecker = new HomologyFileChecker();
    }
    @Test
    void compute() {
        homologyFileChecker.checkFile("ur4_L1_0.txt","homology.txt", "homology.txt");
    }

}