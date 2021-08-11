package models.parameters.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import updater.Main;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;

public class LocalDateTimeConverter implements IStringConverter<LocalDateTime> {

    @Override
    public LocalDateTime convert(String val) {
        LocalTime time;
        LocalDateTime dateTime;
        try {
            time = LocalTime.parse(val);
            dateTime = LocalDateTime.now()
                    .withHour(time.getHour())
                    .withMinute(time.getMinute())
                    .withSecond(time.getSecond());
        }
        catch (DateTimeParseException e){
            String msg = "Incorrect format of time: " + val + ". Correct format is: hh:mm:ss. ";
            Main.verboseInfo(msg, 1, Level.SEVERE);
            throw new ParameterException(msg);
        }

        return dateTime;
    }
}
