package processing.smt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import soot.Local;

public class AndSmtExpression extends ConnectiveSmtExpression {

    public AndSmtExpression() {
        super();
    }

    public AndSmtExpression(List<SmtExpression> expressions) {
        super(expressions);
    }

    @Override
    public String toSmt2() {
        return this.toSmt2("and");
    }

    public Optional<String> toSmt2(Set<Local> variables) {
        return this.toSmt2(variables, "and");
    }
}
