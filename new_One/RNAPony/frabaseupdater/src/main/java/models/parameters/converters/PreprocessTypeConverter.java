package models.parameters.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import models.parameters.PreprocessType;

import java.util.Arrays;

public class PreprocessTypeConverter  implements IStringConverter<PreprocessType> {
    @Override
    public PreprocessType convert(String s) {
        PreprocessType type;
        try {
            type = PreprocessType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParameterException("Incorrect value of parameter type: \"" + s + "\". " +
                    "Possible parameter values are " + Arrays.toString(PreprocessType.values()));
        }
        return type;
    }
}
