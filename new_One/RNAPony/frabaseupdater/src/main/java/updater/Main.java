package updater;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import utils.Utils;

import java.nio.file.Path;
import java.time.LocalTime;
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
        Path downloadDir = updater.downloadNewStructures();
        updater.updateDB(downloadDir);
    }
}
