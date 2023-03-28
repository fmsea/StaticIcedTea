package driver.commands.validation;

import picocli.CommandLine.ITypeConverter;
import processing.Smt2FormatType;

public class Smt2FormatTypeConverter implements ITypeConverter<Smt2FormatType> {
    public Smt2FormatType convert(String value) throws Exception {
        if ("full".equals(value.toLowerCase())) {
            return Smt2FormatType.FULL;
        } else if ("min".equals(value.toLowerCase())) {
            return Smt2FormatType.MIN;
        } else {
            return Smt2FormatType.MIN;
        }
    }
}
