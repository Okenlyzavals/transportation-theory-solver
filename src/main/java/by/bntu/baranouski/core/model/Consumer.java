package by.bntu.baranouski.core.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Consumer {
    private Integer index;
    private BigDecimal initialDemand;
    private BigDecimal currentDemand;
    private Potential potential = new Potential();

    public Consumer(Integer index) {
        this.index = index;
    }

    public Consumer(Consumer cloneFrom) {
        index = cloneFrom.index;
        initialDemand = cloneFrom.initialDemand;
        currentDemand = cloneFrom.currentDemand;
        potential = new Potential(cloneFrom.potential.value);
    }
}
