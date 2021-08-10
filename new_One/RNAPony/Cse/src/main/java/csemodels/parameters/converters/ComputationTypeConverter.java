package csemodels.parameters.converters;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import csemodels.parameters.ComputationType;

import java.util.Arrays;

public class ComputationTypeConverter implements IStringConverter<ComputationType> {
    @Override
    public ComputationType convert(String s) {
        ComputationType type;
        try {
            type = ComputationType.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ParameterException("Incorrect value of parameter type: \"" + s + "\". " +
                    "Possible parameter values are " + Arrays.toString(ComputationType.values()));
        }
        return type;
    }
}
