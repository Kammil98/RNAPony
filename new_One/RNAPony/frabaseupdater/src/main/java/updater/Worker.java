package updater;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.Structure;
import models.DBrecord;
import models.DotFile;
import utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

@AllArgsConstructor
public class Worker implements Callable<Path> {
    private final Path filePath;
    @Getter
    private static final AtomicInteger processedFiles = new AtomicInteger();
    @Getter
    private static final AtomicInteger processedModels = new AtomicInteger();

    @Getter
    private static final Path oldFilesDir = Main.frabaseDir.resolve("oldFiles");

    static {
        Utils.createDirIfNotExist(oldFilesDir.toFile(), false, Main.stdLogger, Main.errLogger);
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
     * Compute record of RNAfrabase based on file with 3D representation of model.
     *
     * @param preprocessedFilePath path to file with model.
     * @return record of RNAfrabase, which represent given model.
     */
    public DBrecord computeModel(Path preprocessedFilePath){
        DotFile dotFile;
        DBrecord record;
        String filename = preprocessedFilePath.getFileName().toString();
        dotFile = new DotFileCreator().getDotFile(preprocessedFilePath);
        if(isOnlyDots(dotFile.getDot())){
            Main.verboseInfo("[" + filename + "]  - model doesn't contain pairs. Omitting this model.",
                    2);
            preprocessedFilePath.toFile().delete();
            return null;
        }
        record = new DBrecord();
        Main.verboseInfo("Compute record for " + filename, 3);
        record.computeRecord(dotFile, filename);
        Main.verboseInfo("Record created for " + filename + "\n", 3);
        preprocessedFilePath.toFile().delete();
        return record;
    }

    /**
     * Compute all records of RNAfrabase based on file with 3D representation of structure.
     *
     * @param filePath path to unzipped file with structure with all its models.
     * @return list of records, which represents models
     *          from file with 3D structure of RNA.
     */
    protected ArrayList<DBrecord> computeStructure(Path filePath){
        String fileName = filePath.getFileName().toString();
        ArrayList<DBrecord> records = new ArrayList<>();
        DBrecord record;
        Path preprocessedFileDir;
        Main.verboseInfo("Preprocess " +  filePath, 3);
        preprocessedFileDir = new Preprocessor().extractRNA(filePath);
        String[] fileList = Objects.requireNonNull(preprocessedFileDir.toFile().list());
        if(fileList.length == 0)//statistics to print
            Preprocessor.getFilesWithAllModelsEmptyNo().incrementAndGet();
        else
            processedModels.addAndGet(fileList.length);
        for(String newFileName: fileList){
            record = computeModel(preprocessedFileDir.resolve(newFileName));
            if(record != null)
                records.add(record);
        }
        addToUpdatedStrucList(records, fileName.substring(0, fileName.length() - 4));//fileName without ".cif"
        return records;
    }

    /**
     * Add to list all files with ids and ids of its models,
     * to check later which one is new, and which one to delete
     *
     * @param records all records representing models of one file
     */
    private void addToUpdatedStrucList(ArrayList<DBrecord> records, String id){
        int fileNo = 0;
        int [] models;
        models = new int[records.size()];
        for(DBrecord record: records){
            models[fileNo] = record.getModelNo();
            fileNo++;
        }
        DBUpdater.getUpdatedFiles().add(new Structure(id, models));
        DBDownloader.saveQueueToFile(DBUpdater.getUpdatedFiles(), DBUpdater.updatedStructuresPath);
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
        if(!filePath.toFile().renameTo(oldFilesDir.resolve(filePath.getFileName()).toFile())) {
            if (!filePath.toFile().delete()) {
                filePath.toFile().deleteOnExit();
            }
        }
    }

    private boolean isUnchanged(Path compressedFile){
        Path oldCompressedFile = oldFilesDir.resolve(compressedFile.getFileName());
        String command = "diff " + compressedFile + " " + oldCompressedFile;
        Utils.createDirIfNotExist(oldCompressedFile.getParent().toFile(), true, Main.stdLogger, Main.errLogger);
        if(!oldCompressedFile.toFile().exists())
            return false;
        InputStream result = Utils.execCommand(command, false, Main.stdLogger, Main.errLogger,
                Arrays.asList(0, 1));
        boolean isUnchanged;
        try {
            isUnchanged = (new String(result.readAllBytes(), StandardCharsets.UTF_8).length() == 0);
        } catch (IOException e) {
            e.printStackTrace();
            isUnchanged = false;
        }
        return isUnchanged;
    }

    public void processFile(Path filePath){
        String unpackedFileName, fileName = filePath.getFileName().toString();
        Path unpackedFilePath;
        long start;
        start = System.currentTimeMillis();
        unpackedFileName = fileName.substring(0, fileName.length() - 3);
        unpackedFilePath = filePath.getParent()
                .resolve(String.valueOf(Thread.currentThread().getId()))
                .resolve(unpackedFileName);
        Main.verboseInfo("Start processing " +  fileName + ". " +
                processedFiles.get() + "/" +  DBDownloader.getFileNo().get() + " files processed. " +
                WorkSubmitter.getDownloadedFileNo().get() + "/" + DBDownloader.getFileNo().get() + " files downloaded", 2);
        if(isUnchanged(filePath)){
            cleanUp(start, filePath);
            Main.verboseInfo(filePath.getFileName() + " is unchanged!!!", 3);
            return;
        }
        unGzipFile(filePath, unpackedFilePath);
        DBDownloader.records.addAll(computeStructure(unpackedFilePath));
        DBDownloader.saveQueueToFile(DBDownloader.records, DBDownloader.newRecordsPath);
        cleanUp(start, unpackedFilePath);
    }

    public void cleanUp(Long start, Path filepath){
        long finish;
        if(!filepath.toFile().delete()){
            System.out.println("couldn't move " + filepath.getFileName());
            filepath.toFile().deleteOnExit();
        }
        finish = System.currentTimeMillis();
        Main.verboseInfo("Time for file " + filePath.getFileName() + ": " + (finish - start), 3);
        processedFiles.incrementAndGet();
    }

    @Override
    public Path call() {
        processFile(filePath);
        return filePath.getFileName();
    }
}
