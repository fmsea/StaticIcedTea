package dev.fmsea.absint.scalar.state.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ZonesNumericalProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.arguments(
                JimpleProvider.constantJimpleMethod("constant_test", true),
                "zones.constantValuePropagation.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("moreConstantMath"),
                "zones.constantMathPropagation.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("anotherSimpleIf"),
                "zones.branching.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("anotherSimpleLoop"),
                "zones.looping.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "zones.example5.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "zones.nonsenseExample.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "zones.neqLoop.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "zones.getArrowSubset.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "zones.intervalComparison.out"),
            Arguments.arguments(
                JimpleProvider.transverseZero(),
                "zones.transverseZero.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "zones.fibonacci.out"),
            Arguments.arguments(
                JimpleProvider.tribonacci(),
                "zones.tribonacci.out"),
            Arguments.arguments(
                JimpleProvider.factorial(),
                "zones.factorial.out"),
            Arguments.arguments(
                JimpleProvider.decode(),
                "zones.decode.out"),
            Arguments.arguments(
                JimpleProvider.swap(),
                "zones.swap.out"));
    }
}
