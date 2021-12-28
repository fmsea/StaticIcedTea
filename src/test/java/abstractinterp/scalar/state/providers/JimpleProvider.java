package abstractinterp.scalar.state.providers;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.IntConstant;
import soot.util.Chain;

import java.util.Collections;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class JimpleProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage targetType) {
        return targetType.isOfType(Body.class);
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
        return Collections.singleton(Arbitraries.of(constantJimpleMethod("constant"),
                                                    binaryArithmaticMethod("arithmetic"),
                                                    simpleIfStatement("simpleIf"),
                                                    simpleLoopStatement("simpleLoop")));
    }

    public static Body constantJimpleMethod(String name) {
        return constantJimpleMethod(name, false);
    }

    public static Body constantJimpleMethod(String name, boolean addParameter) {
        SootClass testClass = new SootClass(name + "SootClass", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        List<Type> parameters = Arrays.asList(new Type[] {IntType.v()});
        SootMethod method = new SootMethod(name, addParameter ? parameters : null, IntType.v());
        Scene.v().addClass(testClass);
        testClass.addMethod(method);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local parameter = Jimple.v().newLocal("l0", IntType.v());
        Local constant = Jimple.v().newLocal("l1", IntType.v());
        if (addParameter) {
            units.add(Jimple.v().newIdentityStmt(parameter,
                                                 Jimple.v().newParameterRef(IntType.v(), 0)));
            body.getLocals().add(parameter);
        }
        units.add(Jimple.v().newAssignStmt(constant, IntConstant.v(6)));
        body.getLocals().add(constant);
        units.add(Jimple.v().newReturnStmt(constant));
        return body;
    }

    public static Body binaryArithmaticMethod(String name) {
        SootClass testClass = new SootClass(name + "SootClass", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod(name, null, VoidType.v());
        Scene.v().addClass(testClass);
        testClass.addMethod(method);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        Local l3 = Jimple.v().newLocal("l3", IntType.v());
        body.getLocals().add(l0);
        body.getLocals().add(l1);
        body.getLocals().add(l2);
        body.getLocals().add(l3);
        units.add(Jimple.v().newAssignStmt(l0, IntConstant.v(3)));
        units.add(Jimple.v().newAssignStmt(l1, Jimple.v().newAddExpr(l0, IntConstant.v(6))));
        units.add(Jimple.v().newAssignStmt(l2, Jimple.v().newSubExpr(l1, l0)));
        units.add(Jimple.v().newAssignStmt(l3, Jimple.v().newMulExpr(l2, IntConstant.v(-1))));
        units.add(Jimple.v().newAssignStmt(l0, Jimple.v().newDivExpr(l3, l2)));
        units.add(Jimple.v().newReturnVoidStmt());
        return body;
    }

    public static Body simpleIfStatement(String name) {
        SootClass testClass = new SootClass(name + "SootClass", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod(name, null, VoidType.v());
        Scene.v().addClass(testClass);
        testClass.addMethod(method);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        Local l3 = Jimple.v().newLocal("l3", IntType.v());
        body.getLocals().add(l0);
        body.getLocals().add(l1);
        body.getLocals().add(l2);
        body.getLocals().add(l3);
        Unit trueTarget = Jimple.v().newAssignStmt(l3, IntConstant.v(6));
        Unit falseTarget = Jimple.v().newAssignStmt(l3, Jimple.v().newDivExpr(l1, l2));
        units.add(Jimple.v().newAssignStmt(l0, IntConstant.v(4)));
        units.add(Jimple.v().newAssignStmt(l1, IntConstant.v(0)));
        units.add(Jimple.v().newAssignStmt(l2, IntConstant.v(0)));
        units.add(Jimple.v().newIfStmt(Jimple.v().newGeExpr(l0, IntConstant.v(3)),
                                       trueTarget));
        units.add(falseTarget);
        units.add(trueTarget);
        units.add(Jimple.v().newReturnVoidStmt());
        return body;
    }

    public static Body simpleLoopStatement(String name) {
        SootClass testClass = new SootClass(name + "SootClass", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod(name, null, VoidType.v());
        Scene.v().addClass(testClass);
        testClass.addMethod(method);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local l0 = Jimple.v().newLocal("l0", IntType.v());
        Local l1 = Jimple.v().newLocal("l1", IntType.v());
        Local l2 = Jimple.v().newLocal("l2", IntType.v());
        Local l3 = Jimple.v().newLocal("l3", IntType.v());
        body.getLocals().add(l0);
        body.getLocals().add(l1);
        body.getLocals().add(l2);
        body.getLocals().add(l3);
        units.add(Jimple.v().newAssignStmt(l0, IntConstant.v(5)));
        units.add(Jimple.v().newAssignStmt(l1, IntConstant.v(0)));
        Unit trueTarget = Jimple.v().newAssignStmt(l3, Jimple.v().newAddExpr(l0, l1));
        Unit loopCond = Jimple.v().newIfStmt(Jimple.v().newGeExpr(l1, IntConstant.v(5)),
                                             trueTarget);
        units.add(loopCond);
        units.add(Jimple.v().newAssignStmt(l1, Jimple.v().newAddExpr(l1, IntConstant.v(1))));
        units.add(Jimple.v().newGotoStmt(loopCond));
        units.add(trueTarget);
        units.add(Jimple.v().newReturnVoidStmt());
        return body;
    }

    public static Body example5() {
        SootClass testClass = new SootClass("test.Example1M", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("example_5",
                                           Arrays.asList(new Type[] {IntType.v()}),
                                           IntType.v(),
                                           Modifier.PUBLIC | Modifier.STATIC);
        Scene.v().addClass(testClass);
        testClass.addMethod(method);
        JimpleBody body = Jimple.v().newBody(method);
        Chain<Unit> units = body.getUnits();
        Local i0 = Jimple.v().newLocal("$i0", IntType.v());
        Local i1 = Jimple.v().newLocal("i1", IntType.v());
        Local i4 = Jimple.v().newLocal("i4", IntType.v());
        Local i5 = Jimple.v().newLocal("i5", IntType.v());
        Local b2 = Jimple.v().newLocal("b2", ByteType.v());
        Local b3 = Jimple.v().newLocal("b3", ByteType.v());
        body.getLocals().add(i0);
        body.getLocals().add(i1);
        body.getLocals().add(i4);
        body.getLocals().add(i5);
        body.getLocals().add(b2);
        body.getLocals().add(b3);
        units.add(Jimple.v().newIdentityStmt(i1,
                                             Jimple.v().newParameterRef(IntType.v(), 0)));
        units.add(Jimple.v().newAssignStmt(b2, IntConstant.v(1)));
        units.add(Jimple.v().newAssignStmt(b3, IntConstant.v(3)));
        Unit label1 = Jimple.v().newAssignStmt(i4, Jimple.v().newAddExpr(b3, b2));
        Unit label2 = Jimple.v().newAssignStmt(i0, Jimple.v().newMulExpr(b3, i4));
        units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(b3, IntConstant.v(0)),
                                       label1));
        units.add(Jimple.v().newAssignStmt(i4, Jimple.v().newSubExpr(b3, b2)));
        units.add(Jimple.v().newGotoStmt(label2));
        units.add(label1);
        units.add(label2);
        units.add(Jimple.v().newAssignStmt(i5, Jimple.v().newSubExpr(i0, IntConstant.v(18))));
        units.add(Jimple.v().newReturnStmt(i5));
        return body;
    }

    public static Body nonsense() {
        SootClass testClass = new SootClass("test.Nonsense", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("decode", null, VoidType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local[] locals = new Local[] {
            Jimple.v().newLocal("l0", IntType.v()),
            Jimple.v().newLocal("l1", IntType.v()),
            Jimple.v().newLocal("l2", IntType.v()),
            Jimple.v().newLocal("l3", IntType.v()),
            Jimple.v().newLocal("l4", IntType.v()),
        };

        for (int i = 0; i < locals.length; i++) {
            body.getLocals().add(locals[i]);
        }

        units.add(Jimple.v().newAssignStmt(locals[0], IntConstant.v(3)));
        units.add(Jimple.v().newAssignStmt(locals[1], IntConstant.v(4)));
        units.add(Jimple.v().newAssignStmt(locals[2], IntConstant.v(1)));
        Unit exit = Jimple.v().newReturnVoidStmt();
        Unit loop = Jimple.v().newIfStmt(Jimple.v().newGtExpr(locals[3], IntConstant.v(20)), exit);
        Unit label01 = Jimple.v().newAssignStmt(locals[3], Jimple.v().newAddExpr(locals[3], IntConstant.v(1)));
        Unit label01f = Jimple.v().newAssignStmt(locals[3], Jimple.v().newSubExpr(locals[0], IntConstant.v(2)));
        Unit branch = Jimple.v().newIfStmt(Jimple.v().newEqExpr(locals[1], IntConstant.v(0)), label01);
        units.add(loop);
        units.add(Jimple.v().newAssignStmt(locals[4], Jimple.v().newRemExpr(locals[3], IntConstant.v(2))));
        units.add(branch);
        units.add(label01f);
        units.add(Jimple.v().newGotoStmt(loop));
        units.add(label01);
        units.add(Jimple.v().newGotoStmt(loop));
        units.add(exit);

        return body;
    }

    public static Body neqLoop() {
        SootClass testClass = new SootClass("test.neqBranch", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("neq", null, VoidType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local[] locals = new Local[] {
            Jimple.v().newLocal("l0", IntType.v()),
            Jimple.v().newLocal("l1", IntType.v()),
            Jimple.v().newLocal("l2", IntType.v()),
        };

        for (int i = 0; i < locals.length; i++) {
            body.getLocals().add(locals[i]);
        }

        units.add(Jimple.v().newAssignStmt(locals[1], IntConstant.v(4)));
        Unit exit = Jimple.v().newReturnVoidStmt();
        Unit loop = Jimple.v().newIfStmt(Jimple.v().newNeExpr(locals[0], IntConstant.v(0)), exit);
        units.add(loop);
        units.add(Jimple.v().newAssignStmt(locals[2], Jimple.v().newAddExpr(locals[1], IntConstant.v(1))));
        units.add(Jimple.v().newGotoStmt(loop));
        units.add(exit);

        return body;
    }

    public static Body ballonGetArrow() {
        SootClass testClass = new SootClass("test.ballonFactory", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("getArrow", null, VoidType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local[] locals = new Local[] {
            Jimple.v().newLocal("b0", IntType.v()),
            Jimple.v().newLocal("b1", IntType.v()),
            Jimple.v().newLocal("b2", IntType.v()),
            Jimple.v().newLocal("$b25", IntType.v()),
            Jimple.v().newLocal("$i26", IntType.v()),
        };

        for (int i = 0; i < locals.length; i++) {
            body.getLocals().add(locals[i]);
        }

        units.add(Jimple.v().newAssignStmt(locals[0], IntConstant.v(0)));
        units.add(Jimple.v().newAssignStmt(locals[1], IntConstant.v(50)));
        units.add(Jimple.v().newAssignStmt(locals[2], IntConstant.v(60)));
        units.add(Jimple.v().newAssignStmt(locals[3], Jimple.v().newNegExpr(locals[2])));
        units.add(Jimple.v().newAssignStmt(locals[4], Jimple.v().newDivExpr(locals[3], IntConstant.v(2))));

        units.add(Jimple.v().newReturnVoidStmt());

        return body;
    }

    public static Body intervalComparison() {
        SootClass testClass = new SootClass("test.ints", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("compareIntervals", null, IntType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local u = Jimple.v().newLocal("u0", IntType.v());
        Local w = Jimple.v().newLocal("w0", IntType.v());
        Local x = Jimple.v().newLocal("x0", IntType.v());

        body.getLocals().add(u);
        body.getLocals().add(w);
        body.getLocals().add(x);

        Unit exit = Jimple.v().newReturnStmt(w);
        Unit wAssign = Jimple.v().newAssignStmt(w, Jimple.v().newAddExpr(x, u));
        Unit innerIf = Jimple.v().newIfStmt(Jimple.v().newGeExpr(x, IntConstant.v(0)), wAssign);
        units.add(Jimple.v().newAssignStmt(u, x));
        units.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(x, IntConstant.v(20)), innerIf));
        units.add(Jimple.v().newGotoStmt(exit));
        units.add(innerIf);
        units.add(Jimple.v().newGotoStmt(exit));
        units.add(wAssign);
        units.add(exit);

        return body;
    }

    public static Body transverseZero() {
        SootClass testClass = new SootClass("test.transverse", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("zero", null, IntType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local r = Jimple.v().newLocal("r0", IntType.v());
        Local u = Jimple.v().newLocal("u0", IntType.v());
        Local x = Jimple.v().newLocal("x0", IntType.v());
        Local y = Jimple.v().newLocal("y0", IntType.v());

        Stream.of(r, u, x, y).forEach(l -> body.getLocals().add(l));

        Unit exit = Jimple.v().newReturnStmt(r);
        Unit rAssign = Jimple.v().newAssignStmt(r, IntConstant.v(1));
        units.add(Jimple.v().newAssignStmt(x, IntConstant.v(60)));
        units.add(Jimple.v().newAssignStmt(y, Jimple.v().newNegExpr(x)));
        units.add(Jimple.v().newAssignStmt(u, Jimple.v().newSubExpr(x, y)));
        units.add(Jimple.v().newIfStmt(Jimple.v().newGtExpr(u, IntConstant.v(120)), rAssign));
        units.add(Jimple.v().newAssignStmt(r, Jimple.v().newNegExpr(IntConstant.v(1))));
        units.add(Jimple.v().newGotoStmt(exit));
        units.add(rAssign);
        units.add(exit);

        return body;
    }

    public static Body fibonacci() {
        SootClass testClass = new SootClass("test.Fibonacci", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("fibonacci",
                                           Arrays.asList(new Type[] {IntType.v()}),
                                           IntType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local i0 = Jimple.v().newLocal("i0", IntType.v());
        Local i1 = Jimple.v().newLocal("i1", IntType.v());
        Local i2 = Jimple.v().newLocal("i2", IntType.v());
        Local i3 = Jimple.v().newLocal("i3", IntType.v());
        Local i4 = Jimple.v().newLocal("i4", IntType.v());

        Stream.of(i0, i1, i2, i3, i4) .forEach(l -> body.getLocals().add(l));

        Unit exit = Jimple.v().newReturnStmt(i3);
        Unit loopGuard = Jimple.v().newIfStmt(Jimple.v().newGeExpr(i4, i0), exit);
        units.add(Jimple.v().newIdentityStmt(i0,
                                             Jimple.v().newParameterRef(IntType.v(), 0)));
        units.add(Jimple.v().newAssignStmt(i2, IntConstant.v(0)));
        units.add(Jimple.v().newAssignStmt(i3, IntConstant.v(1)));
        units.add(Jimple.v().newAssignStmt(i4, IntConstant.v(2)));
        units.add(loopGuard);
        units.add(Jimple.v().newAssignStmt(i1, Jimple.v().newAddExpr(i2, i3)));
        units.add(Jimple.v().newAssignStmt(i2, i3));
        units.add(Jimple.v().newAssignStmt(i3, i1));
        units.add(Jimple.v().newAssignStmt(i4, Jimple.v().newAddExpr(i4, IntConstant.v(1))));
        units.add(Jimple.v().newGotoStmt(loopGuard));
        units.add(exit);

        return body;
    }

    public static Body tribonacci() {
        SootClass testClass = new SootClass("test.Tribonacci", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("tribonacci",
                                           Arrays.asList(new Type[] {IntType.v()}),
                                           IntType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = Jimple.v().newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();

        Local i0 = Jimple.v().newLocal("i0", IntType.v());
        Local i2 = Jimple.v().newLocal("i2", IntType.v());
        Local i3 = Jimple.v().newLocal("i3", IntType.v());
        Local i4 = Jimple.v().newLocal("i4", IntType.v());
        Local i5 = Jimple.v().newLocal("i5", IntType.v());
        Local i6 = Jimple.v().newLocal("i6", IntType.v());
        Local ti1 = Jimple.v().newLocal("$i1", IntType.v());

        Stream.of(i0, i2, i3, i4, i5, i6, ti1)
            .forEach(l -> body.getLocals().add(l));

        Unit exit = Jimple.v().newReturnStmt(i5);
        Unit loopGuard = Jimple.v().newIfStmt(Jimple.v().newGeExpr(i6, i0), exit);
        units.add(Jimple.v().newIdentityStmt(i0,
                                             Jimple.v().newParameterRef(IntType.v(), 0)));
        units.add(Jimple.v().newAssignStmt(i3, IntConstant.v(0)));
        units.add(Jimple.v().newAssignStmt(i4, IntConstant.v(1)));
        units.add(Jimple.v().newAssignStmt(i5, IntConstant.v(1)));
        units.add(Jimple.v().newAssignStmt(i6, IntConstant.v(3)));
        units.add(loopGuard);
        units.add(Jimple.v().newAssignStmt(ti1, Jimple.v().newAddExpr(i5, i4)));
        units.add(Jimple.v().newAssignStmt(i2, Jimple.v().newAddExpr(ti1, i3)));
        units.add(Jimple.v().newAssignStmt(i3, i4));
        units.add(Jimple.v().newAssignStmt(i4, i5));
        units.add(Jimple.v().newAssignStmt(i5, i2));
        units.add(Jimple.v().newAssignStmt(i6, Jimple.v().newAddExpr(i6, IntConstant.v(1))));
        units.add(Jimple.v().newGotoStmt(loopGuard));
        units.add(exit);

        return body;
    }

    public static Body factorial() {
        Jimple jimple = Jimple.v();
        SootClass testClass = new SootClass("test.Factorial", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("factorial",
                                           Arrays.asList(new Type[] {IntType.v()}),
                                           IntType.v());
        testClass.addMethod(method);
        Scene.v().addClass(testClass);
        JimpleBody body = jimple.newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local n = jimple.newLocal("$i0", IntType.v());
        Local f = jimple.newLocal("i1", IntType.v());
        Stream.of(n, f).forEach(l -> body.getLocals().add(l));
        Unit exit = jimple.newReturnStmt(f);
        Unit loopGuard = jimple.newIfStmt(jimple.newLeExpr(n, IntConstant.v(0)), exit);
        units.add(jimple.newIdentityStmt(n, jimple.newParameterRef(IntType.v(), 0)));
        units.add(jimple.newAssignStmt(n, IntConstant.v(1)));
        units.add(loopGuard);
        units.add(jimple.newAssignStmt(f, jimple.newMulExpr(f, n)));
        units.add(jimple.newAssignStmt(n, jimple.newSubExpr(n, IntConstant.v(1))));
        units.add(jimple.newGotoStmt(loopGuard));
        units.add(exit);

        return body;
    }

    public static Body decode() {
        Jimple jimple = Jimple.v();
        SootClass testClass = new SootClass("test.Example", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("decode",
                                           Arrays.asList(new Type[] {IntType.v()}),
                                           IntType.v());
        testClass.addMethod(method);
        JimpleBody body = jimple.newBody(method);
        method.setActiveBody(body);
        Chain<Unit> units = body.getUnits();
        Local[] xs = new Local[] {
            jimple.newLocal("$i0", IntType.v()),
            jimple.newLocal("i0", IntType.v()),
            jimple.newLocal("i1", IntType.v()),
            jimple.newLocal("i2", IntType.v()),
            jimple.newLocal("i3", IntType.v()),
            jimple.newLocal("i4", IntType.v()),
            jimple.newLocal("i5", IntType.v()),
            jimple.newLocal("i6", IntType.v()),
        };
        Stream.of(xs).forEach(x -> body.getLocals().add(x));
        Unit exit = jimple.newReturnStmt(xs[6]);
        Unit loopLabel = jimple.newAssignStmt(xs[5], jimple.newSubExpr(xs[6], IntConstant.v(1)));
        units.add(jimple.newIdentityStmt(xs[0], jimple.newParameterRef(IntType.v(), 0)));
        units.add(jimple.newAssignStmt(xs[1], IntConstant.v(0)));
        units.add(jimple.newAssignStmt(xs[2], IntConstant.v(0)));
        units.add(jimple.newAssignStmt(xs[3], IntConstant.v(0)));
        units.add(jimple.newAssignStmt(xs[4], IntConstant.v(0)));
        units.add(loopLabel);
        units.add(jimple.newIfStmt(jimple.newGeExpr(xs[1], xs[5]), exit));
        units.add(jimple.newAssignStmt(xs[3], jimple.newAddExpr(xs[2], IntConstant.v(1))));
        units.add(jimple.newAssignStmt(xs[1], jimple.newAddExpr(xs[1], IntConstant.v(1))));
        units.add(jimple.newGotoStmt(loopLabel));
        units.add(exit);
        return body;
    }

    public static Body swap() {
        Jimple jimple = Jimple.v();
        SootClass testClass = new SootClass("test.Example", Modifier.PUBLIC);
        testClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
        SootMethod method = new SootMethod("swap",
                                           Arrays.asList(new Type[] { IntType.v() }),
                                           IntType.v());
        testClass.addMethod(method);
        JimpleBody body = jimple.newBody(method);
        method.setActiveBody(body);

        Chain<Unit> units = body.getUnits();
        Local[] xs = new Local[] {
            jimple.newLocal("i0", IntType.v()),
            jimple.newLocal("i1", IntType.v()),
            jimple.newLocal("i2", IntType.v()),
        };
        Stream.of(xs).forEach(x -> body.getLocals().add(x));
        Unit exit = jimple.newReturnStmt(xs[0]);
        units.add(jimple.newIdentityStmt(xs[0], jimple.newParameterRef(IntType.v(), 0)));
        units.add(jimple.newIfStmt(jimple.newLeExpr(xs[1], xs[0]), exit));
        units.add(jimple.newAssignStmt(xs[2], xs[1]));
        units.add(jimple.newAssignStmt(xs[1], xs[0]));
        units.add(jimple.newAssignStmt(xs[0], xs[2]));
        units.add(exit);
        return body;
    }
}
