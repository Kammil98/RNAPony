package homology;

import utils.FileChecker;
import utils.Utils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomologyFileChecker extends FileChecker {

    /**
     * Preparing Homology params to test if two files are equal and test it
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file to save result of java program
     * @param sourceFileName name of file with data to compute homology
     */
    protected void checkFile(String sourceFileName, String cppFileName, String javaFileName){
        Homology homology = new Homology();
        PreparePaths(cppFileName, javaFileName, HomologyFileChecker.class);

        Utils.changeLogHandler(homology.logger, javaFilePath);
        homology.compute(sourceFileName);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }

}
