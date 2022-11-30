package by.bntu.baranouski.core.solver;

import by.bntu.baranouski.core.model.Cycle;
import by.bntu.baranouski.core.model.Potential;
import by.bntu.baranouski.core.model.RouteNode;
import by.bntu.baranouski.core.model.TransportationState;
import by.bntu.baranouski.core.model.util.KineticPriorityQueue;
import by.bntu.baranouski.core.model.util.PotentialEquationWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.*;

public class SecondPhaseSolver {

    public List<TransportationState> solve(TransportationState initialState) {
        boolean calculated = false;
        List<TransportationState> secondPhaseStates = new ArrayList<>();
        do {
            var potentialStates = new ArrayList<TransportationState>();
            try {
                if (isDegenerated(initialState)){
                    potentialStates.add(removeDegeneration(initialState));
                }
                potentialStates.add(!potentialStates.isEmpty()
                        ? calculatePotentials(potentialStates.get(0))
                        : calculatePotentials(initialState));
            } catch (Exception e) {
                potentialStates.clear();
                continue;
            }
            secondPhaseStates.addAll(potentialStates);
            calculated = true;
        } while (!calculated && !Thread.currentThread().isInterrupted());
        secondPhaseStates.add(calculateAlphas(secondPhaseStates.get(secondPhaseStates.size() - 1)));

        var minAlphaNode = findMinimalAlpha(secondPhaseStates.get(secondPhaseStates.size() - 1));
        while (minAlphaNode.getAlpha().compareTo(BigDecimal.ZERO) < 0 && !Thread.currentThread().isInterrupted()) {
            secondPhaseStates.add(buildCycle(
                    secondPhaseStates.get(secondPhaseStates.size() - 1)));
            secondPhaseStates.add(buildRedistributedState(
                    secondPhaseStates.get(secondPhaseStates.size() - 1)));
            calculated = false;
            do {
                var potentialStates = new ArrayList<TransportationState>();
                try {
                    if (isDegenerated(secondPhaseStates.get(secondPhaseStates.size() - 1))){
                        potentialStates.add(removeDegeneration(secondPhaseStates.get(secondPhaseStates.size() - 1)));
                    }
                    potentialStates.add(!potentialStates.isEmpty()
                            ? calculatePotentials(potentialStates.get(0))
                            : calculatePotentials(secondPhaseStates.get(secondPhaseStates.size() - 1)));
                } catch (Exception e) {
                    potentialStates.clear();
                    continue;
                }
                secondPhaseStates.addAll(potentialStates);
                secondPhaseStates.add(calculateAlphas(secondPhaseStates.get(secondPhaseStates.size() - 1)));
                calculated = true;
            } while (!calculated && !Thread.currentThread().isInterrupted());
            minAlphaNode = findMinimalAlpha(secondPhaseStates.get(secondPhaseStates.size() - 1));
        }

        TransportationState finalState = secondPhaseStates.get(secondPhaseStates.size()-1).copy();
        finalState.setComment("final optimized state");
        finalState.producers().forEach(p->p.setPotential(new Potential()));
        finalState.consumers().forEach(p->p.setPotential(new Potential()));
        finalState.nodes().forEach(node-> node
                .setAlpha(null)
                .setOpt(null)
                .setTaken(node.getTaken() != null && BigDecimal.ZERO.equals(node.getTaken()) ? null : node.getTaken()));
        secondPhaseStates.add(finalState);
        return secondPhaseStates;
    }

    private boolean isDegenerated(TransportationState state){
        long baseNodeAmount = state.nodes().stream()
                .filter(node -> node.getTaken() != null).count();
        return baseNodeAmount < state.producers().size() + state.consumers().size() - 1;
    }

    private TransportationState removeDegeneration(TransportationState initialState) {
        TransportationState nextState = initialState.copy();
        nextState.setComment("removing degeneration");
        nextState.nodes().forEach(node->node.setAlpha(null).setOpt(null));
        long baseNodeAmount = nextState.nodes().stream()
                .filter(node -> node.getTaken() != null).count();

        while (baseNodeAmount < nextState.producers().size() + nextState.consumers().size() - 1) {
            var acyclicNodes = findAcyclicFreeNodes(nextState.nodes());
            RouteNode node = acyclicNodes.isEmpty()
                    ? getRandomFreeNode(nextState.nodes())
                    : acyclicNodes.get(ThreadLocalRandom.current().nextInt(acyclicNodes.size()));
            node.setTaken(BigDecimal.ZERO);
            baseNodeAmount++;
        }
        return nextState;
    }

    private List<RouteNode> findAcyclicFreeNodes(List<RouteNode> nodes) {
        var acyclicNodes = new ArrayList<RouteNode>();
        var freeNodes = nodes.stream().filter(node -> node.getTaken() == null).toList();
        var occupiedNodes = nodes.stream().filter(node -> node.getTaken() != null).toList();
        freeNodes.forEach(node -> {
            var sameConsumerNodes = occupiedNodes.stream()
                    .filter(occupiedNode -> occupiedNode.getConsumer().equals(node.getConsumer()))
                    .count();
            var sameProducerNodes = occupiedNodes.stream()
                    .filter(occupiedNode -> occupiedNode.getProducer().equals(node.getProducer()))
                    .count();
            if (sameProducerNodes == 0 ^ sameConsumerNodes == 0) {
                acyclicNodes.add(node);
            }
        });
        return acyclicNodes;
    }

    private RouteNode getRandomFreeNode(List<RouteNode> nodes) {
        var freeNodes = nodes.stream().filter(node -> node.getTaken() == null).toList();
        return freeNodes.get(ThreadLocalRandom.current().nextInt(freeNodes.size()));
    }

    private TransportationState calculatePotentials(TransportationState initialState) {
        var nextState = initialState.copy();
        nextState.nodes().forEach(node->node.setAlpha(null).setOpt(null));
        nextState.setComment("calculating potentials");
        var occupiedNodes = nextState.nodes().stream().filter(node -> node.getTaken() != null).toList();
        var kineticEquationQueue = new KineticPriorityQueue<>(
                PotentialEquationWrapper::getNumberOfKnownVariables,
                occupiedNodes.stream().map(node -> new PotentialEquationWrapper(
                        node.getConsumer().getPotential(),
                        node.getProducer().getPotential(),
                        node.getTariff())).toList());
        occupiedNodes.get(0).getProducer().getPotential().setValue(BigDecimal.ZERO);
        while (kineticEquationQueue.size() != 0) {
            var equation = kineticEquationQueue.popNext(1);
            if (equation.getProducerPotential().getValue() == null) {
                BigDecimal tariff = equation.getTariff();
                BigDecimal consumerPotential = equation.getConsumerPotential().getValue();
                equation.getProducerPotential().setValue(tariff.subtract(consumerPotential));
            }
            if (equation.getConsumerPotential().getValue() == null) {
                BigDecimal tariff = equation.getTariff();
                BigDecimal producerPotential = equation.getProducerPotential().getValue();
                equation.getConsumerPotential().setValue(tariff.subtract(producerPotential));
            }
        }
        return nextState;
    }

    private TransportationState calculateAlphas(TransportationState initialState) {
        TransportationState nextState = initialState.copy();
        nextState.setComment("calculating nodes' alphas");
        nextState.nodes().forEach(node -> {
            var consumerPotential = node.getConsumer().getPotential().getValue();
            var producerPotential = node.getProducer().getPotential().getValue();
            node.setAlpha(
                    node.getTaken() == null
                            ? node.getTariff().subtract(consumerPotential).subtract(producerPotential)
                            : BigDecimal.ZERO
            );
        });
        return nextState;
    }

    private RouteNode findMinimalAlpha(TransportationState state) {
        return state.nodes().stream().min(Comparator.comparing(RouteNode::getAlpha)).orElseThrow();
    }

    private TransportationState buildCycle(TransportationState initialState){
        TransportationState nextState = initialState.copy();
        nextState.setComment("building cycle");
        nextState.producers().forEach(e->e.setPotential(new Potential()));
        nextState.consumers().forEach(e->e.setPotential(new Potential()));
        var minAlpha = findMinimalAlpha(nextState);
        var cycleNodePool = nextState.nodes().stream().filter(node->node.getTaken() != null).toList();
        var cycle = new Cycle(List.of(minAlpha));
        nextState.cycle().addAll(buildCycle(cycle, cycleNodePool));

        for (int i = 0, plus = 1; i<nextState.cycle().size()-1; i++, plus = -plus){
            nextState.cycle().get(i)
                    .setOpt(plus > 0 ? RouteNode.OptimizationValue.PLUS : RouteNode.OptimizationValue.MINUS);
        }
        return nextState;
    }

    private TransportationState buildRedistributedState(TransportationState initialState){
        TransportationState nextState = initialState.copy(true);
        nextState.setComment("building redistributed state");
        var minNodeTaken = nextState.cycle().stream()
                .filter(node->node.getOpt().equals(RouteNode.OptimizationValue.MINUS))
                .min(Comparator.comparing(RouteNode::getTaken)).orElseThrow().getTaken();
        for (int i = 0; i<nextState.cycle().size()-1; i++){
            if (nextState.cycle().get(i).getOpt().equals(RouteNode.OptimizationValue.PLUS)){
                nextState.cycle().get(i).setTaken(
                        nextState.cycle().get(i).getTaken() == null
                                ? minNodeTaken
                                : nextState.cycle().get(i).getTaken().add(minNodeTaken));
            } else {
                var result = nextState.cycle().get(i).getTaken().subtract(minNodeTaken);
                nextState.cycle().get(i).setTaken(result.equals(BigDecimal.ZERO) ? null : result);
            }
        }
        return nextState;
    }

    private Cycle buildCycle(Cycle cycle, List<RouteNode> nodePool){
        if(Thread.currentThread().isInterrupted()) return cycle;
        if (cycle.size()>3 && cycle.get(0).equals(cycle.get(cycle.size()-1))){
            return cycle;
        }
        var availableNodes = nodePool.stream()
                .filter(node->isNodeAvailable(node, cycle)).toList();
        if (canFinishCycle(cycle)) {
            cycle.add(cycle.get(0));
            return cycle;
        }
        if (availableNodes.isEmpty()){
            return cycle;
        }

        Cycle result = new Cycle(cycle);
        for (var node : availableNodes){
            result = new Cycle(cycle);
            result.add(node);
            result = buildCycle(result, nodePool);
            if (result.size()> 3 && result.get(0).equals(result.get(result.size()-1))){
                return result;
            }
        }
        return result;
    }

    private boolean isNodeAvailable(RouteNode toCheck, Cycle cycle) {
        var finalNode = cycle.get(cycle.size()-1);

        if (!finalNode.getConsumer().equals(toCheck.getConsumer())
                && !finalNode.getProducer().equals(toCheck.getProducer())){
            return false;
        }
        var preFinal = cycle.size() > 1 ? cycle.get(cycle.size()-2) : null;

        if (isNodeInsideOrCoveredByCycle(toCheck, cycle)){
            return false;
        }
        if (preFinal != null){
            if (finalNode.getConsumer().equals(preFinal.getConsumer()) && toCheck.getConsumer().equals(finalNode.getConsumer())){
                return false;
            }
            if (finalNode.getProducer().equals(preFinal.getProducer()) && toCheck.getProducer().equals(finalNode.getProducer())){
                return false;
            }
        }
        return true;
    }

    private boolean isNodeInsideOrCoveredByCycle(RouteNode toCheck, Cycle cycle) {
        var prod = toCheck.getProducer();
        var cons = toCheck.getConsumer();
        if (cycle.contains(toCheck)){
            return true;
        } else if (cycle.size()==1){
            return false;
        }

        for (int i = 0, j = 1; i < cycle.size(); i++, j = (i != cycle.size() - 1) ? i + 1 : 0) {
            var n1 = cycle.get(i);
            var n2 = cycle.get(j);

            if (n1.getProducer().equals(n2.getProducer())
                    && toCheck.getProducer().equals(n1.getProducer())
                    && prod.getIndex() < max(n1.getProducer().getIndex(), n2.getProducer().getIndex())
                    && prod.getIndex() > min(n1.getProducer().getIndex(), n2.getProducer().getIndex())) {
                return true;
            }
            if (n1.getConsumer().equals(n2.getConsumer())
                    && toCheck.getConsumer().equals(n1.getConsumer())
                    && cons.getIndex() < max(n1.getConsumer().getIndex(), n2.getConsumer().getIndex())
                    && cons.getIndex() > min(n1.getConsumer().getIndex(), n2.getConsumer().getIndex())) {
                return true;
            }
        }
        return false;
    }

    private boolean canFinishCycle(Cycle cycle){
        if (cycle.size()<4){
            return false;
        }
        var finish = cycle.get(cycle.size()-1);
        var start = cycle.get(0);
        List<RouteNode> occupiedNodesInBetween;
        if (finish.getConsumer().equals(start.getConsumer())){
            occupiedNodesInBetween = cycle.stream()
                    .filter(node -> node != start && node != finish)
                    .filter(node -> node.getConsumer().equals(finish.getConsumer()) && cycle.contains(node))
                    .toList();
            if (occupiedNodesInBetween.isEmpty()){
                return true;
            }
        }
        if (finish.getProducer().equals(start.getProducer())){
            occupiedNodesInBetween = cycle.stream()
                    .filter(node -> node != start && node != finish)
                    .filter(node -> node.getProducer().equals(finish.getProducer()) && cycle.contains(node))
                    .toList();
            if (occupiedNodesInBetween.isEmpty()){
                return true;
            }
        }
        return false;
    }
}
