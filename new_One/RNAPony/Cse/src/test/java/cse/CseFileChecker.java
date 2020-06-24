package cse;

import utils.FileChecker;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CseFileChecker extends FileChecker {

    private final Logger logger = Logger.getLogger(CseFileChecker.class.getName());

    /**
     * Preparing CSE params to test if two files are equal and test it
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file with result of java program
     * @param cse CSE object to find Sequences
     */
    protected void checkFile(String cppFileName, String javaFileName, CSE cse){
        System.out.println(Path.of(".").toAbsolutePath());

        Path cppFilePath = Path.of("results", "c++", cppFileName);
        Path javaFilePath = Path.of("results", "java", javaFileName);
        ClassLoader classLoader = cse.getClass().getClassLoader();
        //substring(6), to cut off "file: "
        javaFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6), javaFilePath.toString());
        cppFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6), cppFilePath.toString());

        try {//create file and directory if it doesn't exist
            Files.createDirectories(javaFilePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.changeLogHandler(cse.logger, javaFilePath);
        cse.findSequences();
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }
}
