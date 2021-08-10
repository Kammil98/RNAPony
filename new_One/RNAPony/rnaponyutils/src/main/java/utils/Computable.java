package utils;

import java.nio.file.Path;

public interface Computable {
    void compute(String fileName);
    void changeLogFile(Path resultPath);
}
