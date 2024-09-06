package dev.fmsea.absint.scalar.state.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class OctagonNumericalProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.arguments(
                JimpleProvider.constantJimpleMethod("constant_test", true),
                "octagons.constantValuePropagation.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("moreConstantMath"),
                "octagons.constantMathPropagation.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("anotherSimpleIf"),
                "octagons.branching.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("anotherSimpleLoop"),
                "octagons.looping.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "octagons.example5.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "octagons.nonsenseExample.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "octagons.neqLoop.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "octagons.getArrowSubset.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "octagons.intervalComparison.out"),
            Arguments.arguments(
                JimpleProvider.transverseZero(),
                "octagons.transverseZero.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "octagons.fibonacci.out"),
            Arguments.arguments(
                JimpleProvider.tribonacci(),
                "octagons.tribonacci.out"),
            Arguments.arguments(
                JimpleProvider.factorial(),
                "octagons.factorial.out"),
            Arguments.arguments(
                JimpleProvider.decode(),
                "octagons.decode.out"),
            Arguments.arguments(
                JimpleProvider.swap(),
                "octagons.swap.out"));
    }
}
