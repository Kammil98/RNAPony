package models.parameters.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import updater.Main;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.util.logging.Level;

public class DayOfWeekConverter implements IStringConverter<DayOfWeek> {
    @Override
    public DayOfWeek convert(String s) {
        try {
            if(s.length() == 1)
                return DayOfWeek.of(Integer.parseInt(s));
            else
                return DayOfWeek.valueOf(s.toUpperCase());
        }
        catch (IllegalArgumentException | DateTimeException e){
            String msg = "Incorrect value of parameter day: " + s + ". " +
                    "Possible parameter values are 1-7 or MONDAY-SUNDAY.";
            Main.verboseInfo(msg, 1, Level.SEVERE);
            throw new ParameterException(msg);
        }

    }
}
