package updater;

import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class Worker implements Callable<Byte> {
    private final Path filePath;

    @Override
    public Byte call() throws Exception {
        new DBUpdater().processFile(filePath);
        return 1;
    }
}
