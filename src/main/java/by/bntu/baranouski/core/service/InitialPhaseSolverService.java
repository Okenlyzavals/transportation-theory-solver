package by.bntu.baranouski.core.service;

import by.bntu.baranouski.core.model.Consumer;
import by.bntu.baranouski.core.model.Producer;
import by.bntu.baranouski.core.model.RouteNode;
import by.bntu.baranouski.core.model.TransportationState;
import by.bntu.baranouski.core.solver.InitialPhaseSolver;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InitialPhaseSolverService {

    InitialPhaseSolver solver = new InitialPhaseSolver();

    public List<TransportationState> solveVogel(TransportationState initialState){
        return solver.solve(initialState, state->{
            state.setComment("creating reference plan (Vogel approximation method)");
            return findVogelNode(state);
        });
    }

    public List<TransportationState> solveMinimal(TransportationState initialState){
        return solver.solve(initialState, state-> {
            state.setComment("creating reference plan (minimal element method)");
            return state.nodes()
                    .stream()
                    .filter(e ->
                            e.getProducer().getCurrentSupply().compareTo(BigDecimal.ZERO) > 0
                                    && e.getConsumer().getCurrentDemand().compareTo(BigDecimal.ZERO) > 0
                                    && e.getTaken() == null)
                    .min(Comparator.comparing(RouteNode::getTariff))
                    .orElseThrow();});
    }

    public List<TransportationState> solveNorthWest(TransportationState initialState){
        return solver.solve(initialState, state-> {
            state.setComment("creating reference plan (north-west node method)");
            return state.nodes()
                    .stream()
                    .filter(e ->
                            e.getProducer().getCurrentSupply().compareTo(BigDecimal.ZERO) > 0
                                    && e.getConsumer().getCurrentDemand().compareTo(BigDecimal.ZERO) > 0
                                    && e.getTaken() == null)
                    .min(Comparator.comparingInt(o -> o.getConsumer().getIndex() + o.getProducer().getIndex()))
                    .orElseThrow();
        });
    }

    private RouteNode findVogelNode(TransportationState initialState){
        List<RouteNode> nodes = initialState.nodes()
                .stream()
                .filter(e->
                        e.getProducer().getCurrentSupply().compareTo(BigDecimal.ZERO) > 0
                                && e.getConsumer().getCurrentDemand().compareTo(BigDecimal.ZERO) > 0
                                && e.getTaken() == null)
                .toList();

        Map<Producer, List<RouteNode>> nodesByProducers = nodes.stream()
                .collect(Collectors.groupingBy(RouteNode::getProducer));
        Map<Consumer, List<RouteNode>> nodesByConsumers = nodes.stream()
                .collect(Collectors.groupingBy(RouteNode::getConsumer));

        BigDecimal maximalDifference = BigDecimal.ZERO;
        List<RouteNode> chosenRowOrColumn = new ArrayList<>();

        for (var rowOrColumn :
                Stream.concat(
                        nodesByProducers.values().stream(),
                        nodesByConsumers.values().stream())
                .toList()){

            var twoSmallest = rowOrColumn.stream()
                    .sorted(Comparator.comparing(RouteNode::getTariff))
                    .limit(2L)
                    .toList();
            var difference  = twoSmallest.size() == 2 ?
                    twoSmallest.get(1).getTariff().subtract(twoSmallest.get(0).getTariff()).abs()
                    : BigDecimal.ZERO;
            if (difference.compareTo(maximalDifference) >= 0){
                maximalDifference = difference;
                chosenRowOrColumn = rowOrColumn;
            }
        }

        return chosenRowOrColumn.stream().min(Comparator.comparing(RouteNode::getTariff)).orElseThrow();
    }


}
