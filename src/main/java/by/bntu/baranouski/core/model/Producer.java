package by.bntu.baranouski.core.model;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Producer {
    private Integer index;
    private BigDecimal initialSupply;
    private BigDecimal currentSupply;
    private Potential potential = new Potential();

    public Producer(Integer index) {
        this.index = index;
    }

    public Producer(Producer cloneFrom) {
        index = cloneFrom.index;
        initialSupply = cloneFrom.initialSupply;
        currentSupply = cloneFrom.currentSupply;
        potential = new Potential(cloneFrom.potential.value);
    }
}
