package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class HairpinTest {
    private final Logger logger = Logger.getLogger(cse.CSETest.class.getName());
    private static Path resultsFilesPath;

    @BeforeAll
    public static void setup(){
        resultsFilesPath = Path.of("../", "files", "results");
        CSE.setSaveToFile(true);
    }

    /**
     * Testing if two files are equal line by line
     * @param filenameCpp path to file with result of cpp program
     * @param filenameJava path to file with result of java program
     * @return true if files are equal, false otherwise
     */
    private boolean isContentEqual(String filenameCpp, String filenameJava){
        try(BufferedReader CppReader = new BufferedReader(new InputStreamReader(new FileInputStream(filenameCpp)));
            BufferedReader javaReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(filenameJava)))){
            for(String line1 = CppReader.readLine(), line2  = javaReader2.readLine(); line1 != null & line2 != null;
                line1 = CppReader.readLine(), line2  = javaReader2.readLine()){
                //need to do .replaceAll("\\.0 "," "), because in java 5 is saved as "5.0" and in c++ as "5"
                if(!line1.replaceAll("\\s+","")
                        .equals(line2.replaceAll("\\.0 "," ").replaceAll("\\s+",""))){
                    logger.log(Level.SEVERE, "Following lines are not equal:\n\t" + line1 + line1.length() + "\n\t" + line2 + line2.length());
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
     */
    private void checkFile(){
        Path cppFilePath = Path.of(resultsFilesPath.toString(), "c++", "hairpin.txt");
        Path javaFilePath = Path.of(resultsFilesPath.toString(), "java", "hairpin.txt");
        CSE.changeLogFile(javaFilePath);
        String[] args = {"hairpin.dot", "cse.txt", "0"};
        Hairpin.main(args);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        if(!new File(javaFilePath.toString()).delete()){
            logger.warning("Couldn't delete file at: " + javaFilePath.toString());
        }
    }

    @Test
    void hairpin() {
        checkFile(
        );
    }
}