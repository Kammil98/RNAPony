package cse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LoopTest {
    private final Logger logger = Logger.getLogger(cse.CSETest.class.getName());
    private static Path cppFilePath, javaFilePath, resultsFilesPath;

    @BeforeAll
    public static void setup(){
        resultsFilesPath = Path.of("../", "files", "results");
        CSE.setSaveToFile(true);
    }

    @AfterEach
    public void cleanUp(){
        new File(javaFilePath.toString()).delete();
    }

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

    private void checkFile(String cppFileName, String javaFileName, String sourceFileName, String insertion){
        cppFilePath = Path.of(resultsFilesPath.toString(), "c++", cppFileName);
        javaFilePath = Path.of(resultsFilesPath.toString(), "java", javaFileName);
        CSE.changeLogFile(javaFilePath);
        String[] args = {sourceFileName, "cse.txt", insertion, "0"};
        Loop.main(args);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
    }
    @Test
    void bulge() {
        checkFile("bulge.txt", "bulge.txt",
                "bulge.dot", "0");
    }

    @Test
    void bulge1(){
        checkFile("bulge1.txt", "bulge1.txt",
                "bulge1.dot", "0");
    }

    /*@Test
    void ur15_spinka(){
        checkFile("ur15_spinka.txt", "ur15_spinka.txt",
                    "ur15_spinka.dot", "0");
    }*/

    @Test
    void dinucl_steps(){
        checkFile("dinucl_steps.txt", "dinucl_steps.txt",
                "dinucl_steps.dot", "0");
    }

    @Test
    void ur4_L1_0(){
        checkFile("ur4_L1_0.txt", "ur4_L1_0.txt",
                "ur4_L1.dot", "0");
    }

    @Test
    void ur4_L1_1(){
        checkFile("ur4_L1_1.txt", "ur4_L1_1.txt",
                "ur4_L1.dot", "1");
    }

    @Test
    void ur4_L2_0(){
        checkFile("ur4_L2_0.txt", "ur4_L2_0.txt",
                "ur4_L2.dot", "0");
    }

    @Test
    void ur4_L2_1(){
        checkFile("ur4_L2_1.txt", "ur4_L2_1.txt",
                "ur4_L2.dot", "1");
    }
}