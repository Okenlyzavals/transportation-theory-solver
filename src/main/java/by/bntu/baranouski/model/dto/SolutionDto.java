package by.bntu.baranouski.model.dto;

import by.bntu.baranouski.model.TransportationState;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SolutionDto{
    List<TransportationState> initialPhaseSolution;
    List<TransportationState> secondPhaseSolution;
}
