package updater;

import models.parameters.PreprocessType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
        assertEquals(DayOfWeek.FRIDAY, Main.dayOfWeek);
        assertEquals(2, Main.time.getHour());
        assertEquals(15, Main.time.getMinute());
        assertEquals(0, Main.time.getSecond());
        assertEquals(PreprocessType.ALL, Preprocessor.getPreprocessType());
        Preprocessor.setPreprocessType(PreprocessType.FIRST);
        Main.dayOfWeek = DayOfWeek.THURSDAY;
        Main.time = LocalDateTime.now()
                .with(TemporalAdjusters.nextOrSame(Main.dayOfWeek))
                .withHour(0)
                .withMinute(10)
                .withSecond(0);
        Preprocessor.setLastPreprocessType(PreprocessType.FIRST);
        Preprocessor.setPreprocessType(PreprocessType.FIRST);

    }
}