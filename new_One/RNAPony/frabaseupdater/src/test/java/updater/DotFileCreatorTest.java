package updater;

import models.DotFile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DotFileCreatorTest {

    private static final DotFileCreator creator = new DotFileCreator();

    public static void deleteOutDir(Path outPath, int pathDepths){
        Path dirToDelete = outPath;
        for(int i = 0; i < pathDepths; i++)
            dirToDelete = dirToDelete.getParent();
        if(dirToDelete.getFileName().toString().equals("frabase_update") &&
                dirToDelete.toAbsolutePath().getParent().getFileName().toString().equals("frabaseupdater")){//in case of fatal mistake
            try {
                FileUtils.deleteDirectory(dirToDelete.toFile());
                dirToDelete.toAbsolutePath().getParent().resolve("errApp.txt").toFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Main.stdLogger.info("Couldn't delete file after tests in " + DotFileCreatorTest.class.getName());
        }
    }

    @Test
    void createDotFile() {
        Path path = Path.of(Objects.requireNonNull(getClass().getResource("/1zz5.cif")).getPath());
        Path outPath = creator.createDotFile(path);
        boolean isEqual = false;
        try {
            isEqual = FileUtils.contentEquals(outPath.resolve("0").resolve("strands.dbn").toFile(),
                    new File(Objects.requireNonNull(getClass().getResource("/strands.dbn")).getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        DotFileCreatorTest.deleteOutDir(outPath, 2);
        assertTrue(isEqual);
    }

    @Test
    void readDotFile() {
        Path path = Path.of(Objects.requireNonNull(getClass().getResource("/1zz5.cif")).getPath());
        Path outPath = creator.createDotFile(path);
        DotFile dotFile = creator.readDotFile(outPath.resolve("0").resolve("strands.dbn"));
        DotFile expectedDotFile = new DotFile("A_B_C_D", "GUGGUGAAGUCGCGG;CGCGUCACACCACC;GUGGUGAAGUCGCGG;CGCGUCACACCACC;",
                "((((((..(.((((..)))).).))))))[.((((((..(.((((].)))).).))))))..");
        assertEquals(expectedDotFile.getName(), dotFile.getName());
        assertEquals(expectedDotFile.getSeq(), dotFile.getSeq());
        assertEquals(expectedDotFile.getDot(), dotFile.getDot());
        DotFileCreatorTest.deleteOutDir(outPath, 2);
    }
}