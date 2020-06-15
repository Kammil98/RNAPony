package cse;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileChecker {

    private static final Path resultsFilesPath = Path.of("../", "files", "results");
    private final Logger logger = Logger.getLogger(cse.FileChecker.class.getName());

    /**
     * Testing if two files are equal line by line
     * @param filenameCpp path to file with result of cpp program
     * @param filenameJava path to file with result of java program
     * @return true if files are equal, false otherwise
     */
    boolean isContentEqual(String filenameCpp, String filenameJava){
        try(BufferedReader CppReader = new BufferedReader(new InputStreamReader(new FileInputStream(filenameCpp)));
            BufferedReader javaReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(filenameJava)))){
            for(String line1 = CppReader.readLine(), line2  = javaReader2.readLine(); line1 != null & line2 != null;
                line1 = CppReader.readLine(), line2  = javaReader2.readLine()){
                //need to do .replaceAll("\\.0 "," "), because in java 5 is saved as "5.0" and in c++ as "5"
                if(!line1.replaceAll("\\s+","")
                        .equals(line2.replaceAll("\\.0 "," ").replaceAll("\\s+",""))){
                    logger.log(Level.SEVERE, "Following lines are not equal:\n\t" + line1 + "\t" + line1.length() + "letters\n\t" + line2 + "\t" + line2.length() + "letters");
                    logger.log(Level.SEVERE, "After replacing:\n\t" + line1.replaceAll("\\s+","") + "\n\t" +
                            line2.replaceAll("\\.0 "," ").replaceAll("\\s+",""));
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Didn't found file filenameCpp or filenameJava.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Preparing CSE params to test if two files are equal and test it
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file with result of java program
     * @param cse CSE object to find Sequences
     */
    void checkFile(String cppFileName, String javaFileName, CSE cse){
        System.out.println(Path.of(".").toAbsolutePath());
        Path cppFilePath = Path.of(resultsFilesPath.toString(), "c++", cppFileName);
        Path javaFilePath = Path.of(resultsFilesPath.toString(), "java", javaFileName);
        cse.setSaveToFile(true);
        cse.changeLogFile(javaFilePath);
        cse.findSequences();
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        new File(javaFilePath.toString()).deleteOnExit();
    }
}
