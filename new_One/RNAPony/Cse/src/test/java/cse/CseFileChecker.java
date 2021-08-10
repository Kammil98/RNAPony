package cse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import utils.FileChecker;
import utils.Utils;

import java.io.*;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor
public class CseFileChecker extends FileChecker {
    @Getter
    private final CSE cse;

    /**
     * Preparing CSE params to test if two files are equal and test it
     * @param sourceFileName name of file with sequence
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file to save result of java program
     */
    @Override
    public void checkFile(String sourceFileName, String cppFileName, String javaFileName){
        PreparePaths(cppFileName, javaFileName, CseFileChecker.class);
        Utils.changeLogHandler(CSE.logger, javaFilePath);
        cse.compute(Objects.requireNonNull(CseFileChecker.class.getResource("/" + sourceFileName)).getPath());
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }

}
