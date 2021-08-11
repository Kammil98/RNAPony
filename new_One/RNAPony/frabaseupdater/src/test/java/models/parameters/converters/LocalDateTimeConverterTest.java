package models.parameters.converters;

import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeConverterTest {

    private boolean isTimeEqual(LocalTime expected, LocalDateTime actual){
        return (expected.getHour() == actual.getHour()) &&
                (expected.getMinute() == actual.getMinute()) &&
                (expected.getSecond() == actual.getSecond());
    }

    @Test
    void convert() {
        LocalDateTimeConverter converter = new LocalDateTimeConverter();
        LocalDateTime time;
        time = converter.convert("00:10:00");
        assertTrue(isTimeEqual(LocalTime.parse("00:10:00"), time));
        time = converter.convert("15:10:13");
        assertTrue(isTimeEqual(LocalTime.parse("15:10:13"), time));

        assertThrows(ParameterException.class, () -> converter.convert("0:10:00"));
        assertThrows(ParameterException.class, () -> converter.convert("25:10:13"));
        assertThrows(ParameterException.class, () -> converter.convert("15:61:13"));
        assertThrows(ParameterException.class, () -> converter.convert("15:10:61"));
    }
}