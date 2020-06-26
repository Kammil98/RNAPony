package utils;

public interface ComparsionFiles {
    /**
     * Compare two files at given Paths
     * @param filenameCpp Path to file with cpp output of computations
     * @param filenameJava Path to file with java output of computations
     * @return true if files are equal, false otherwise
     */
    boolean isContentEqual(String filenameCpp, String filenameJava);

    /**
     * create output file for java program and compare it, to output of c++ program
     * @param sourceFileName file with data to calculate output
     * @param cppFileName file with c++ output
     * @param javaFileName file to save java output in
     */
    void checkFile(String sourceFileName, String cppFileName, String javaFileName);
}
