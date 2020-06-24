package homology;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HomologyTest extends HomologyFileChecker {

    @Test
    void homology() {
        checkFile("homology.txt", "homology.txt", "ur4_L1_0.txt");
    }
}