package utils;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileChecker {

    private final Logger logger = Logger.getLogger(utils.FileChecker.class.getName());

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
                    logger.log(Level.SEVERE, "Following lines are not equal:\n\t" + line1 + "\t" + line1.length() + "letters\n\t" + line2 + "\t" + line2.length() + "letters");
                    logger.log(Level.SEVERE, "After replacing:\n\t" + line1.replaceAll("\\s+","") + "\n\t" +
                            correctedLine2);
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
}

