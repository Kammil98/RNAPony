package homology;

import utils.FileChecker;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomologyFileChecker extends FileChecker {

    /**
     * Preparing Homology params to test if two files are equal and test it
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file with result of java program
     * @param sourceFileName name of file with data to compute homology
     */
    void checkFile(String cppFileName, String javaFileName, String sourceFileName){
        Homology homology = new Homology();
        Path cppFilePath = Path.of("results", "c++", cppFileName);
        Path javaFilePath = Path.of("results", "java", javaFileName);
        ClassLoader classLoader = homology.getClass().getClassLoader();
        //substring(6), to cut off "file: "
        javaFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6), javaFilePath.toString());
        cppFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6), cppFilePath.toString());

        try {//create file and directory if it doesn't exist
            Files.createDirectories(javaFilePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.changeLogHandler(homology.logger, javaFilePath);
        homology.compute(sourceFileName);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }
}
