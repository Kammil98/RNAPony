package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HairpinTest{
    private static CseFileChecker cseFileChecker;
    @BeforeAll
    static void setUp(){
        cseFileChecker = new CseFileChecker(new Hairpin( "cse.txt", 0));
    }
    @Test
    void hairpin() {
        cseFileChecker.checkFile("hairpin.dot", "hairpin.txt", "hairpin.txt");
    }
}