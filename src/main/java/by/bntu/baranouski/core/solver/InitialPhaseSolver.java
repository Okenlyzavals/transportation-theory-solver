package by.bntu.baranouski.core.solver;

import by.bntu.baranouski.core.model.Consumer;
import by.bntu.baranouski.core.model.Producer;
import by.bntu.baranouski.core.model.RouteNode;
import by.bntu.baranouski.core.model.TransportationState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class InitialPhaseSolver {

    public List<TransportationState> solve(TransportationState initialState,
                                           Function<TransportationState, RouteNode> nodePickFunction){

        if (isNonsensical(initialState)){
            throw new IllegalArgumentException("Consumers demand more than suppliers offer! Solving is of no sense.");
        }

        List<TransportationState> resultList = new ArrayList<>();
        resultList.add(initialState);
        initialState.setComment("initial state");

        TransportationState tempState = initialState;
        while (!tempState.allConsumersSatisfied()){
            tempState = solveSingleStep(tempState, nodePickFunction);
            resultList.add(tempState);
        }
        resultList.get(resultList.size()-1).setComment("final reference plan");
        return resultList;
    }

    private TransportationState solveSingleStep(TransportationState startingState,
                                                Function<TransportationState, RouteNode> nodePickFunction){
        TransportationState nextState = startingState.copy();

        RouteNode pickedNode = nodePickFunction.apply(nextState);

        Producer producer = pickedNode.getProducer();
        Consumer consumer = pickedNode.getConsumer();

        if (consumer.getCurrentDemand().compareTo(producer.getCurrentSupply()) > 0){
            pickedNode.setTaken(producer.getCurrentSupply());
            consumer.setCurrentDemand(consumer.getCurrentDemand().subtract(producer.getCurrentSupply()));
            producer.setCurrentSupply(BigDecimal.ZERO);
        } else {
            pickedNode.setTaken(consumer.getCurrentDemand());
            producer.setCurrentSupply(producer.getCurrentSupply().subtract(consumer.getCurrentDemand()));
            consumer.setCurrentDemand(BigDecimal.ZERO);
        }

        return nextState;
    }

    private boolean isNonsensical(TransportationState state){
        BigDecimal conSum = state.getConsumers().stream().map(Consumer::getInitialDemand).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prodSum = state.getProducers().stream().map(Producer::getInitialSupply).reduce(BigDecimal.ZERO, BigDecimal::add);
        return conSum.compareTo(prodSum) > 0;
    }

}
