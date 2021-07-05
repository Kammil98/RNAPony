package updater;

import utils.Utils;

import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {

    public static final Logger errLogger = Logger.getLogger(Main.class.getName() + "err");
    public static final Logger stdLogger = Logger.getLogger(Main.class.getName() + "std");
    public static final Path frabaseDir = Path.of("frabase_update");
    //private static String fileName = "1ekd.cif";
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        Main.errLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(errLogger, Path.of(".", "errApp.txt"));
        Main.stdLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(stdLogger);
    }

    public static void main(String[] args){
        DBUpdater updater = new DBUpdater();
        updater.downloadAndUpdateNewStructures();
        /*Preprocessor preprocessor = new Preprocessor();
        DataLoader loader = new DataLoader();
        stdLogger.info("Preprocess" +  fileName);
        preprocessor.extractRNA(Path.of(fileName));
        for(String newFileName: Objects.requireNonNull(new File(Preprocessor.preprocessOutDir.toString()).list())){
            stdLogger.info("Prepare dot file for " +  newFileName);
            loader.prepareDotFile(Path.of(Preprocessor.preprocessOutDir.toString(), newFileName));
        }
        stdLogger.info("main done");*/
    }
}
