package cse;

import utils.FileChecker;
import utils.Utils;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CseFileChecker extends FileChecker {

    /**
     * Preparing CSE params to test if two files are equal and test it
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file with result of java program
     * @param cse CSE object to find Sequences
     */
    protected void checkFile( String sourceFile, String cppFileName, String javaFileName, CSE cse){
        PreparePaths(cppFileName, javaFileName, CseFileChecker.class);
        Utils.changeLogHandler(cse.logger, javaFilePath);
        cse.compute(sourceFile);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }
}
