package updater;

import models.DBrecord;
import models.DotFile;
import utils.Utils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class DotFileCreator {

    private static final Path pdbeeOutDir = Path.of(Main.frabaseDir.getFileName().toString(),"output");

    private void removeLogs(final String directory) throws IOException {
        String command;
        command = "rm " + directory.toString() + " *.log";
        Runtime.getRuntime().exec(command);
    }

    /**
     * Create Dot file with use of RNApdbee.
     * @param filePath path to 3D file.
     * @return directory, where output of RNApdbee was written
     */
    private Path createDotFile(final Path filePath){
        Process proc;
        String command, inputFilePath = filePath.toAbsolutePath().toString();
        Path outDir = Path.of(pdbeeOutDir.toString(), String.valueOf(Thread.currentThread().getId()));
        InputStream err;
        try {
            Utils.createDirIfNotExist(outDir.toFile(), true, Main.stdLogger, Main.errLogger);

            URL rnapdbeUrl = getClass().getResource("/rnapdbee/rnapdbee");
            command = rnapdbeUrl.getPath()
                    + " -a DSSR -d NONE -i " + inputFilePath
                    + " -o " + outDir.toAbsolutePath()
                    + " -p HYBRID";
            proc = Runtime.getRuntime().exec(command);
            //err = proc.getErrorStream();
            proc.waitFor();
        } catch (IOException e) {
            Main.errLogger.severe("Unexpected IOException: \n" + e.getMessage());
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return outDir;
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
        Path outDir;
        DotFile dotFile;
        if(Main.getVerboseMode() >= 3)
            Main.stdLogger.info("Prepare dot file: run RNApdbee for " + filePath.getFileName());
        outDir = createDotFile(filePath);
        dotFile = readDotFile(Path.of(outDir.toString(), "0", "strands.dbn"));
        return dotFile;
    }
}
