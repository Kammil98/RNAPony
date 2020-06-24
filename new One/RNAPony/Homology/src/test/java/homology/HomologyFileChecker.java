package homology;

import cse.CSE;
import utils.FileChecker;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

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
        System.out.println(Path.of(".").toAbsolutePath());
        Path cppFilePath = Path.of("src", "test", "resources", "results", "c++", cppFileName);
        Path javaFilePath = Path.of("src", "test", "resources", "results", "java", javaFileName);
        ClassLoader classLoader = homology.getClass().getClassLoader();
        //substring(6), to cut off "file: "
        //javaFilePath = Path.of(classLoader.getResource(".").toString().substring(6), javaFilePath.toString());
        //cppFilePath = Path.of(classLoader.getResource(".").toString().substring(6), cppFilePath.toString());

        /*File javaFile = new File(javaFilePath.toString());
        try {//create file if it doesn't exist
            System.out.println(javaFilePath.getParent().toString());
            Files.createDirectories(javaFilePath.getParent());
            javaFile.createNewFile();
        } catch (IOException e) {
            System.out.println("tutaj");
            e.printStackTrace();
        }*/
        Utils.changeLogHandler(homology.logger, javaFilePath);
        homology.compute(sourceFileName);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        //new File(javaFilePath.toString()).deleteOnExit();
    }
}
