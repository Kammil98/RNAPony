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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

public class DBUpdater {

    private static final ConcurrentLinkedQueue<DBrecord> records = new ConcurrentLinkedQueue<>();
    private static AtomicInteger processedFiles = new AtomicInteger();
    private static int fileNo = 0;

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

        Main.verboseInfo("Downloading list of resolutions of new structures.", 2);
        downloadResolList();
        Main.verboseInfo("Filtering out DNA structures.", 2);
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
        Main.verboseInfo(counter + " RNA structures will be downloaded. " +
                "Keep calm. It will take some time.", 1);
        return changedIdListPath;
    }

    public Path downloadNewStructures(){
        Path outDir = Main.frabaseDir.resolve("3DStructures");
        String command;
        boolean displayInfo = (Main.getVerboseMode() >= 2);
        Utils.createDirIfNotExist(outDir.toFile(), Main.stdLogger);

        Main.verboseInfo("Downloading list of new structures.", 2);
        Path newStrucListPath = getNewStructuresList();
        Main.verboseInfo("Downloading new nuc and prot-nuc structures.", 1);
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

    /**
     * Compute all records of RNAfrabase based on file with 3D representation of structure.
     *
     * @param filePath path to file with structure with all its models.
     * @return list of records, which represents models
     *          from file with 3D structure of RNA.
     */
    protected ArrayList<DBrecord> computeStructure(Path filePath){
        ArrayList<DBrecord> records = new ArrayList<>();
        DBrecord record;
        Path preprocessedFileDir;
        Main.verboseInfo("Preprocess " +  filePath, 3);
        preprocessedFileDir = new Preprocessor().extractRNA(filePath);
        String[] fileList = Objects.requireNonNull(preprocessedFileDir.toFile().list());
        for(String newFileName: fileList){
            record = computeModel(preprocessedFileDir.resolve(newFileName));
            if(record != null)
                records.add(record);
        }
        if(records.size() > 1)
            Main.stdLogger.info("!!!!!!!!!!!!!!!!FOUND 2 records for " + filePath + "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return records;
    }

    /**
     * Compute record of RNAfrabase based on file with 3D representation of model.
     *
     * @param preprocessedFilePath path to file with model.
     * @return record of RNAfrabase, which represent given model.
     */
    public DBrecord computeModel(Path preprocessedFilePath){
        DotFile dotFile;
        DBrecord record;
        dotFile = new DotFileCreator().getDotFile(preprocessedFilePath);
        if(isOnlyDots(dotFile.getDot())){
            Main.verboseInfo(preprocessedFilePath.getFileName() +
                    " doesn't contain pairs - omitting this model.", 2);
            preprocessedFilePath.toFile().delete();
            return null;
        }
        record = new DBrecord();
        Main.verboseInfo("Compute record", 3);
        record.computeRecord(dotFile, preprocessedFilePath.getFileName().toString());
        Main.verboseInfo("Record created\n", 3);
        preprocessedFilePath.toFile().delete();
        return record;
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
                Main.verboseInfo("Couldn't access file:\n" + outFile.getAbsolutePath(), 1);
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
        List<Callable<Byte>> callables = new ArrayList<>();
        Main.verboseInfo("Computing records.", 1);
        FilenameFilter filter = (dir1, name) -> name.endsWith(".gz");
        String[] fileList = Objects.requireNonNull(dir.toFile().list(filter));
        ExecutorService executor = Executors.newFixedThreadPool(Main.WorkersNo);

        prepareDBFile();
        fileNo = fileList.length;
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
        Main.verboseInfo("Processing " +  fileName + ". " +
                processedFiles.get() + "/" + fileNo + " files processed.", 2);
        unGzipFile(filePath, unpackedFilePath);
        records.addAll(computeStructure(unpackedFilePath));
        saveRecordsToFile();
        if(!unpackedFilePath.toFile().delete()){
            unpackedFilePath.toFile().deleteOnExit();
        }
        finish = System.currentTimeMillis();
        Main.verboseInfo("Time for whole file: " + (finish - start), 3);
        processedFiles.incrementAndGet();
    }
}
