package models.parameters.converters;

import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.*;

class DayOfWeekConverterTest {


    @Test
    void convert() {
        DayOfWeekConverter converter = new DayOfWeekConverter();
        assertEquals(DayOfWeek.MONDAY, converter.convert("MONDAY"));
        assertEquals(DayOfWeek.MONDAY, converter.convert("1"));
        assertEquals(DayOfWeek.SUNDAY, converter.convert("7"));
        assertEquals(DayOfWeek.FRIDAY, converter.convert("FRIDAY"));
        assertEquals(DayOfWeek.TUESDAY, converter.convert("tuesday"));
        assertThrows(ParameterException.class, () -> converter.convert("MONDA"));
        assertThrows(ParameterException.class, () -> converter.convert("8"));
    }
}