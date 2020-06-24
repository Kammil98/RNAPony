package homology;

import org.junit.jupiter.api.Test;

class HomologyTest extends HomologyFileChecker {

    @Test
    void compute() {
        checkFile("homology.txt", "homology.txt", "ur4_L1_0.txt");
    }

}