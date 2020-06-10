package cse;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class LoopTest {
    private final Logger logger = Logger.getLogger(cse.CSETest.class.getName());
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
    @Test
    void main() {
        Path resultsFilesPath = Path.of("../", "files", "results");
        System.out.println(Path.of("./").toAbsolutePath());
        Path cppFilePath = Path.of(resultsFilesPath.toString(), "c++", "bulge.txt"),
                javaFilePath = Path.of(resultsFilesPath.toString(), "java", "bulge.txt");

        String[] args = {"bulge.dot", "cse.txt", "0", "0"};
        CSE.setSaveToFile(true);
        CSE.changeLogFile(javaFilePath);
        Loop.main(args);
        assertTrue(isContentEqual(cppFilePath.toString(), javaFilePath.toString()));
    }
}