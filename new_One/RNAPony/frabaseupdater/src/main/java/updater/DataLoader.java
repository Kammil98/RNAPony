package updater;

import models.DBrecord;
import models.DotFile;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class DataLoader {

    private static final Path pdbeeOutDir = Path.of(Main.frabaseDir.getFileName().toString(),"output");

    private void removeLogs(final String directory) throws IOException {
        String command;
        command = "rm " + directory.toString() + " *.log";
        Runtime.getRuntime().exec(command);
    }

    /**
     * Create Dot file with use of RNApdbee.
     * @param filePath path to 3D file.
     */
    private void createDotFile(final Path filePath){
        Process proc;
        String command, inputFilePath = filePath.toAbsolutePath().toString();
        File outDir = new File(pdbeeOutDir.toString());
        InputStream err;
        try {
            if(!outDir.exists())
                outDir.mkdirs();
            URL rnapdbeUrl = getClass().getResource("/rnapdbee/rnapdbee");
            command = rnapdbeUrl.getPath()
                    + " -a DSSR -d NONE -i " + inputFilePath
                    + " -o " + outDir.getAbsolutePath()
                    + " -p HYBRID";
            proc = Runtime.getRuntime().exec(command);
            //err = proc.getErrorStream();
            proc.waitFor();
        } catch (IOException e) {
            Main.errLogger.severe("Unexpected IOException: \n" + e.getMessage());
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read .dot file and save it to DotFile Object.
     * @param filePath path to file with 2D representation of structure.
     * @return DotFile Object, which represent given dot file.
     */
    private DotFile readDotFile(final Path filePath){
        String header;
        StringBuilder name = new StringBuilder(), seq = new StringBuilder(), dot = new StringBuilder();
        try(Scanner dotReader = new Scanner(new File(filePath.toString()))){
            while (dotReader.hasNextLine()){
                header = dotReader.nextLine();
                name.append(header.substring(7));
                seq.append(dotReader.nextLine().toUpperCase()).append(';');
                dot.append(dotReader.nextLine()).append('.');
            }
            name.deleteCharAt(0);
        } catch (FileNotFoundException e) {
            Main.errLogger.severe("Couldn't find output .dbn file: " + filePath);
        }
        return new DotFile(name.toString(), seq.toString(), dot.toString());
    }

    public DotFile getDotFile(final Path filePath){
        Main.stdLogger.info("Prepare dot file: run RNApdbee for " + filePath.getFileName());
        createDotFile(filePath);
        DotFile dotFile = readDotFile(Path.of(pdbeeOutDir.toString(), "0", "strands.dbn"));
        return dotFile;
    }
}
