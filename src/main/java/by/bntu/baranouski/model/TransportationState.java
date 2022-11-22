package by.bntu.baranouski.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TransportationState {
    List<Producer> producers;
    List<Consumer> consumers;
    List<RouteNode> nodes;
    Cycle cycle;
    String comment;


    public TransportationState(List<Producer> producers, List<Consumer> consumers, List<RouteNode> nodes) {
        this.producers = producers;
        this.consumers = consumers;
        this.nodes = nodes;
    }

    public TransportationState copy() {
        return copy(false);
    }

    public TransportationState copy(boolean withCycle){
        TransportationState clonedState = new TransportationState(
                producers.stream().map(Producer::new).toList(),
                consumers.stream().map(Consumer::new).toList(),
                new ArrayList<>());
        clonedState.cycle = new Cycle();
        for (RouteNode node : nodes) {
            Producer newProducer = clonedState.producers.get(
                    clonedState.producers.indexOf(node.getProducer()));
            Consumer newConsumer = clonedState.consumers.get(
                    clonedState.consumers.indexOf(node.getConsumer()));
            RouteNode clonedNode = new RouteNode(newProducer, newConsumer)
                    .setTaken(node.getTaken())
                    .setTariff(node.getTariff())
                    .setAlpha(node.getAlpha())
                    .setOpt(node.getOpt());
            clonedState.nodes.add(clonedNode);
        }
        if (withCycle){
            cycle.forEach(n->
                    clonedState.cycle.add(
                            clonedState.nodes.get(clonedState.nodes.indexOf(n))));
        }
        return clonedState;
    }


    public BigDecimal calculateRouteCost() {
        BigDecimal f = BigDecimal.ZERO;
        for (var node : nodes) {
            if (node.getTaken() != null) {
                f = f.add(node.getTaken().multiply(node.getTariff()));
            }
        }
        return f;
    }

    public boolean allConsumersSatisfied() {
        return consumers
                .stream()
                .allMatch(e -> e.getCurrentDemand().equals(BigDecimal.ZERO));
    }

    public List<Producer> producers() {
        return producers;
    }

    public List<Consumer> consumers() {
        return consumers;
    }

    public List<RouteNode> nodes() {
        return nodes;
    }

    public Cycle cycle() {
        return cycle;
    }

    public String comment() {
        return comment;
    }
}
