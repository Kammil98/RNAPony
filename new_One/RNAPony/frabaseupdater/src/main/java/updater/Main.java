package updater;

import com.beust.jcommander.Parameter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import models.parameters.converters.DayOfWeekConverter;
import models.parameters.converters.LocalDateTimeConverter;
import utils.Utils;

import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
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
    @Getter @Setter(AccessLevel.PACKAGE)
    private static int WorkersNo = 3;

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

    public static void main(String[] args){
        Timer timer = new Timer();
        Date firstUpdateDate = Timestamp.valueOf(time);
        long period = ChronoUnit.MILLIS.between(LocalDateTime.now(), LocalDateTime.now().plusWeeks(1));
        timer.schedule(new TimerUpdateTask(args, timer),
                Timestamp.valueOf(LocalDateTime.now().plusSeconds(2)),
                ChronoUnit.MILLIS.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(3)));
    }
}
