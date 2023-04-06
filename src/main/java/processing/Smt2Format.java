package processing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processing.util.FlowSet;
import solver.Smt2Logic;

public class Smt2Format {
    private static String resultsPathFull;
    private static String resultsPathCombined;
    private static String smt2FilesPath;
    private static Logger LOGGER = LoggerFactory.getLogger(Smt2Format.class);

    /**
     * Creates an smt2 formula to if file1Name implies file2Name
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String className = "test.MapViewer";
        String methodId = "2";
        String domain = "_dom5.txt";
        String type = "c1";
        String dataPath = "./ScratchData/";
        if (args.length > 0) {
            dataPath = args[0];
            className = args[1];
            methodId = args[2];
            domain = "_" + args[3] + ".txt";
            type = args[4];
        }
        resultsPathFull = dataPath + "/resultsVA/invariants/";
        resultsPathCombined = dataPath + "/resultsVA/combined/" + type + "/";
        smt2FilesPath = dataPath + "/resultsVA/smt2Files/" + type + "/";
        // get the file with the number of paths
        String pathFileName = dataPath + "/conditions/paths/" + className + "_" + methodId + ".txt";
        File pathFile = new File(pathFileName);
        if (pathFile.exists()) {
            Scanner sPath = new Scanner(new FileReader(pathFile));
            String fullPath = className + "_" + methodId + domain;
            int pathId = 1;
            while (sPath.hasNextLine()) {
                String l = sPath.nextLine();
                if (!l.isEmpty()) {
                    // call the Smt2Format
                    String combinedPath =
                            className + "_" + methodId + "_" + String.valueOf(pathId) + domain;
                    LOGGER.info(combinedPath + " " + fullPath);
                    SMT2Format(combinedPath, fullPath);
                    pathId++;
                }
            }
            sPath.close();
        } else {
            LOGGER.warn("Cannot find {}", pathFileName);
        }


        // new Smt2Format(file1Name, file2Name);
    }

    public static void SMT2Format(String file1Name, String file2Name) throws IOException {
        File file1 = new File(resultsPathCombined + file1Name);
        File file2 = new File(resultsPathFull + file2Name);
        String outputFileName = smt2FilesPath + file1Name + "_VS_" + file2Name;
        SMT2Format(new FileReader(file1), new FileReader(file2), new FileWriter(outputFileName));
    }

    public static void SMT2Format(Reader reader1, Reader reader2, Writer writer) throws IOException {
        AnalysisSMTReport report1 = Smt2Reader.parse(reader1);
        AnalysisSMTReport report2 = Smt2Reader.parse(reader2);
        Set<String> allVariables = new HashSet<>();
        allVariables.addAll(report1.variables());
        allVariables.addAll(report2.variables());
        Set<String> statements = new TreeSet<>();
        statements.addAll(report1.statements());
        statements.addAll(report2.statements());
        List<String> sortedStatements = sortStatements(statements);
        writer.write("(set-logic LIA)\n");
        for (String statement : sortedStatements) {
            writer.write("(echo \"");
            writer.write(statement.replaceAll("\"", "\"\""));
            writer.write("\")\n");
            Optional<String> fall1 = report1.getFallThrough(statement);
            Optional<String> branch1 = report1.getBranchOut(statement);
            Optional<String> fall2 = report2.getFallThrough(statement);
            Optional<String> branch2 = report2.getBranchOut(statement);
            if (fall1.isEmpty() && fall2.isEmpty()) {
                // skip
            } else {
                Set<String> variables = new HashSet<>();
                variables.addAll(Smt2Reader.getIdentifiers(fall1.orElse("true")));
                variables.addAll(Smt2Reader.getIdentifiers(fall2.orElse("true")));
                writer.write("(echo \"fall through\")\n");
                writer.write(formatImplies(variables,
                                           fall1.orElse("true"),
                                           fall2.orElse("true")));
                writer.write(formatImplies(variables,
                                           fall2.orElse("true"),
                                           fall1.orElse("true")));
            }

            if (branch1.isEmpty() && branch2.isEmpty()) {
                // skip
            } else {
                Set<String> variables = new HashSet<>();
                variables.addAll(Smt2Reader.getIdentifiers(branch1.orElse("true")));
                variables.addAll(Smt2Reader.getIdentifiers(branch2.orElse("true")));
                writer.write("(echo \"branch out\")\n");
                writer.write(formatImplies(variables,
                                           branch1.orElse("true"),
                                           branch2.orElse("true")));
                writer.write(formatImplies(variables,
                                           branch2.orElse("true"),
                                           branch1.orElse("true")));
            }
        }
        writer.flush();
        writer.close();
    }

    public static void SMT2FormatIdentifiers(Reader reader, Writer writer) throws IOException {
        Map<String, FlowSet<String>> statementIdentifierMap = Smt2Reader.getIdentifiersPerStatement(reader);
        for (String statement : statementIdentifierMap.keySet()) {
            writer.write(statement);
            FlowSet<String> flowSet = statementIdentifierMap.get(statement);
            writer.write("\tfall");
            for (String identifier : flowSet.getFallThrough()) {
                writer.write("\t");
                writer.write(identifier);
            }
            writer.write("\n");
            writer.write(statement);
            writer.write("\tbranch");
            for (String identifier : flowSet.getBranchOut()) {
                writer.write("\t");
                writer.write(identifier);
            }
            writer.write("\n");
        }
    }

    // private static String formatConstraints(List<SmtTypedExpression> exprs1, List<SmtTypedExpression> exprs2) {
    //     StringBuilder sb = new StringBuilder();
    //     // Collections.sort(exprs1, (a, b) -> a.identifier.compareTo(b.identifier));
    //     // Collections.sort(exprs2, (a, b) -> a.identifier.compareTo(b.identifier));
    //     BiFunction<Integer, List<SmtTypedExpression>, SmtTypedExpression> getExpr = (index, exprs) -> {
    //         if (index >= exprs.size()) {
    //             return new SmtTypedExpression("empty", new HashSet<String>(), "(= 0 0)");
    //         } else {
    //             return exprs.get(index);
    //         }
    //     };
    //     int len = Math.max(exprs1.size(), exprs2.size());
    //     for (int i = 0; i < len; i++) {
    //         SmtTypedExpression expr1 = getExpr.apply(i, exprs1);
    //         SmtTypedExpression expr2 = getExpr.apply(i, exprs2);
    //         sb.append(Smt2Format.formatConstraint(expr1, expr2));
    //     }
    //     return sb.toString();
    // }

    // protected static String formatConstraint(SmtTypedExpression expr1, SmtTypedExpression expr2) {
    //     LOGGER.debug("formatting constraint {} <=> {}", expr1, expr2);
    //     StringBuilder sb = new StringBuilder();
    //     // local
    //     sb.append("(echo \"");
    //     sb.append(getIdentifier(expr1.identifier, expr2.identifier));
    //     sb.append("\")\n");
    //     // expression
    //     Set<String> vars = new TreeSet<>();
    //     // UNION identifiers
    //     vars.addAll(expr1.identifiers);
    //     vars.addAll(expr2.identifiers);
    //     String forward = Smt2Format.formatImplies(vars, expr1.expression, expr2.expression);
    //     String backward = Smt2Format.formatImplies(vars, expr2.expression, expr1.expression);
    //     sb.append(forward);
    //     sb.append(backward);

    //     LOGGER.debug("forward: {} -> backward {}", forward, backward);
    //     return sb.toString();
    // }

    protected static String formatImplies(Set<String> vars, String from, String to) {
        StringBuilder sb = new StringBuilder();
        sb.append("(push)\n");
        sb.append("(assert ");
        if (vars.size() > 0) {
            sb.append("(forall (");
            vars.stream().sorted().forEach(var -> {
                    sb.append("(");
                    sb.append(var);
                    sb.append(" Int)");
                });
            sb.append(")\n");
        }
        sb.append("(=> ");
        sb.append(from);
        sb.append(" ");
        sb.append(to);
        if (vars.size() > 0) {
            sb.append(")))\n");
        } else {
            sb.append("))\n");
        }
        sb.append("(check-sat)\n(pop)\n");
        return sb.toString();
    }

    protected static String formatImplies(String from, String to) {
        StringBuilder sb = new StringBuilder();
        sb.append("(push)\n");
        sb.append("(assert\n (=> ");
        sb.append(from);
        sb.append("\n     ");
        sb.append(to);
        sb.append("))\n");
        sb.append("(check-sat)\n");
        sb.append("(pop)\n");
        return sb.toString();
    }

    private static String formatVariables(Set<String> vars) {
        StringBuilder sb = new StringBuilder();
        vars.stream().sorted().forEach(v -> {
                sb.append("(declare-const ");
                sb.append(v);
                sb.append(" Int)\n");
            });
        return sb.toString();
    }

    protected static String getIdentifier(String expr1, String expr2) {
        String identifier = "empty";
        if (expr1.equals(expr2)) {
            identifier = expr1;
        } else if (expr1.equals("empty") && !expr2.equals("empty")) {
            identifier = expr2;
        } else if (expr2.equals("empty") && !expr1.equals("empty")) {
            identifier = expr1;
        }
        return identifier;
    }

    protected static List<String> sortStatements(Set<String> statements) {
        return statements.stream().sorted((a, b) -> {
                try {
                    int aCount = Integer.parseInt(a.split(" ")[0]);
                    int bCount = Integer.parseInt(b.split(" ")[0]);
                    return Integer.compare(aCount, bCount);
                } catch (NumberFormatException ex) {
                    LOGGER.error("Exception during parsing: {}", ex);
                    return a.compareTo(b);
                }
            }).collect(Collectors.toList());
    }

    protected static String convertLogicToString(Smt2Logic logic) {
        switch (logic) {
        case QF_LIA:
            return "QF_LIA";
        case LIA:
            return "LIA";
        case NIA:
            return "NIA";
        case UFNIA:
            return "UFNIA";
        case AUFNIRA:
        default:
            return "AUFNIRA";
        }
    }
}


/*
 * (echo "statement 1")
 * (echo "i4")
 * (push)
 * (assert
 * (forall ((i4 Int))
 *  (=> (or (>= i4 0) (= i4 0) (<= i4 0))
 *     (>= i4 0)
 *   )
 * )
 * )
 *  (check-sat)
 *  (pop)
 *  (push)
 *  (assert
 *  (forall ((i4 Int))
 *    (=> (>= i4 0)
 *      (or (>= i4 0) (= i4 0) (<= i4 0))
 *    )
 *  )
 *  )
 * (check-sat)
 * (pop)
*/
