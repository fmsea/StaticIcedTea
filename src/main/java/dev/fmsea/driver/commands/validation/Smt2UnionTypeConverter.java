package dev.fmsea.driver.commands.validation;

import picocli.CommandLine.ITypeConverter;
import dev.fmsea.processing.Smt2UnionType;

public class Smt2UnionTypeConverter implements ITypeConverter<Smt2UnionType> {
    public Smt2UnionType convert(String value) throws Exception {
        if ("connected".equals(value.toLowerCase())) {
            return Smt2UnionType.CONNECTED;
        } else if ("reachable".equals(value.toLowerCase())) {
            return Smt2UnionType.REACHABLE;
        } else {
            return Smt2UnionType.REACHABLE;
        }
    }
}
