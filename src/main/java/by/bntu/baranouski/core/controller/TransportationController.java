package by.bntu.baranouski.core.controller;

import by.bntu.baranouski.core.service.InitialPhaseSolverService;
import by.bntu.baranouski.core.model.TransportationState;
import by.bntu.baranouski.core.model.dto.SolutionDto;
import by.bntu.baranouski.core.solver.SecondPhaseSolver;
import lombok.SneakyThrows;

import java.util.List;

public class TransportationController {

    public enum InitialSolveMethod{
        VOGEL,NORTH_WEST,MINIMAL
    }

    private final InitialPhaseSolverService initialService = new InitialPhaseSolverService();
    private final SecondPhaseSolver secondPhaseSolver = new SecondPhaseSolver();

    @SneakyThrows
    public SolutionDto solve(TransportationState initialState, InitialSolveMethod solveMethod){
        List<TransportationState> initialPhase = null;
        switch (solveMethod){
            case VOGEL ->  initialPhase = initialService.solveVogel(initialState);
            case NORTH_WEST ->  initialPhase = initialService.solveNorthWest(initialState);
            case MINIMAL ->  initialPhase = initialService.solveMinimal(initialState);
        }
        return new SolutionDto(initialPhase, secondPhaseSolver.solve(initialPhase.get(initialPhase.size()-1)));
    }

}
