package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileChecker {

    private final Logger logger = Logger.getLogger(utils.FileChecker.class.getName());
    protected Path cppFilePath;
    protected Path javaFilePath;

    public void PreparePaths(String cppFileName, String javaFileName, Class resourceClass){
        cppFilePath = Path.of("results", "c++", cppFileName);
        javaFilePath = Path.of("results", "java", javaFileName);
        ClassLoader classLoader = resourceClass.getClassLoader();
        //substring(6), to cut off "file: "
        javaFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6),
                javaFilePath.toString());
        cppFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(6),
                cppFilePath.toString());

        try {//create file and directory if it doesn't exist
            Files.createDirectories(javaFilePath.getParent());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Testing if two files are equal line by line
     * @param filenameCpp path to file with result of cpp program
     * @param filenameJava path to file with result of java program
     * @return true if files are equal, false otherwise
     */
    public boolean isContentEqual(String filenameCpp, String filenameJava){
        try(BufferedReader CppReader = new BufferedReader(new InputStreamReader(new FileInputStream(filenameCpp)));
            BufferedReader javaReader2 = new BufferedReader(new InputStreamReader(new FileInputStream(filenameJava)))){
            for(String line1 = CppReader.readLine(), line2  = javaReader2.readLine(); line1 != null & line2 != null;
                line1 = CppReader.readLine(), line2  = javaReader2.readLine()){
                //need to do .replaceAll("\\.0 "," "), because in java 5 is saved as "5.0" and in c++ as "5"
                String correctedLine2 = line2.replaceAll("\\.0 "," ")
                        .replaceAll("\\s+","")
                        .replaceAll(",", ".");
                if(!line1.replaceAll("\\s+","")
                        .equals(correctedLine2)){
                    logger.log(Level.SEVERE, new StringBuilder("Following lines are not equal:\n\t").append(line1).append("\t").append(line1.length())
                            .append("letters\n\t").append(line2).append("\t").append(line2.length()).append("letters").toString());
                    logger.log(Level.SEVERE, new StringBuilder("After replacing:\n\t").append(line1.replaceAll("\\s+",""))
                            .append("\n\t" + correctedLine2).toString());
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.log(Level.SEVERE, "Didn't found file filenameCpp or filenameJava.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return true;
    }
}

