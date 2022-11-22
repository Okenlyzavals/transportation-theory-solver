package by.bntu.baranouski.controller;

import by.bntu.baranouski.model.TransportationState;
import by.bntu.baranouski.model.dto.SolutionDto;
import by.bntu.baranouski.service.InitialPhaseSolverService;
import by.bntu.baranouski.solver.SecondPhaseSolver;
import lombok.SneakyThrows;

public class TransportationController {

    public enum InitialSolveMethod{
        VOGEL,NORTH_WEST,MINIMAL
    }

    private final InitialPhaseSolverService initialService = new InitialPhaseSolverService();
    private final SecondPhaseSolver secondPhaseSolver = new SecondPhaseSolver();

    @SneakyThrows
    public SolutionDto solve(TransportationState initialState, InitialSolveMethod solveMethod){
        var initialPhase = switch (solveMethod){
            case VOGEL -> initialService.solveVogel(initialState);
            case NORTH_WEST -> initialService.solveNorthWest(initialState);
            case MINIMAL -> initialService.solveMinimal(initialState);
        };
        return new SolutionDto(initialPhase, secondPhaseSolver.solve(initialPhase.get(initialPhase.size()-1)));
    }

}
