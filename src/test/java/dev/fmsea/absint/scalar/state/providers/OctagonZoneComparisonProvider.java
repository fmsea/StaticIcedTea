package dev.fmsea.absint.scalar.state.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;

public class OctagonZoneComparisonProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.arguments(
                JimpleProvider.constantJimpleMethod("z3_constant_test"),
                "octagons.zones.constantValuePropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("z3ConstantMath"),
                "octagons.zones.constantMathPropagation.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("z3_simpleIf"),
                "octagons.zones.branching.smt.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("z3_simple_loop"),
                "octagons.zones.looping.smt.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "octagons.zones.example5.smt.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "octagons.zones.nonsenseExample.smt.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "octagons.zones.neqLoop.smt.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "octagons.zones.getArrowSubset.smt.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "octagons.zones.intervalComparison.smt.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "octagons.zones.fibonacci.smt.out"));
    }
}
