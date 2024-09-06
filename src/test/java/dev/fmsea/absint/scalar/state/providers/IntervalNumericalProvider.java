package dev.fmsea.absint.scalar.state.providers;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class IntervalNumericalProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        return Stream.of(
            Arguments.arguments(
                JimpleProvider.constantJimpleMethod("constant_test", true),
                "int.constantValuePropagation.out"),
            Arguments.arguments(
                JimpleProvider.binaryArithmaticMethod("moreConstantMath"),
                "int.constantMathPropagation.out"),
            Arguments.arguments(
                JimpleProvider.simpleIfStatement("anotherSimpleIf"),
                "int.branching.out"),
            Arguments.arguments(
                JimpleProvider.simpleLoopStatement("anotherSimpleLoop"),
                "int.looping.out"),
            Arguments.arguments(
                JimpleProvider.example5(),
                "int.example5.out"),
            Arguments.arguments(
                JimpleProvider.nonsense(),
                "int.nonsenseExample.out"),
            Arguments.arguments(
                JimpleProvider.neqLoop(),
                "int.neqLoop.out"),
            Arguments.arguments(
                JimpleProvider.ballonGetArrow(),
                "int.getArrowSubset.out"),
            Arguments.arguments(
                JimpleProvider.intervalComparison(),
                "int.intervalComparison.out"),
            Arguments.arguments(
                JimpleProvider.transverseZero(),
                "int.transverseZero.out"),
            Arguments.arguments(
                JimpleProvider.fibonacci(),
                "int.fibonacci.out"),
            Arguments.arguments(
                JimpleProvider.tribonacci(),
                "int.tribonacci.out"),
            Arguments.arguments(
                JimpleProvider.factorial(),
                "int.factorial.out"),
            Arguments.arguments(
                JimpleProvider.decode(),
                "int.decode.out"),
            Arguments.arguments(
                JimpleProvider.swap(),
                "int.swap.out"));
    }
}
