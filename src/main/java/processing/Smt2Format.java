package processing;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
        // statement -> variable -> value_as_formula
        Map<String, Map<String, String>> file1Map = new HashMap<String, Map<String, String>>();

        // read from the first file and populate the map

        Scanner scanner = new Scanner(reader1);
        Map<String, String> stmtTo = null;
        String formula = "";
        String var = "";
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            LOGGER.debug(line);
            // if line starts as integer then
            // it is a statement
            if (line.matches("^[0-9].*")) {
                // finish the previous var
                if (stmtTo != null) {
                    // if not the first iteration
                    stmtTo.put(var, formula);
                    var = "";
                    formula = "";
                }
                LOGGER.debug("Stmt {}", line);
                // found a new statement
                // create a map for it
                stmtTo = new HashMap<String, String>();
                file1Map.put(line, stmtTo);
            } else if (line.contains("->")) {
                // when we see a new variable
                // it means that the previous one is done
                if (!formula.equals("") && !var.equals("")) {
                    // add to the current map of the statement
                    stmtTo.put(var, formula);
                }
                // it means the assignment of the variable to
                // the value
                String[] data = line.split("->");
                var = data[0];
                formula = data[1];
                LOGGER.debug("formula {}", formula);
            } else {
                // formula might have several lines
                // so all other lines are nothing else
                // but the continuation of that formula
                formula += line.trim();
            }
        } // end of the scanner loop
          // add the last var to formula
        if (stmtTo != null) {
            stmtTo.put(var, formula);
        }
        scanner.close();
        LOGGER.debug("Map of file1: {}", file1Map);

        // now traverse similarly the other file
        // only write a new formula out of it
        formula = "";
        var = "";
        String stmt = "";
        String constraint = "";
        scanner = new Scanner(reader2);
        // File to write to smt2 constraints
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            if (line.matches("^[0-9].*")) {
                if (!constraint.equals("") && stmtTo != null) {
                    // first create the constraint
                    // for the last read variable
                    constraint += writeConstraint(stmtTo, constraint, var, formula);
                    var = "";
                    formula = "";
                }
                // write to the file
                writer.write(constraint);
                // starting a new constraint
                stmtTo = file1Map.get(line);
                stmt = line;
                constraint = "(echo \"" + stmt + "\")\n";
                if (stmtTo == null) {
                    LOGGER.warn("Stmt not present {}", line);
                    // System.exit(2);
                    var = "";
                    formula = "";
                }
            } else if (line.contains("->")) {
                // variable
                if (!formula.equals("") || !var.equals("")) {
                    // //not the first time around
                    // //then create an smt2 formula for
                    // //the just ended constraint
                    // String formula1 = stmtTo.get(var);
                    // if(formula1 == null){
                    // System.out.println("No formula for var " + var);
                    // System.exit(2);
                    // }
                    // String forward = Smt2Format.implies(var, formula1, formula);
                    // String backward = Smt2Format.implies(var, formula, formula1);
                    // //System.out.println(forward + backward);
                    // constraint += "(echo \"" + var + "\") \n" + forward + backward;
                    if (stmtTo != null) {
                        constraint += writeConstraint(stmtTo, constraint, var, formula);
                    }
                }
                String[] data = line.split("->");
                var = data[0];
                formula = data[1];

            } else {
                formula += line.trim();
            }
        }
        // write the formula for the last variable in the last statement
        // first create the constraint
        // for the last read variable
        if (stmtTo != null) {
            constraint += writeConstraint(stmtTo, constraint, var, formula);
        }
        writer.write(constraint);
        scanner.close();
        writer.flush();
        writer.close();
    }

    private static String writeConstraint(Map<String, String> stmtTo, String constraint, String var,
            String formula) throws IOException {
        LOGGER.debug("stmtTo {} {} {}", constraint, var, formula);
        String formula1 = stmtTo.get(var);
        if (formula1 == null) {
            LOGGER.warn("No formula for var {} in stmt {}", var, stmtTo);
            // set it to unsat
            // need to make var to be without f
            String varF = var;
            if (var.endsWith("f")) {
                // false branch need to remove f
                varF = var.substring(0, var.length() - 1);
                LOGGER.debug("var {}", var);
            }
            formula1 = "(and (> " + varF + " 0) (< " + varF + " 0))";
            // System.exit(2);
        }
        String forward = Smt2Format.implies(var, formula1, formula);
        String backward = Smt2Format.implies(var, formula, formula1);
        LOGGER.debug("forward: {} -> backward {}", forward, backward);
        // constraint += "(echo \"" + var + "\") \n" + forward + backward;
        return "(echo \"" + var + "\")\n" + forward + backward;
    }

    private static String implies(String var, String from, String to) {
        if (var.endsWith("f")) {
            // false branch need to remove f
            var = var.substring(0, var.length() - 1);
            LOGGER.debug("var {}", var);
        }
        String implies = "(push)\n" + "(assert (forall ((" + var + " Int))\n" + "(=> " + from + " "
                + to + ")))\n" + "(check-sat)\n(pop)\n";

        return implies;
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
