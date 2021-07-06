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

}
