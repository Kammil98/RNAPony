package utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class FileChecker implements ComparsionFiles{

    private final Logger logger = Logger.getLogger(utils.FileChecker.class.getName());
    protected Path cppFilePath;
    protected Path javaFilePath;

    public abstract void checkFile(String sourceFileName, String cppFileName, String javaFileName);

    public int getSystemPrefixLen(){
        if (Path.of(".").getFileSystem().getSeparator().equals("\\"))//if this is windows
            return 6;
        else //if this is linux
            return 5;
    }
    protected void PreparePaths(String cppFileName, String javaFileName, Class resourceClass){
        int sysPrefixLen = getSystemPrefixLen();
        cppFilePath = Path.of("results", "c++", cppFileName);
        javaFilePath = Path.of("results", "java", javaFileName);
        ClassLoader classLoader = resourceClass.getClassLoader();
        //substring(6), to cut off "file: "
        javaFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(sysPrefixLen),
                javaFilePath.toString());
        cppFilePath = Path.of(Objects.requireNonNull(classLoader.getResource(".")).toString().substring(sysPrefixLen),
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
                    logger.log(Level.SEVERE, "Following lines are not equal:\n\t" + line1 + "\t" + line1.length()
                            + "letters\n\t" + line2 + "\t" + line2.length() + "letters");
                    logger.log(Level.SEVERE, "After replacing:\n\t" + line1.replaceAll("\\s+", "")
                            + "\n\t" + correctedLine2);
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

