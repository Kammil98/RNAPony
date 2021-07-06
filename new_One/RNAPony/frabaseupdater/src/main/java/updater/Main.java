package updater;

import utils.Utils;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.logging.Logger;

public class Main {

    public static final Logger errLogger = Logger.getLogger(Main.class.getName() + "err");
    public static final Logger stdLogger = Logger.getLogger(Main.class.getName() + "std");
    public static final Path frabaseDir = Path.of("frabase_update");

    public static int dayOfWeek;
    public static LocalTime time;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
        Main.errLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(errLogger, Path.of(".", "errApp.txt"));
        Main.stdLogger.setUseParentHandlers(false);
        Utils.changeLogHandler(stdLogger);
    }

    public static void main(String[] args){
        PropertiesReader.loadProperties(args);
        DBUpdater updater = new DBUpdater();
        File downloadDir = updater.downloadAndUpdateNewStructures();
        updater.updateDB(downloadDir);
    }
}
