package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;

public class OrSmtExpression extends ConnectiveSmtExpression {

    public OrSmtExpression() {
        super();
    }

    public OrSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    @Override
    public String toSmt2() {
        return this.toSmt2("or");
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.toSmt2(variables, "or");
    }
}
