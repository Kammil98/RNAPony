package updater;

import models.parameters.PreprocessType;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesReaderTest {

    @Test
    void loadProperties() {
        String[] args = new String[]{"-c",
                Objects.requireNonNull(PropertiesReaderTest.class.getResource("/customConfig.properties"))
                        .getPath(),
                "--type",
                "all"};
        PropertiesReader.loadProperties(args);
        assertEquals(5, Main.dayOfWeek);
        assertEquals(LocalTime.of(2, 15, 0), Main.time);
        assertEquals(PreprocessType.ALL, Preprocessor.getPreprocessType());
        Preprocessor.setPreprocessType(PreprocessType.FIRST);
        Main.dayOfWeek = 4;
        Main.time = LocalTime.of(0, 10, 0);
    }
}