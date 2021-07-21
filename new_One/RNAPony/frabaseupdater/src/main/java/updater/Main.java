package updater;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final Logger errLogger = Logger.getLogger(Main.class.getName() + "err");
    public static final Logger stdLogger = Logger.getLogger(Main.class.getName() + "std");
    public static final Path frabaseDir = Path.of("frabase_update");

    public static int dayOfWeek;
    public static LocalTime time;
    public static int WorkersNo = 4;
    @Parameter(names = {"--verbose", "-v"}, description = "Level of verbosity. The higher value, the more information " +
            "are printed. Possible verbosity levels are in range 0-3. 0 Means no information printed")
    @Getter
    private static int verboseMode = 2;

    /**
     * Print message, if verbose level is equal or higher than given.
     *
     * @param message message to print.
     * @param verboseLevel level of priority of this message.
     * @param level One of the message level identifiers, e.g., SEVERE.
     */
    public static void verboseInfo(String message, int verboseLevel, Level level){
        if(Main.getVerboseMode() >= verboseLevel)
            Main.stdLogger.log(level, message);
    }

    /**
     * Print info, if verbose level is equal or higher than given.
     *
     * @param message message to print.
     * @param verboseLevel level of priority of this message.
     */
    public static void verboseInfo(String message, int verboseLevel){
        verboseInfo(message, verboseLevel, Level.INFO);
    }

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        Main.errLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(errLogger, Path.of(".", "errApp.txt"));
        Main.stdLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(stdLogger);
    }

    private static void updateDB(){
        int affectedRows;
        try(DBUpdater updater = new DBUpdater()) {
            affectedRows = updater.addOrUpdateNewRecords(frabaseDir.resolve("DBrecords.txt"));
            Main.verboseInfo(affectedRows + " rows were added or updated.", 1);
            affectedRows = updater.deleteOldRecords();
            Main.verboseInfo(affectedRows + " rows were deleted.", 1);
        } catch (SQLException throwables) {
            Main.verboseInfo("Couldn't connect to database: ", 1);
            Main.errLogger.severe("Couldn't connect to database: ");
            System.exit(-1);
        }
    }

    public static void main(String[] args){
        PropertiesReader.loadProperties(args);
        DBDownloader.prepareFiles();
        WorkSubmitter submitter = new WorkSubmitter(2000);// 2 seconds
        Thread submitterThread = new Thread(submitter);
        submitterThread.start();
        DBDownloader loader = new DBDownloader();
        Path downloadDir = loader.downloadNewStructures();
        submitter.setDownloading(false);//notify submitter, that downloading stopped
        //loader.updateDB(downloadDir);
        Main.verboseInfo(Preprocessor.getFilesWithAllModelsEmptyNo().get() + " files have 0 models with strands " +
                "available to process.\n" + Worker.getProcessedModels() + " models were processed.", 1);
        try {
            submitterThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Main.verboseInfo("Updating database", 1);
        updateDB();
    }
}
