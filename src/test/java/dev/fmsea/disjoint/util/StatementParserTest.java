package dev.fmsea.disjoint.util;

import java.util.Optional;
import soot.IntType;
import soot.Local;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.IntConstant;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.fmsea.solver.SolverWrapper;
import dev.fmsea.solver.SolverFactory;

public class StatementParserTest {
    private SolverWrapper solver;

    @BeforeEach
    void setup() {
        this.solver = SolverFactory.getSolver();
    }

    @Test
    void parseSimpleAssignment() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        AssignStmt s = Jimple.v().newAssignStmt(l0, IntConstant.v(42));
        assertEquals("(= l0 42)", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
    }

    @Test
    void parseSimpleAlias() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        AssignStmt s = Jimple.v().newAssignStmt(l0, l1);
        assertEquals("(= l0 l1)", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
    }

    @Test
    void parseSimpleAddition() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newAddExpr(l1, IntConstant.v(42)));
            assertEquals("(= l0 (+ l1 42))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }
        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newAddExpr(IntConstant.v(42), l1));
            assertEquals("(= l0 (+ 42 l1))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }
    }

    @Test
    void parseRelationalAddition() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newAddExpr(l1, l2));
        assertEquals("(= l0 (+ l1 l2))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
    }

    @Test
    void parseSimpleSubtraction() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newSubExpr(l1, IntConstant.v(42)));
            assertEquals("(= l0 (- l1 42))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }

        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newSubExpr(IntConstant.v(42), l1));
            assertEquals("(= l0 (- 42 l1))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }
    }

    @Test
    void parseSimpleMultiplication() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());

        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newMulExpr(l1, IntConstant.v(2)));
            assertEquals("(= l0 (* l1 2))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }

        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newMulExpr(IntConstant.v(2), l1));
            assertEquals("(= l0 (* 2 l1))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }
    }

    @Test
    void parseRelationalMultiplication() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newMulExpr(l1, l2));
        assertEquals("(= l0 (* l1 l2))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
    }

    @Test
    void parseSimpleDivision() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());

        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newDivExpr(l1, IntConstant.v(2)));
            assertEquals("(= l0 (div l1 2))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }

        {
            AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newDivExpr(IntConstant.v(2), l1));
            assertEquals("(= l0 (div 2 l1))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
        }
    }

    @Test
    void parseRelationalDivision() {
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        AssignStmt s = Jimple.v().newAssignStmt(l0, Jimple.v().newDivExpr(l1, l2));
        assertEquals("(= l0 (div l1 l2))", StatementParser.parse(s).map(e -> this.solver.smt2(e)).orElse("false"));
    }
}
