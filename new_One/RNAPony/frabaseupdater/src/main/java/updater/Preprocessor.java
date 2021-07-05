package updater;

import utils.Utils;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class Preprocessor {

    public static final Path preprocessOutDir = Path.of("frabase_update", "RNApony_cif_files");

    public void extractRNA(final Path filePath){
        URL preprocesing3dUrl = getClass().getResource("/preprocesing3d.py");
        String command = "python3 " + Objects.requireNonNull(preprocesing3dUrl).getPath()
                + " " + filePath.toAbsolutePath()
                + " " + preprocessOutDir.toAbsolutePath();

        File outDir = preprocessOutDir.toFile();
        Utils.createDirIfNotExist(outDir, Main.stdLogger);

        Utils.execCommand(command, Main.stdLogger, Main.errLogger);
    }
}
