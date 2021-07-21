package updater;

import lombok.Getter;
import lombok.NonNull;
import models.Structure;
import models.DBrecord;
import utils.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DBDownloader {

    public static final Path downloadPath = Main.frabaseDir.resolve("3DStructures");
    public static final Path newRecordsPath = Main.frabaseDir.resolve("DBrecords.txt");
    public static final ConcurrentLinkedQueue<DBrecord> records = new ConcurrentLinkedQueue<>();
    @Getter
    private static final AtomicInteger fileNo = new AtomicInteger();

    /**
     * Download file with new structures in PDB.
     * @return Path to downloaded file.
     */
    public Path downloadChangeList(){
        File outDir = Main.frabaseDir.toFile();
        Path outPath = Main.frabaseDir.resolve("pdb_entry_type.txt");
        Utils.createDirIfNotExist(outDir, Main.stdLogger);

        //-O stands for overwrite, -P stands for directory
        /*Utils.execCommand("wget -P " +
                        outDir.getAbsolutePath() +
                        " https://ftp.wwpdb.org/pub/pdb/derived_data/pdb_entry_type.txt -O " +
                        outPath,
                false,
                Main.stdLogger,
                Main.errLogger);*/
        return outPath;
    }

    /**
     * Download file with resolution of new structures in PDB.
     * @return Path to downloaded file.
     */
    public Path downloadResolList(){
        Path outPath = Main.frabaseDir.resolve("resolu.idx");
        //-O stands for overwrite, -P stands for directory
        Utils.execCommand("wget -P " +
                        Main.frabaseDir +
                        " https://ftp.wwpdb.org/pub/pdb/derived_data/index/resolu.idx -O " +
                        outPath,
                false,
                Main.stdLogger,
                Main.errLogger);
        return outPath;
    }


    private void addToUpdatedFiles(HashSet<String> oldFiles){
        oldFiles.forEach(fileName ->{
            File file = Worker.getOldFilesDir().resolve(fileName).toFile();
            if(!file.delete())
                file.deleteOnExit();
            DBUpdater.updatedFiles.add(new Structure(fileName.substring(0, fileName.length() - 7), null));
            if(DBUpdater.updatedFiles.size() > 1000){
                DBDownloader.saveQueueToFile(DBUpdater.updatedFiles, DBUpdater.updatedStructuresPath);
            }
        });
    }

    private void filterOutDNAStructures(Path dnaListPath, Path dnaIdListPath){
        String line, id, type;
        StringTokenizer structure;
        int counter = 0;

        HashSet<String> oldFiles = new HashSet<>(
                Arrays.asList(Objects.requireNonNullElse((Worker.getOldFilesDir().toFile().list()), new String[]{})));
        HashSet<String> newFiles = new HashSet<>(1000);

        //change preprocess mode. Need to recompute all files again, soo here we delete all old files.
        if(!Preprocessor.getPreprocessType().equals(Preprocessor.getLastPreprocessType())){
            Arrays.asList(Objects.requireNonNullElse(Worker.getOldFilesDir().toFile().listFiles(), new File[]{}))
                    .forEach(File::delete);
        }

        Main.verboseInfo("Filtering out DNA structures.", 2);
        try(Scanner structuresReader = new Scanner(dnaListPath.toFile());
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(dnaIdListPath.toString())))){
            //uncomment code below, when testing on real list of files

            while (structuresReader.hasNextLine()){
                line = structuresReader.nextLine();
                structure = new StringTokenizer(line, " \t");
                id = structure.nextToken();
                type = structure.nextToken();
                if(type.equals("nuc") || type.equals("prot-nuc")){
                    bw.write(id + ",");
                    newFiles.add(id + ".cif.gz");
                    counter++;
                    if(newFiles.size() == 1000){
                        oldFiles.removeAll(newFiles);
                        newFiles.clear();
                    }
                }
            }
            oldFiles.removeAll(newFiles);
            newFiles.clear();
            addToUpdatedFiles(oldFiles);
            //uncomment code below, when testing on chosen list of files
            /*newFiles.addAll(Arrays.asList("100d.cif.gz", "1ekz.cif.gz", "1elh.cif.gz", "1ekd.cif.gz", "1eqq.cif.gz"));//, "1et4.cif.gz"
            oldFiles.removeAll(newFiles);
            newFiles.clear();
            bw.write("100d,1ekz,1elh,1ekd,1eqq");//,1et4
            addToUpdatedFiles(oldFiles);*/
        } catch (FileNotFoundException e) {
            Main.errLogger.severe("Couldn't find file: resolu.idx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileNo.set(counter);
        Main.verboseInfo(counter + " RNA structures will be downloaded. " +
                "Keep calm. It will take some time.", 1);
    }

    public Path getNewStructuresList(){
        Path dnaIdListPath = Path.of(
                Objects.requireNonNull(this.getClass().getResource("/")).getPath(),
                "changedIds.txt");
        Path dnaListPath = downloadChangeList();

        Main.verboseInfo("Downloading list of resolutions of new structures.", 2);
        downloadResolList();
        filterOutDNAStructures(dnaListPath, dnaIdListPath);
        return dnaIdListPath;
    }

    public Path downloadNewStructures(){
        String command;
        boolean displayInfo = (Main.getVerboseMode() >= 2);
        Utils.createDirIfNotExist(downloadPath.toFile(), Main.stdLogger);

        Main.verboseInfo("Downloading list of new structures.", 2);
        Path newStrucListPath = getNewStructuresList();
        Main.verboseInfo("Downloading new nuc and prot-nuc structures.", 1);
        Path downloadScriptPath = Path.of(
                Objects.requireNonNull(getClass().getResource("/batch_download.sh")).getPath());
        command = downloadScriptPath + " -f " + newStrucListPath + " -o " + downloadPath.toAbsolutePath() + " -c";
        Utils.execCommand(command, displayInfo, Main.stdLogger, Main.errLogger);
        return downloadPath;
    }

    /**
     * Delete file with database records and file with models of updated structures
     * from old update and create new ones.
     */
    public static void prepareFiles(){
        prepareFile(newRecordsPath.toFile());
        prepareFile(DBUpdater.updatedStructuresPath.toFile());
    }

    private static void prepareFile(File outFile){
        try {
            outFile.delete();
            if(!outFile.createNewFile()){
                Main.verboseInfo("Couldn't access file:\n" + outFile.getAbsolutePath(), 1);
                Main.errLogger.severe("Couldn't access file:\n" + outFile.getAbsolutePath());
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void saveQueueToFile(ArrayList<T> records, Path filePath){
        if(filePath.getFileName().equals(newRecordsPath.getFileName())){
            WorkSubmitter.getRecordsNo().addAndGet(records.size());
        }
        try(BufferedWriter bw = new BufferedWriter(
                new FileWriter(filePath.toString(), true))) {
            for(T record: records){
                bw.write(record + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void saveQueueToFile(@NonNull ConcurrentLinkedQueue<T> queue, boolean forceSave, Path filePath){
        synchronized (queue) {
            ArrayList<T> handler;
            if (queue.size() > 1000 || forceSave) {
                handler = new ArrayList<>(queue);
                saveQueueToFile(handler, filePath);
                queue.removeAll(handler);
            }
        }
    }

    public static <T> void saveQueueToFile(ConcurrentLinkedQueue<T> queue, Path filePath){
        DBDownloader.saveQueueToFile(queue, false, filePath);
    }

    /**
     * Compute records for all 3D structures under
     * given directory
     * @param dir directory with 3D structures
     */
    public void updateDBAfterDownload(final Path dir){
        List<Callable<Path>> callables = new ArrayList<>();
        Main.verboseInfo("Computing records.", 1);
        FilenameFilter filter = (dir1, name) -> name.endsWith(".gz");
        String[] fileList = Objects.requireNonNull(dir.toFile().list(filter));
        ExecutorService executor = Executors.newFixedThreadPool(Main.WorkersNo);

        prepareFiles();
        fileNo.set(fileList.length);
        for (String file : fileList) {
            callables.add(new Worker(dir.resolve(file)));
        }
        try {
            executor.invokeAll(callables);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DBDownloader.saveQueueToFile(records, true, newRecordsPath);
    }
}
