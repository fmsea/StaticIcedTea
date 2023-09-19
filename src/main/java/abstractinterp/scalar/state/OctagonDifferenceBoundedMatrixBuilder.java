package abstractinterp.scalar.state;

import java.util.Arrays;
import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.IntegerType;
import soot.Local;
import soot.Value;
import soot.grimp.Grimp;
import soot.jimple.BinopExpr;
import soot.jimple.IntConstant;
import soot.jimple.internal.JNegExpr;

import common.Locals;
import abstractinterp.scalar.state.util.GraphProjection;
import solver.SolverWrapper;
import util.Configuration;
import util.Pair;

public class OctagonDifferenceBoundedMatrixBuilder {

    private OctagonDifferenceBoundedMatrix matrix;

    public OctagonDifferenceBoundedMatrixBuilder(int N, boolean top) {
        this.matrix = new OctagonDifferenceBoundedMatrix(N, top);
    }

    /** Add a constraint to the underlying matrix.
     *
     * @param: {@link Integer} i, Source variable of difference constraint
     * @param: {@link Integer} j, Target variable of difference constraint
     * @param: {@link Constraint} constraint, difference constraint bound
     * @return self.
     */
    public OctagonDifferenceBoundedMatrixBuilder setConstraint(int i, int j, Constraint c) {
        this.matrix.setConstraint(i, j, c);
        return this;
    }

    public OctagonDifferenceBoundedMatrixBuilder peek() {
        System.err.println(this.matrix.toString());
        return this;
    }

    public OctagonDifferenceBoundedMatrixBuilder close() {
        this.matrix.canonicalize();
        return this;
    }

    public OctagonDifferenceBoundedMatrix build() {
        return this.matrix;
    }
}
