package updater;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.DBrecord;
import models.DotFile;
import utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
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
        if(fileList.length == 0)//statistics to print
            Preprocessor.getFilesWithAllModelsEmptyNo().incrementAndGet();
        else
            processedModels.addAndGet(fileList.length);
        for(String newFileName: fileList){
            record = computeModel(preprocessedFileDir.resolve(newFileName));
            if(record != null)
                records.add(record);
        }
        return records;
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

    public void processFile(Path filePath){
        String unpackedFileName, fileName = filePath.getFileName().toString();
        Path unpackedFilePath;
        long start, finish;
        start = System.currentTimeMillis();
        unpackedFileName = fileName.substring(0, fileName.length() - 3);
        unpackedFilePath = filePath.getParent()
                .resolve(String.valueOf(Thread.currentThread().getId()))
                .resolve(unpackedFileName);
        Main.verboseInfo("Start processing " +  fileName + ". " +
                processedFiles.get() + "/" +  DBUpdater.getFileNo().get() + " files processed. " +
                WorkSubmitter.getFileNo().get() + "/" + DBUpdater.getFileNo().get() + "files downloaded", 2);
        unGzipFile(filePath, unpackedFilePath);
        if(!filePath.toFile().delete()){
            filePath.toFile().deleteOnExit();
        }
        DBUpdater.records.addAll(computeStructure(unpackedFilePath));
        DBUpdater.saveRecordsToFile();
        if(!unpackedFilePath.toFile().delete()){
            unpackedFilePath.toFile().deleteOnExit();
        }
        finish = System.currentTimeMillis();
        Main.verboseInfo("Time for file " + fileName + ": " + (finish - start), 3);
        processedFiles.incrementAndGet();
    }

    @Override
    public Path call() throws Exception {
        processFile(filePath);
        return filePath.getFileName();
    }
}
