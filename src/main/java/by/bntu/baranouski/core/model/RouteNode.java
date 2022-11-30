package by.bntu.baranouski.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RouteNode{

    private Producer producer;
    private Consumer consumer;

    public RouteNode(Producer producer, Consumer consumer) {
        this.producer = producer;
        this.consumer = consumer;
    }

    private BigDecimal tariff;
    private BigDecimal taken;
    private BigDecimal alpha;
    private OptimizationValue opt;

    public enum OptimizationValue{
        PLUS, MINUS
    }


}
