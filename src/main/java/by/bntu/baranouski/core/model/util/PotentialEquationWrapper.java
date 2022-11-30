package by.bntu.baranouski.core.model.util;

import by.bntu.baranouski.core.model.Potential;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PotentialEquationWrapper {
    Potential consumerPotential;
    Potential producerPotential;
    BigDecimal tariff;

    public Integer getNumberOfKnownVariables(){
        int res = 0;
        res += consumerPotential.getValue() != null ? 1 : 0;
        res += producerPotential.getValue() != null ? 1 : 0;
        return res;
    }
}
