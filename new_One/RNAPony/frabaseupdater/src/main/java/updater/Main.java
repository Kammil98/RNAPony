package updater;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import models.parameters.converters.DayOfWeekConverter;
import models.parameters.converters.LocalDateTimeConverter;
import utils.Utils;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static final Logger errLogger = Logger.getLogger(Main.class.getName() + "err");
    public static final Logger stdLogger = Logger.getLogger(Main.class.getName() + "std");
    public static final Path frabaseDir = Path.of("frabase_update");

    @Parameter(names = {"--day", "-d"}, converter = DayOfWeekConverter.class,
            description = "Day of week, when update will be made. Possible values are 1-7, " +
            "where 1 = Monday and 7 = Sunday.")
    public static DayOfWeek dayOfWeek;
    @Parameter(names = {"--time", "-t"}, converter = LocalDateTimeConverter.class,
            description = "Day of week, when update will be made. Possible values are 1-7, " +
            "where 1 = Monday and 7 = Sunday.")
    public static LocalDateTime time = LocalDateTime.now();
    @Parameter(names = {"--workers", "-w"}, description = "Number of threads, which computes records in parallel" +
            " to downloading files.")
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

    /**
     * Updates database based on downloaded and preprocessed files.
     */
    private static void updateDB(){
        int affectedRows;
        try(DBUpdater updater = new DBUpdater("rnapony")) {
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
        Timer timer = new Timer();
        Date firstUpdateDate = Timestamp.valueOf(time);
        long period = ChronoUnit.MILLIS.between(LocalDate.now(), LocalDate.now().plusWeeks(1));
        timer.schedule(new TimerUpdateTask(args, timer), firstUpdateDate, period);
    }
}
