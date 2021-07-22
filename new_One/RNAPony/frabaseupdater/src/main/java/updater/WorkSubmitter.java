package updater;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkSubmitter implements Runnable{

    private final Timer timer;
    @Getter(AccessLevel.PACKAGE)
    private static final AtomicInteger downloadedFileNo = new AtomicInteger();
    private final HashSet<String> files = new HashSet<>(DBDownloader.getFilesBatchSize());
    private final ArrayList<Future<Path>> tasks = new ArrayList<>(200);
    private final ExecutorService executor = Executors.newFixedThreadPool(Main.WorkersNo);
    @Getter(AccessLevel.PACKAGE)
    private static final AtomicInteger recordsNo = new AtomicInteger();
    @Setter
    private boolean isDownloading = true;
    private boolean isAlive = true;

    /**
     * Constructor of WorkSubmitter.
     *
     * @param refreshTime time in miliseconds, which say how often
     *                    new tasks will be searched and submitted.
     */
    public WorkSubmitter(long refreshTime) {
        this.timer = new Timer();
        //schedule submitter refreshing
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (WorkSubmitter.this){
                    WorkSubmitter.this.notify();
                }
            }
        }, 0, refreshTime);
    }

    private void removeDeletedFiles(){
        HashSet<String> deletedFiles = new HashSet<>();
        //Removing submitted files
        tasks.removeIf(task ->{
            if(task.isDone()){
                try {
                    deletedFiles.add(task.get().getFileName().toString());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });
        files.removeAll(deletedFiles);
    }

    private HashSet<String> getNewFiles(final Path dir){
        FilenameFilter filter = (dir1, name) -> name.endsWith(".gz");
        HashSet<String> currFileList;

        //remove files processed by completed tasks - less work for currFileList.removeAll(files)
        removeDeletedFiles();
        currFileList = new HashSet<>(Arrays.asList(Objects.requireNonNull(dir.toFile().list(filter))));
        //remove tasks which was already submitted
        currFileList.removeAll(files);

        //remove files, which was modified less than 2 seconds ago, because they can be still downloaded
        currFileList.removeIf(fileName ->{
            File file = DBDownloader.downloadPath.resolve(fileName).toFile();
            long difference = (System.currentTimeMillis() - file.lastModified());
            if(difference <= 2000){
                isAlive = true;//needed next iteration in case of ended download
                return true;
            }
            return false;
        });
        WorkSubmitter.downloadedFileNo.addAndGet(currFileList.size());
        files.addAll(currFileList);
        return currFileList;
    }

    /**
     * If too many tasks added to executor que, then this function
     * can be called. Count how many tasks from queue is complete
     * and return, when number of completed tasks is bigger than
     * given threshold. Scheduling refreshing count of completed
     * task is connected to timer of this WorkSubmitter. In other
     * words number of completed tasks is refreshed every n seconds,
     * where n is time of refreshing this WorkSubmitter.
     *
     * @param taskDoneThreshold amount of tasks, which need to be completed
     *                          to make this procedure stop blocking.
     */
    private void waitTillQueueShrunk(long taskDoneThreshold){
        long taskDone = 0;
        while (taskDone < taskDoneThreshold) {
            taskDone = 0;
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Future<Path> task : tasks) {
                if (task.isDone()) {
                    taskDone++;
                    if (taskDone >= taskDoneThreshold)
                        return;
                }
            }
        }
    }

    /**
     * Compute records for all 3D structures under
     * given directory
     * @param dir directory with 3D structures
     */
    public void updateDB(final Path dir){
        HashSet<String> currFileList;
        long taskDoneThreshold;
        isAlive = isDownloading;
        currFileList = getNewFiles(dir);
        Main.verboseInfo("Submitting files to process: " + currFileList, 3);
        for (String file : currFileList) {
            try {
                tasks.add(executor.submit(new Worker(dir.resolve(file))));
            }
            catch (RejectedExecutionException e){//queue of executor can be full
                taskDoneThreshold = tasks.size() / 2;
                waitTillQueueShrunk(taskDoneThreshold);
                tasks.add(executor.submit(new Worker(dir.resolve(file))));
            }
        }
    }

    @Override
    public void run() {
        Main.verboseInfo("Computing records.", 1);
        while (isAlive){
            //wait some time, till new files will be downloaded
            try {
                synchronized (this){
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateDB(DBDownloader.downloadPath);
        }
        timer.cancel();
        executor.shutdown();
        //wait till workers end work, before save records to file.
        tasks.forEach(task -> {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        Main.stdLogger.info("records to save: " + DBDownloader.records.size());
        DBDownloader.saveQueueToFile(DBDownloader.records, true, DBDownloader.newRecordsPath);
    }
}
