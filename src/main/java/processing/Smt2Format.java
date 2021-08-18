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
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        Map<String, List<SmtExpression>> file1Map = Smt2Reader.parse(reader1);
        Map<String, List<SmtExpression>> file2Map = Smt2Reader.parse(reader2);
        Set<String> keys = new TreeSet<>();
        keys.addAll(file1Map.keySet());
        keys.addAll(file2Map.keySet());
        for (String key : keys) {
            writer.write("(echo \"");
            writer.write(key);
            writer.write("\")\n");
            List<SmtExpression> exprs1 = file1Map.get(key);
            List<SmtExpression> exprs2 = file2Map.get(key);
            if (exprs1 == null && exprs2 == null) {
                // nothing to do, carry on?
            } else if (exprs1 == null && exprs2 != null) {
                writer.write(formatConstraints(Collections.emptyList(), exprs2));
            } else if (exprs2 == null && exprs1 != null) {
                writer.write(formatConstraints(exprs1, Collections.emptyList()));
            } else {
                writer.write(formatConstraints(exprs1, exprs2));
            }
        }
        writer.flush();
        writer.close();
    }

    private static String formatConstraints(List<SmtExpression> exprs1, List<SmtExpression> exprs2) {
        StringBuilder sb = new StringBuilder();
        Collections.sort(exprs1, (a, b) -> a.identifier.compareTo(b.identifier));
        Collections.sort(exprs2, (a, b) -> a.identifier.compareTo(b.identifier));
        BiFunction<Integer, List<SmtExpression>, SmtExpression> getExpr = (index, exprs) -> {
            if (index >= exprs.size()) {
                return new SmtExpression("empty", new HashSet<String>(), "(= 0 0)");
            } else {
                return exprs.get(index);
            }
        };
        int len = Math.max(exprs1.size(), exprs2.size());
        for (int i = 0; i < len; i++) {
            SmtExpression expr1 = getExpr.apply(i, exprs1);
            SmtExpression expr2 = getExpr.apply(i, exprs2);
            sb.append(Smt2Format.formatConstraint(expr1, expr2));
        }
        return sb.toString();
    }

    private static String formatConstraint(SmtExpression expr1, SmtExpression expr2) {
        LOGGER.debug("formatting constraint {} <=> {}", expr1, expr2);
        StringBuilder sb = new StringBuilder();
        // local
        sb.append("(echo \"");
        sb.append(getIdentifier(expr1.identifier, expr2.identifier));
        sb.append("\")\n");
        // expression
        Set<String> vars = new TreeSet<>();
        // UNION identifiers
        vars.addAll(expr1.identifiers);
        vars.addAll(expr2.identifiers);
        String forward = Smt2Format.formatImplies(vars, expr1.expression, expr2.expression);
        String backward = Smt2Format.formatImplies(vars, expr2.expression, expr1.expression);
        sb.append(forward);
        sb.append(backward);

        LOGGER.debug("forward: {} -> backward {}", forward, backward);
        return sb.toString();
    }

    private static String formatImplies(Set<String> vars, String from, String to) {
        StringBuilder sb = new StringBuilder();
        sb.append("(push)\n");
        sb.append("(assert (forall (");
        for (String var : vars) {
            sb.append("(");
            sb.append(var);
            sb.append(" Int)");
        }
        sb.append(")\n");
        sb.append("(=> ");
        sb.append(from);
        sb.append(" ");
        sb.append(to);
        sb.append(")))\n");
        sb.append("(check-sat)\n(pop)\n");
        return sb.toString();
    }

    private static String getIdentifier(String expr1, String expr2) {
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
