package updater;

import models.DBrecord;
import models.DotFile;
import utils.Utils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

public class DBUpdater {



    /**
     * Download file with new structures in PDB.
     * @return Path to downloaded file.
     */
    public Path downloadChangeList(){
        File outDir = Main.frabaseDir.toFile();
        Path outPath = Path.of(Main.frabaseDir.toString(), "pdb_entry_type.txt");
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
        Path outPath = Path.of(Main.frabaseDir.toString(), "resolu.idx");
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
        Path changedIdListPath = Path.of(this.getClass().getResource("/").getPath(), "changedIds.txt");
        Path changedListPath = downloadChangeList();
        String line, id, type;
        StringTokenizer structure;
        int counter = 0;

        Main.stdLogger.info("Downloading list of resolutions of new structures.");
        downloadResolList();
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
            Main.errLogger.severe("Couldn't find output .dbn file: resolu.idx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.stdLogger.info(counter + " RNA structures found");
        return changedIdListPath;
    }

    public File downloadAndUpdateNewStructures(){
        File outDir = Path.of(Main.frabaseDir.toString(), "3DStructures").toFile();
        Utils.createDirIfNotExist(outDir, Main.stdLogger);

        Main.stdLogger.info("Downloading list of new structures.");
        Path newStrucListPath = getNewStructuresList();
        Main.stdLogger.info("Downloading new nuc and prot-nuc structures.");
        Path downloadScriptPath = Path.of(
                Objects.requireNonNull(getClass().getResource("/batch_download.sh")).getPath());
        Utils.execCommand(downloadScriptPath + " -f " + newStrucListPath + " -o " + outDir.getAbsolutePath() + " -c",
                true,
                Main.stdLogger,
                Main.errLogger);
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

    public ArrayList<DBrecord> computeRecord(Path filePath){
        ArrayList<DBrecord> records = new ArrayList<>();
        DataLoader loader = new DataLoader();
        DotFile dotFile;
        DBrecord record;
        Path preprocessedFilePath;
        Main.stdLogger.info("Preprocess " +  filePath);
        long start = System.currentTimeMillis();
        new Preprocessor().extractRNA(filePath);
        long finish = System.currentTimeMillis();
        Main.stdLogger.info("Time for preprocess: " + (finish - start));
        String[] fileList = Objects.requireNonNull(Preprocessor.preprocessOutDir.toFile().list());
        for(String newFileName: fileList){
            preprocessedFilePath = Path.of(Preprocessor.preprocessOutDir.toString(), newFileName);
            start = System.currentTimeMillis();
            dotFile = loader.getDotFile(preprocessedFilePath);
            finish = System.currentTimeMillis();
            Main.stdLogger.info("Time for RNApdbee: " + (finish - start));
            if(isOnlyDots(dotFile.getDot())){
                Main.stdLogger.info(newFileName + " doesn't contain pairs - omitting this structure.");
                continue;
            }
            start = System.currentTimeMillis();
            record = new DBrecord();
            Main.stdLogger.info("Compute record");
            record.computeRecord(dotFile, preprocessedFilePath.getFileName().toString());
            records.add(record);
            finish = System.currentTimeMillis();
            Main.stdLogger.info("Time for computing record: " + (finish - start));
            Main.stdLogger.info("Record created\n");
            Path.of(Preprocessor.preprocessOutDir.toString(), newFileName).toFile().delete();
        }
        if(records.size() > 1)
            Main.stdLogger.info("!!!!!!!!!!!!!!!!FOUND 2 records for " + filePath + "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return records;
    }

    public void saveRecordsToFile(ArrayList<DBrecord> records){
        try(BufferedWriter bw = new BufferedWriter(
                new FileWriter(Path.of(Main.frabaseDir.toString(), "DBrecords.txt").toString(), true))) {
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

    private void unGzipFile(String compressedFile, String decompressedFile) {
        byte[] buffer = new byte[1024];
        int bytes_read;

        try(GZIPInputStream gZIPInputStream = new GZIPInputStream(new FileInputStream(compressedFile));
            FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile)) {
            while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, bytes_read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Compute records for all 3D structures under
     * given directory
     * @param dir directory with 3D structures
     */
    public void updateDB(final File dir){
        ArrayList<DBrecord> records = new ArrayList<>();
        prepareDBFile();
        String unpackedFileName;
        Path unpackedFilePath;
        String[] fileList = Objects.requireNonNull(dir.list());
        long start, finish;
        for(String file: fileList){
            start = System.currentTimeMillis();
            unpackedFileName = file.substring(0, file.length() - 3);
            unpackedFilePath = Path.of(dir.toString(), unpackedFileName);
            Main.stdLogger.info("Processing " +  file);
            unGzipFile(Path.of(dir.toString(), file).toString(), unpackedFilePath.toString());
            records.addAll(computeRecord(unpackedFilePath));
            if(records.size() > 1000){
                saveRecordsToFile(records);
                records.clear();
            }
            if(!unpackedFilePath.toFile().delete()){
                unpackedFilePath.toFile().deleteOnExit();
            }
            finish = System.currentTimeMillis();
            Main.stdLogger.info("Time for whole file: " + (finish - start));
        }
        if(!records.isEmpty())
            saveRecordsToFile(records);
    }
}
