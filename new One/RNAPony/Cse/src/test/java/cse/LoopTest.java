package cse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LoopTest {
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
     * @param cppFileName name of file with result of cpp program
     * @param javaFileName name of file with result of java program
     * @param sourceFileName name of file with source Sequence
     * @param insertion number of insertions
     */
    private void checkFile(String cppFileName, String javaFileName, String sourceFileName, String insertion, String loopOpen){
        Path cppFilePath = Path.of(resultsFilesPath.toString(), "c++", cppFileName);
        Path javaFilePath = Path.of(resultsFilesPath.toString(), "java", javaFileName);
        CSE.changeLogFile(javaFilePath);
        String[] args = {sourceFileName, "cse.txt", insertion, loopOpen};
        Loop.main(args);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
        if(!new File(javaFilePath.toString()).delete()){
            logger.warning("Couldn't delete file at: " + javaFilePath.toString());
        }
    }

    @Test
    void bulge() {
        checkFile("bulge.txt", "bulge.txt",
                "bulge.dot", "0", "0");
    }

    @Test
    void bulge1(){
        checkFile("bulge1.txt", "bulge1.txt",
                "bulge1.dot", "0", "0");
    }

    /*@Test
    void ur15_spinka(){
        checkFile("ur15_spinka.txt", "ur15_spinka.txt",
                    "ur15_spinka.dot", "0", "0");
    }*/

    @Test
    void dinucl_steps(){
        checkFile("dinucl_steps.txt", "dinucl_steps.txt",
                "dinucl_steps.dot", "0", "0");
    }

    @Test
    void ur4_L1_0(){
        checkFile("ur4_L1_0.txt", "ur4_L1_0.txt",
                "ur4_L1.dot", "0", "0");
    }

    @Test
    void ur4_L1_1(){
        checkFile("ur4_L1_1.txt", "ur4_L1_1.txt",
                "ur4_L1.dot", "1", "0");
    }

    @Test
    void ur4_L2_0(){
        checkFile("ur4_L2_0.txt", "ur4_L2_0.txt",
                "ur4_L2.dot", "0", "0");
    }

    @Test
    void ur4_L2_1(){
        checkFile("ur4_L2_1.txt", "ur4_L2_1.txt",
                "ur4_L2.dot", "1", "0");
    }

    @Test
    void bp(){
        checkFile("bp.txt", "bp.txt",
                "bp.dot", "0", "1");
    }

    @Test
    void bps2_4(){
        checkFile("bps2_4.txt", "bps2_4.txt",
                "bps2_4.dot", "0", "1");
    }

    @Test
    void bps3_4_10_20(){
        checkFile("bps3_4_10_20.txt", "bps3_4_10_20.txt",
                "bps3_4_10_20.dot", "0", "1");
    }
}