package updater;

import com.beust.jcommander.Parameter;
import lombok.Getter;
import lombok.Setter;
import models.parameters.PreprocessType;
import models.parameters.converters.PreprocessTypeConverter;
import utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Preprocessor {

    public static final Path preprocessOutDir = Path.of("frabase_update", "RNApony_cif_files");
    @Getter
    private static final AtomicInteger filesWithAllModelsEmptyNo = new AtomicInteger();
    @Setter
    @Parameter(names = {"--type", "-t"}, converter = PreprocessTypeConverter.class,
            description = "If equals all, then all models in file are processed." +
                    "If equals first, then only first model in file is processed. Possible types are: all, first.")
    private static PreprocessType preprocessType = PreprocessType.ALL;
    public Path extractRNA(final Path filePath){
        URL preprocesing3dUrl = getClass().getResource("/preprocesing3d.py");
        Path outDir = preprocessOutDir.resolve(String.valueOf(Thread.currentThread().getId()));
        InputStream info;
        String msg;

        String command = "python3 " + Objects.requireNonNull(preprocesing3dUrl).getPath()
                + " " + filePath.toAbsolutePath()
                + " " + outDir.toAbsolutePath()
                + " " + preprocessType.toString();

        Utils.createDirIfNotExist(outDir.toFile(), Main.stdLogger);
        info = Utils.execCommand(command, Main.stdLogger, Main.errLogger);
        try {
            msg = new String(info.readAllBytes(), StandardCharsets.UTF_8);
            if(msg.contains("PrintMsg:[")){
                msg = msg.replaceAll("PrintMsg:", "");
                Main.verboseInfo( msg, 2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outDir;
    }
}
