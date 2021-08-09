package updater;

import models.DBrecord;
import org.junit.jupiter.api.Test;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class WorkerTest {

    @Test
    void computeModel() {
        DBrecord result, expected = DBrecord.valueOf("157d 1 A_B 1.8 CGCGAAUUAGCG;CGCGAAUUAGCG; " +
                "(((.((((.(((.))).)))).))). " +
                "24;22;20;0;16;14;12;10;0;6;4;2;0;-2;-4;-6;0;-10;-12;-14;-16;0;-20;-22;-24;0 0");
        File file = new File(WorkerTest.class.getResource("/157d_1.cif").getPath());
        File resoluFile = new File(WorkerTest.class.getResource("/resolu.idx").getPath());

        Utils.createDirIfNotExist(Main.frabaseDir.toFile());
        try {
            Files.copy(file.toPath(), Main.frabaseDir.resolve("157d_1.cif"));//copy to delete after processing
            Files.copy(resoluFile.toPath(), Main.frabaseDir.resolve("resolu.idx"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

        Worker worker = new Worker(null);//file path to zipped file is unnecessary in this test
        result = worker.computeModel(Main.frabaseDir.resolve("157d_1.cif"));
        DotFileCreatorTest.deleteOutDir(Main.frabaseDir, 0);
        assertEquals(expected, result);
    }

    @Test
    void processFile() {
        DBrecord record = DBrecord.valueOf("157d 1 A_B 1.8 CGCGAAUUAGCG;CGCGAAUUAGCG; (((.((((.(((.))).)))).))). " +
                "24;22;20;0;16;14;12;10;0;6;4;2;0;-2;-4;-6;0;-10;-12;-14;-16;0;-20;-22;-24;0 0");
        File file = new File(WorkerTest.class.getResource("/157d.cif.gz").getPath());
        File resoluFile = new File(WorkerTest.class.getResource("/resolu.idx").getPath());
        Utils.createDirIfNotExist(Main.frabaseDir.resolve("3DStructures").toFile());
        Utils.createDirIfNotExist(Main.frabaseDir.resolve("oldFiles").toFile());
        Utils.createDirIfNotExist(Main.frabaseDir.resolve("RNApony_cif_files").toFile());

        try {
            Files.copy(file.toPath(), Main.frabaseDir.resolve("3DStructures").resolve("157d.cif.gz"));
            Files.copy(resoluFile.toPath(), Main.frabaseDir.resolve("resolu.idx"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        Worker worker = new Worker(Main.frabaseDir.resolve("3DStructures").resolve("157d.cif.gz").toAbsolutePath());
        worker.call();
        DotFileCreatorTest.deleteOutDir(Main.frabaseDir, 0);
        assertEquals(1, DBDownloader.records.size());
        assertEquals(record, DBDownloader.records.poll());
    }
}