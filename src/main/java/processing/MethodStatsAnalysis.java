package processing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import soot.Body;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.LoopNestTree;
import soot.util.Chain;
import abstractinterp.scalar.ForwardBranchedFlowNumerical;
import abstractinterp.scalar.state.BinaryOperatorType;
import abstractinterp.scalar.state.PredicateType;

/**
 * Class which performs statement counting and various other statistics about programs.
 *
 * This class currently makes assumptions about the predicate domain.  Constant
 * values for example are counted in the range of dom5_4.  The range should be
 * parameterized later so that we can perform this with respect to other
 * domains.
 */
public class MethodStatsAnalysis {

    private enum Statistics {
        ASSIGNMENT_STATEMENTS,
        BITWISE_OPERATIONS,
        BRANCHES,
        LOOPS,
        CONSTANTS,
        EQUAL_COMPARISONS_TO_PARTITION_MEMBER,
        EQUAL_COMPARISONS_TO_CONSTANT,
        IDENTITY_STATEMENTS,
        INSTRUCTIONS,
        INVOCATIONS,
        NON_INTEGER_COMPARISON,
        NON_LINEAR_OPERATIONS,
        NOT_EQUAL_COMPARISONS_TO_PARTITION_MEMBER,
        NOT_EQUAL_COMPARISONS_TO_CONSTANT,
        RELATIONAL_ASSIGNMENT,
        RELATIONAL_COMPARISON,
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodStatsAnalysis.class);
    private final Body body;
    private final Chain<Unit> methodUnits;
    private Map<Statistics, Integer> counts;

    public MethodStatsAnalysis(Body methodBody) {
        this.body = methodBody;
        this.methodUnits = this.body.getUnits();
        this.counts = new HashMap<>();
        for (Statistics s : Statistics.values()) {
            this.counts.put(s, 0);
        }
    }

    public void runAnalysis() {
        LOGGER.info("Quantifying stats about method");
        LOGGER.debug("Soot method under analysis:\n{}", this.body);
        Consumer<Statistics> update = (key) -> {
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        };

        LoopNestTree loopTree = new LoopNestTree(this.body);
        loopTree.stream().forEach(l -> update.accept(Statistics.LOOPS));

        for (Unit unit : this.methodUnits) {
            LOGGER.trace("[unit: {}]", unit);
            if (unit instanceof IdentityStmt) {
                update.accept(Statistics.IDENTITY_STATEMENTS);
            } else if (unit instanceof InvokeStmt) {
                update.accept(Statistics.INVOCATIONS);
            } else if (unit instanceof AssignStmt) {
                update.accept(Statistics.ASSIGNMENT_STATEMENTS);
                AssignStmt a = (AssignStmt)unit;
                Value lhs = a.getLeftOp();
                Value rhs = a.getRightOp();
                if (lhs instanceof Local && ForwardBranchedFlowNumerical.isIntType(lhs)) {
                    if (rhs instanceof BinopExpr) {
                        BinopExpr biexpr = (BinopExpr) rhs;
                        BinaryOperatorType op = BinaryOperatorType.fromJimple(biexpr);
                        switch (op) {
                        case MULTIPLICATION:
                        case DIVISION:
                        case MODULUS:
                            update.accept(Statistics.NON_LINEAR_OPERATIONS);
                        case BAND:
                        case BOR:
                        case BSHL:
                        case BSHR:
                        case BUSHR:
                        case XOR:
                            update.accept(Statistics.BITWISE_OPERATIONS);
                        default:
                        }
                        Value left = biexpr.getOp1();
                        Value right = biexpr.getOp2();
                        if (left instanceof IntConstant) {
                            IntConstant c = (IntConstant) left;
                            if (c.value < -1 || c.value > 1) {
                                update.accept(Statistics.CONSTANTS);
                                LOGGER.trace("Found constant [c={}]", c.value);
                            }
                        }
                        if (right instanceof IntConstant) {
                            IntConstant c = (IntConstant) right;
                            if (c.value < -1 || c.value > 1) {
                                update.accept(Statistics.CONSTANTS);
                                LOGGER.trace("Found constant [c={}]", c.value);
                            }
                        }
                        if ((op == BinaryOperatorType.ADDITION &&
                             left instanceof Local &&
                             right instanceof IntConstant) ||
                            (op == BinaryOperatorType.ADDITION &&
                             left instanceof IntConstant &&
                             right instanceof Local) ||
                            (op == BinaryOperatorType.SUBTRACTION &&
                             left instanceof Local &&
                             right instanceof IntConstant)) {
                            // relational assignment
                            update.accept(Statistics.RELATIONAL_ASSIGNMENT);
                        }
                    } else if (rhs instanceof IntConstant) {
                        IntConstant c = (IntConstant) rhs;
                        if (c.value < -1 || c.value > 1) {
                            update.accept(Statistics.CONSTANTS);
                            LOGGER.trace("Found constant [c={}]", c.value);
                        }
                    }
                }
            } else if (unit instanceof IfStmt) {
                update.accept(Statistics.BRANCHES);
                IfStmt ifStmt = (IfStmt) unit;
                ConditionExpr condExpr = (ConditionExpr) ifStmt.getCondition();
                PredicateType type = PredicateType.fromJimple(condExpr);
                Value left = condExpr.getOp1();
                Value right = condExpr.getOp2();
                if (left instanceof IntConstant) {
                    LOGGER.info("left is an int");
                    IntConstant c = (IntConstant) left;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.CONSTANTS);
                        LOGGER.trace("Found constant [c={}]", c.value);
                    }
                }
                if (right instanceof IntConstant) {
                    LOGGER.info("right is an int");
                    IntConstant c = (IntConstant) right;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.CONSTANTS);
                        LOGGER.trace("Found constant [c={}]", c.value);
                    }
                }
                if (left instanceof Local && right instanceof Local) {
                    update.accept(Statistics.RELATIONAL_COMPARISON);
                } else if (type == PredicateType.Eq &&
                    right instanceof IntConstant) {
                    IntConstant c = (IntConstant) right;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.EQUAL_COMPARISONS_TO_CONSTANT);
                    } else {
                        update.accept(Statistics.EQUAL_COMPARISONS_TO_PARTITION_MEMBER);
                    }
                } else if (type == PredicateType.Eq && left instanceof IntConstant) {
                    IntConstant c = (IntConstant) left;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.EQUAL_COMPARISONS_TO_CONSTANT);
                    } else {
                        update.accept(Statistics.EQUAL_COMPARISONS_TO_PARTITION_MEMBER);
                    }
                } else if (type == PredicateType.Ne && right instanceof IntConstant) {
                    IntConstant c = (IntConstant) right;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.NOT_EQUAL_COMPARISONS_TO_CONSTANT);
                    } else {
                        update.accept(Statistics.NOT_EQUAL_COMPARISONS_TO_PARTITION_MEMBER);
                    }
                } else if (type == PredicateType.Ne && left instanceof IntConstant) {
                    IntConstant c = (IntConstant) left;
                    if (c.value < -1 || c.value > 1) {
                        update.accept(Statistics.NOT_EQUAL_COMPARISONS_TO_CONSTANT);
                    } else {
                        update.accept(Statistics.NOT_EQUAL_COMPARISONS_TO_PARTITION_MEMBER);
                    }
                }
                if (!(left instanceof IntConstant || right instanceof IntConstant)) {
                    update.accept(Statistics.NON_INTEGER_COMPARISON);
                }

                LOGGER.trace("[unit={}, left={}, left-type={} right={}, right-type={}, type={}]",
                             unit, left, left.getType(), right, right.getType(), type);
            }

            update.accept(Statistics.INSTRUCTIONS);
        }
    }

    public String report() {
        JSONObject o = new JSONObject(this.counts);
        return o.toString();
    }
}
