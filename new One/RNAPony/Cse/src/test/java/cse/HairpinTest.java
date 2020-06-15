package cse;

import org.junit.jupiter.api.Test;

class HairpinTest extends FileChecker{
    //private static Path resultsFilesPath = Path.of("../", "files", "results");
    @Test
    void hairpin() {
        Hairpin hairpin = new Hairpin("hairpin.dot", "cse.txt", 0);
        checkFile("hairpin.txt", "hairpin.txt", hairpin);
    }
}