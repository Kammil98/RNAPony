package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Objects;

class HairpinTest{
    private static CseFileChecker cseFileChecker;

    @BeforeAll
    static void setUp(){
        String dbPath = Objects.requireNonNull(HairpinTest.class.getResource("/cse.txt")).getPath();
        cseFileChecker = new CseFileChecker(new Hairpin( dbPath, 0));
    }

    @Test
    void hairpin() {
        cseFileChecker.checkFile("hairpin.dot", "hairpin.txt", "hairpin.txt");
    }
}