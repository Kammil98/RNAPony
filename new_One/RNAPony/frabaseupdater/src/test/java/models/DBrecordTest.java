package models;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import updater.Main;
import utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DBrecordTest {

    private static DBrecord record;

    @BeforeEach
    public void setUp(){
        record = new DBrecord();
    }

    private void deleteOutDir(Path outPath){
        if(outPath.getFileName().toString().equals("frabase_update") &&
                outPath.toAbsolutePath().getParent().getFileName().toString().equals("frabaseupdater")){//in case of fatal mistake
            try {
                FileUtils.deleteDirectory(outPath.toFile());
                outPath.resolve("errApp.txt").toFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Main.stdLogger.info("Couldn't delete file after tests in " + DBrecordTest.class.getName());
        }
    }

    private void createResolFile(File resolFileDir){
        Utils.createDirIfNotExist(resolFileDir.getParentFile());
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(resolFileDir))) {
            bw.write("PROTEIN DATA BANK LIST OF IDCODES AND DATA RESOLUTION VALUES\n" +
                    "Fri Jul 16 13:39:10 EDT 2021\n" +
                    "RESOLUTION VALUE IS -1.00 FOR ENTRIES DERIVED FROM NMR AND OTHER EXPERIMENT METHODS (NOT INCLUDING X-RAY) IN WHICH THE FIELD REFINE.LS_D_RES_HIGH IS EMPTY\n" +
                    "\n" +
                    "IDCODE       RESOLUTION\n" +
                    "------  -    ----------\n" +
                    "100D\t;\t1.9\n" +
                    "102L\t;\t1.74\n" +
                    "1ZZ5\t;\t3.00\n" +
                    "106D\t;\t-1.00");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void computeRecord() {
        File resolFileDir = Main.frabaseDir.resolve("resolu.idx").toFile();
        createResolFile(resolFileDir);

        //based on PDB: 1zz5
        DotFile dotFile = new DotFile("A_B_C_D", "GUGGUGAAGUCGCGG;CGCGUCACACCACC;GUGGUGAAGUCGCGG;CGCGUCACACCACC;",
            "((((((..(.((((..)))).).))))))[.((((((..(.((((].)))).).))))))..");
        record.computeRecord(dotFile, "1zz5_1.cif");
        assertEquals("1ZZ5", record.getId());
        assertEquals(1, record.getModelNo());
        assertEquals("A_B_C_D", record.getChain());
        assertEquals(3.0, record.getResol());
        assertEquals("((((((..(.((((..)))).).))))))[.((((((..(.((((].)))).).))))))..", record.getDot());
        assertEquals("GUGGUGAAGUCGCGG;CGCGUCACACCACC;GUGGUGAAGUCGCGG;CGCGUCACACCACC;", record.getSeq());
        assertEquals("28;26;24;22;20;18;0;0;13;0;9;7;5;3;0;0;-3;-5;-7;-9;0;-13;0;-18;-20;-22;-24;-26;-28;16;0;" +
                "28;26;24;22;20;18;0;0;13;0;9;7;5;3;-16;0;-3;-5;-7;-9;0;-13;0;-18;-20;-22;-24;-26;-28;0;0",
                record.getDotIntervals());
        assertEquals(1, record.getMaxOrder());

        deleteOutDir(resolFileDir.getParentFile().toPath());
    }

    @Test
    void testToString() {
    }
}