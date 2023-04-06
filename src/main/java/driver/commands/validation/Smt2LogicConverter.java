package driver.commands.validation;

import picocli.CommandLine.ITypeConverter;
import solver.Smt2Logic;

public class Smt2LogicConverter implements ITypeConverter<Smt2Logic> {
    public Smt2Logic convert(String value) throws Exception {
        if ("QF_LIA".equals(value.toUpperCase())) {
            return Smt2Logic.QF_LIA;
        } else if ("LIA".equals(value.toUpperCase())) {
            return Smt2Logic.LIA;
        } else if ("NIA".equals(value.toUpperCase())) {
            return Smt2Logic.NIA;
        } else if ("UFNIA".equals(value.toUpperCase())) {
            return Smt2Logic.UFNIA;
        } else if ("AUFNIRA".equals(value.toUpperCase())) {
            return Smt2Logic.AUFNIRA;
        } else {
            return Smt2Logic.LIA;
        }
    }
}
