package updater;

import models.DBrecord;
import models.DotFile;
import utils.Utils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class DBUpdater {

    private static final ConcurrentLinkedQueue<DBrecord> records = new ConcurrentLinkedQueue<>();
    private static int processedFiles = 0;
    /**
     * Download file with new structures in PDB.
     * @return Path to downloaded file.
     */
    public Path downloadChangeList(){
        File outDir = Main.frabaseDir.toFile();
        Path outPath = Main.frabaseDir.resolve("pdb_entry_type.txt");
        Utils.createDirIfNotExist(outDir, Main.stdLogger);

        //-O stands for overwrite, -P stands for directory
        Utils.execCommand("wget -P " +
                        outDir.getAbsolutePath() +
                        " https://ftp.wwpdb.org/pub/pdb/derived_data/pdb_entry_type.txt -O " +
                        outPath,
                false,
                Main.stdLogger,
                Main.errLogger);
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


    public Path getNewStructuresList(){
        Path changedIdListPath = Path.of(
                Objects.requireNonNull(this.getClass().getResource("/")).getPath(),
                "changedIds.txt");
        Path changedListPath = downloadChangeList();
        String line, id, type;
        StringTokenizer structure;
        int counter = 0;

        if(Main.getVerboseMode() >= 2)
            Main.stdLogger.info("Downloading list of resolutions of new structures.");
        downloadResolList();
        if(Main.getVerboseMode() >= 2)
            Main.stdLogger.info("Filtering out DNA structures.");
        try(Scanner structuresReader = new Scanner(changedListPath.toFile());
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(changedIdListPath.toString())))){
            //uncomment code below, when testing on real list of files
            /*while (structuresReader.hasNextLine()){
                line = structuresReader.nextLine();
                structure = new StringTokenizer(line, " \t");
                id = structure.nextToken();
                type = structure.nextToken();
                if(type.equals("nuc") || type.equals("prot-nuc")){
                    bw.write(id + ",");
                    counter++;
                }
            }*/
            bw.write("1ekd,1et4,1ekz,1elh,1eqq");
        } catch (FileNotFoundException e) {
            Main.errLogger.severe("Couldn't find file: resolu.idx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(Main.getVerboseMode() >= 1)
            Main.stdLogger.info(counter + " RNA structures will be downloaded. " +
                    "Keep calm. It will take some time.");
        return changedIdListPath;
    }

    public Path downloadNewStructures(){
        Path outDir = Main.frabaseDir.resolve("3DStructures");
        String command;
        boolean displayInfo = (Main.getVerboseMode() >= 2);
        Utils.createDirIfNotExist(outDir.toFile(), Main.stdLogger);
        if(Main.getVerboseMode() >= 2)
            Main.stdLogger.info("Downloading list of new structures.");
        Path newStrucListPath = getNewStructuresList();
        if(Main.getVerboseMode() >= 1)
            Main.stdLogger.info("Downloading new nuc and prot-nuc structures.");
        Path downloadScriptPath = Path.of(
                Objects.requireNonNull(getClass().getResource("/batch_download.sh")).getPath());
        command = downloadScriptPath + " -f " + newStrucListPath + " -o " + outDir.toAbsolutePath() + " -c";
        Utils.execCommand(command, displayInfo, Main.stdLogger, Main.errLogger);
        return outDir;
    }

    /**
     * Check weather given string contains characters different,
     * than '.' and '-' or not.
     * @param dots 2D sequence of structure id dot-bracket notation
     * @return true, if string contains some pairs.
     */
    private boolean isOnlyDots(String dots){
        return dots.matches(("^[.|-]*$"));
    }

    protected ArrayList<DBrecord> computeRecord(Path filePath){
        ArrayList<DBrecord> records = new ArrayList<>();
        DotFileCreator dotFileCreator = new DotFileCreator();
        DotFile dotFile;
        DBrecord record;
        Path preprocessedFilePath;
        Path preprocessedFileDir;
        if(Main.getVerboseMode() >= 3)
            Main.stdLogger.info("Preprocess " +  filePath);
        preprocessedFileDir = new Preprocessor().extractRNA(filePath);
        String[] fileList = Objects.requireNonNull(preprocessedFileDir.toFile().list());
        for(String newFileName: fileList){
            preprocessedFilePath = preprocessedFileDir.resolve(newFileName);
            dotFile = dotFileCreator.getDotFile(preprocessedFilePath);
            if(isOnlyDots(dotFile.getDot())){
                if(Main.getVerboseMode() >= 2)
                    Main.stdLogger.info(newFileName + " doesn't contain pairs - omitting this model.");
                preprocessedFilePath.toFile().delete();
                continue;
            }
            record = new DBrecord();
            if(Main.getVerboseMode() >= 3)
                Main.stdLogger.info("Compute record");
            record.computeRecord(dotFile, preprocessedFilePath.getFileName().toString());
            records.add(record);
            if(Main.getVerboseMode() >= 3)
                Main.stdLogger.info("Record created\n");
            preprocessedFilePath.toFile().delete();
        }
        if(records.size() > 1)
            Main.stdLogger.info("!!!!!!!!!!!!!!!!FOUND 2 records for " + filePath + "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return records;
    }

    public void saveRecordsToFile(ArrayList<DBrecord> records){
        try(BufferedWriter bw = new BufferedWriter(
                new FileWriter(Main.frabaseDir.resolve("DBrecords.txt").toString(), true))) {
            for(DBrecord record: records){
                bw.write(record + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete file with database from old update and create new one.
     */
    private void prepareDBFile(){
        File outFile = Path.of(Main.frabaseDir.toString(), "DBrecords.txt").toFile();
        try {
            outFile.delete();
            if(!outFile.createNewFile()){
                Main.stdLogger.severe("Couldn't access file:\n" + outFile.getAbsolutePath());
                Main.errLogger.severe("Couldn't access file:\n" + outFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unGzipFile(Path compressedFile, Path decompressedFile) {
        byte[] buffer = new byte[1024];
        int bytes_read;

        Utils.createDirIfNotExist(decompressedFile.getParent().toFile(), true, Main.stdLogger, Main.errLogger);
        try(GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(compressedFile.toString()));
            FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile.toString())) {
            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveRecordsToFile(boolean forceSave){
        ArrayList<DBrecord> handler;
        if (records.size() > 1000 || forceSave) {
            handler = new ArrayList<>(records);
            saveRecordsToFile(handler);
            records.removeAll(handler);
        }
    }

    private synchronized void saveRecordsToFile(){
        saveRecordsToFile(false);
    }

    /**
     * Compute records for all 3D structures under
     * given directory
     * @param dir directory with 3D structures
     */
    public void updateDB(final Path dir){
        if(Main.getVerboseMode() >= 1)
            Main.stdLogger.info("Computing records.");
        prepareDBFile();
        FilenameFilter filter = (dir1, name) -> name.endsWith(".gz");
        String[] fileList = Objects.requireNonNull(dir.toFile().list(filter));
        ExecutorService executor = Executors.newFixedThreadPool(Main.WorkersNo);
        List<Callable<Byte>> callables = new ArrayList<>();
        for (String file : fileList) {
            callables.add(new Worker(dir.resolve(file)));
        }
        try {
            executor.invokeAll(callables);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        saveRecordsToFile(true);
    }

    public void processFile(Path filePath){
        String unpackedFileName, fileName = filePath.getFileName().toString();
        Path unpackedFilePath;
        long start, finish;
        start = System.currentTimeMillis();
        unpackedFileName = fileName.substring(0, fileName.length() - 3);
        unpackedFilePath = filePath.getParent()
                .resolve(String.valueOf(Thread.currentThread().getId()))
                .resolve(unpackedFileName);
        if(Main.getVerboseMode() >= 2)
            Main.stdLogger.info("Processing " +  fileName);
        unGzipFile(filePath, unpackedFilePath);
        records.addAll(computeRecord(unpackedFilePath));
        saveRecordsToFile();
        if(!unpackedFilePath.toFile().delete()){
            unpackedFilePath.toFile().deleteOnExit();
        }
        finish = System.currentTimeMillis();
        if(Main.getVerboseMode() >= 3)
            Main.stdLogger.info("Time for whole file: " + (finish - start));
    }
}
