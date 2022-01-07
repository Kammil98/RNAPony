package updater;

import models.Structure;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Timer;
import java.util.TimerTask;

public class TimerUpdateTask extends TimerTask {
    private final String[] args;
    private final Timer timer;

    public TimerUpdateTask(String[] args, Timer timer){
        this.args = args;
        this.timer = timer;
    }

    /**
     * Calculate next date with given time, which occur after present
     * date and has given day of week.
     *
     * @param time time, which returned date will be keeping
     * @param dayOfWeek day of week, which returned date will be representing
     * @return date with time, which occur after present
     * date and has given day of week.
     */
    static LocalDateTime getNextDate(LocalTime time, DayOfWeek dayOfWeek){
        return LocalDateTime.now()
                .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                .withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(time.getSecond());
    }

    /**
     * Initialize starting state of program.
     * In other words it return program to initial state
     * before next actualization of database.
     * @param args arguments given to program through console
     */
    private static void initProgramState(String[] args){
        Structure.resetMaxModelsNo();
        DBDownloader.getFileNo().set(0);
        DBDownloader.records.clear();
        DBUpdater.getUpdatedFiles().clear();
        Preprocessor.getFilesWithAllModelsEmptyNo().set(0);
        Worker.getProcessedFiles().set(0);
        Worker.getProcessedModels().set(0);
        WorkSubmitter.getDownloadedFileNo().set(0);
        WorkSubmitter.getRecordsNo().set(0);
        Preprocessor.setLastPreprocessType(Preprocessor.getPreprocessType());
        PropertiesReader.loadProperties(args);
    }

    /**
     * Updates RNAfrabase by nucleotides from Protein Data Bank.
     *
     * @param args arguments given to program at beginning
     */
    private void downloadDataAndUpdateRNAfrabase(String[] args){
        initProgramState(args);
        DBDownloader.prepareFiles();
        WorkSubmitter submitter = new WorkSubmitter(2000);// 2 seconds
        Thread submitterThread = new Thread(submitter);
        submitterThread.start();
        DBDownloader loader = new DBDownloader();
        Path downloadDir = loader.downloadNewStructures();
        submitter.setDownloading(false);//notify submitter, that downloading stopped
        //loader.updateDB(downloadDir);
        try {
            submitterThread.join();
            Main.verboseInfo(Preprocessor.getFilesWithAllModelsEmptyNo().get() +
                    " files have 0 models with strands available to process.\nModels from " +
                    Worker.getProcessedModels() + " different files were processed.", 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Main.verboseInfo("Updating database", 1);
        updateDB();
    }

    /**
     * Updates database based on downloaded and preprocessed files.
     */
    private static void updateDB(){
        int affectedRows;
        try(DBUpdater updater = new DBUpdater()) {
            affectedRows = updater.addOrUpdateNewRecords(Main.frabaseDir.resolve("DBrecords.txt"));
            Main.verboseInfo(affectedRows + " rows were added or updated(including unchanged models from " +
                    "updated files.", 1);
            affectedRows = updater.deleteOldRecords();
            Main.verboseInfo(affectedRows + " rows were deleted.", 1);
        } catch (SQLException throwables) {
            Main.verboseInfo("Couldn't connect to database: " + throwables.getMessage(), 1);
            Main.errLogger.severe("Couldn't connect to database: " + throwables.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        Main.verboseInfo("\n\n\n\t\t\t\t\t\tStarting Updating RNAfrabase at " + LocalTime.now() +
                "\t\t\t\t\t\t\n\n\n", 1);
        downloadDataAndUpdateRNAfrabase(args);
    }
}
