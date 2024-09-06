package dev.fmsea.driver.commands.validation;

import dev.fmsea.driver.util.OrdererType;
import picocli.CommandLine.ITypeConverter;

public class OrdererTypeConverter implements ITypeConverter<OrdererType> {
    public OrdererType convert(String value) throws Exception {
        if ("pseudo".equals(value.toLowerCase())) {
            return OrdererType.PseudoTopological;
        } else if ("conditional".equals(value.toLowerCase())) {
            return OrdererType.ConditionalTopological;
        } else if ("slow".equals(value.toLowerCase())) {
            return OrdererType.SlowPseudoTopological;
        } else {
            return OrdererType.PseudoTopological;
        }
    }
}
