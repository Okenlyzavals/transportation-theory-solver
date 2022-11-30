package by.bntu.baranouski.gui.swing.panels;

import by.bntu.baranouski.core.model.Consumer;
import by.bntu.baranouski.core.model.Producer;
import by.bntu.baranouski.core.model.RouteNode;
import by.bntu.baranouski.core.model.TransportationState;
import by.bntu.baranouski.gui.swing.panels.util.GridBagHelper;
import by.bntu.baranouski.gui.swing.panels.util.JDecimalField;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static javax.swing.SwingConstants.CENTER;

public class StateInputPanel extends JPanel{

    private final List<EntityInputFieldTuple<Producer>> producers;
    private final List<EntityInputFieldTuple<Consumer>> consumers;
    @Getter
    private final List<RouteNodePanel> nodes;

    public StateInputPanel(int producerAmount, int consumerAmount) {
        super();
        producers = new ArrayList<>(producerAmount);
        consumers = new ArrayList<>(consumerAmount);
        nodes = new ArrayList<>(producerAmount*consumerAmount);

        fillData(producerAmount, consumerAmount);
        createLayout();
    }

    public StateInputPanel(TransportationState state){
        this(state.getProducers().size(), state.getConsumers().size());
        for (int i = 0; i<producers.size();i++){
            producers.get(i).inputField.setBigDecimalValue(state.getProducers().get(i).getInitialSupply());
        }
        for (int i = 0; i<consumers.size();i++){
            consumers.get(i).inputField.setBigDecimalValue(state.getConsumers().get(i).getInitialDemand());
        }
        for (var nodePanel : nodes){
            var pickedNode = state.nodes().stream()
                    .filter(e->e.getProducer().getIndex().equals(nodePanel.getNode().getProducer().getIndex()))
                    .filter(e->e.getConsumer().getIndex().equals(nodePanel.getNode().getConsumer().getIndex()))
                    .findFirst().orElseThrow();
            nodePanel.getTariffField().setBigDecimalValue(pickedNode.getTariff());
        }
    }

    public void reset(){
        nodes.forEach(n->{
            n.getTariffField().setText("0");
            n.getNode().setTariff(null);
        });
        producers.forEach(p->{
            p.inputField.setText("0");
            p.getEntity().setInitialSupply(null);
        });
        consumers.forEach(c->{
            c.inputField.setText("0");
            c.getEntity().setInitialDemand(null);
        });
    }

    public void fillWithRandomData(){
        nodes.forEach(n->n
                .getTariffField()
                .setText(""+ThreadLocalRandom.current().nextInt(0,30)));
        List<Integer> demands = new ArrayList<>();
        consumers.forEach(c-> {
            int rand = ThreadLocalRandom.current().nextInt(50, 150);
            demands.add(rand);
            c.inputField.setText(""+rand);
        });
        Double supplySum = demands.stream().mapToInt(Integer::intValue).sum() * ThreadLocalRandom.current().nextDouble(1d, 2d);
        List<Double> partitions = generatePartitions(producers.size());

        for (int i = 0; i<producers.size(); i++){
            int supply = (int)Math.ceil(supplySum*partitions.get(i));
            producers.get(i).inputField.setText(""+supply);
        }

        this.revalidate();
        this.repaint();
    }

    private List<Double> generatePartitions(int number){
        List<Integer> ints = IntStream
                .range(0, number)
                .mapToObj(i -> ThreadLocalRandom.current().nextInt(1000) + 1).toList();
        int sum = ints.stream().mapToInt(Integer::intValue).sum();
        return ints.stream().mapToDouble(e->(double)e/sum).boxed().toList();
    }

    public TransportationState readState() throws IllegalArgumentException{
        producers.forEach(panel->
                panel.updateDataInEntity(producer-> {
                    producer.setInitialSupply(panel.inputField.getBigDecimalValue());
                    producer.setCurrentSupply(producer.getInitialSupply());
                }));
        consumers.forEach(panel->
                panel.updateDataInEntity(consumer-> {
                    consumer.setInitialDemand(panel.inputField.getBigDecimalValue());
                    consumer.setCurrentDemand(consumer.getInitialDemand());
                }));
        nodes.forEach(RouteNodePanel::updateRoutePanel);

        return new TransportationState(
                producers.stream().map(EntityInputFieldTuple::getEntity).toList(),
                consumers.stream().map(EntityInputFieldTuple::getEntity).toList(),
                nodes.stream().map(RouteNodePanel::getNode).toList());
    }

    private void createLayout(){
        setLayout(new GridBagLayout());
        GridBagHelper helper = new GridBagHelper();
        helper.nextRow().nextCell();

        for (var consumer : consumers){
            helper.nextCell().setInsets(1,1,1,1);
            var label = new JLabel("Cons. "+consumer.entity.getIndex(), CENTER);
            add(label, helper.get());
        }
        helper.nextCell().setInsets(1,1,1,1);
        add(new JLabel("Supply", CENTER), helper.get());

        for (var producer : producers){
            helper.nextRow().nextCell().setInsets(1,1,1,1);
            add(new JLabel("Prod. "+producer.entity.getIndex(), CENTER), helper.get());

            for (var consumer : consumers){
                helper.nextCell().setInsets(1,1,1,1);
                RouteNodePanel nodePanel = nodes.stream()
                        .filter(e->e.getNode().getConsumer().equals(consumer.entity)
                                && e.getNode().getProducer().equals(producer.entity))
                        .findFirst().orElseThrow();
                JPanel borderPane = new JPanel(new BorderLayout());
                borderPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                borderPane.add(nodePanel);
                add(borderPane, helper.get());
            }
            helper.nextCell().setInsets(1,1,1,1);
            add(producer.getInputField(), helper.get());
        }
        helper.nextRow().nextCell().setInsets(1,1,1,1);
        add(new JLabel("Demand", CENTER), helper.get());
        for (var consumer : consumers){
            helper.nextCell().setInsets(1,1,1,1);
            add(consumer.getInputField(), helper.get());
        }
    }

    private void fillData(int producerAmount, int consumerAmount){
        for (int i = 1; i<=consumerAmount; i++){
            Consumer producer = new Consumer(i);
            consumers.add(new EntityInputFieldTuple<>(producer));
        }

        for (int i = 1; i<=producerAmount; i++){
            Producer producer = new Producer(i);
            producers.add(new EntityInputFieldTuple<>(producer));
        }
        for (var consumer : consumers){
            for (var producer : producers){
                RouteNode node = new RouteNode(producer.entity, consumer.entity);
                nodes.add(new RouteNodePanel(node));
            }
        }
    }

    @Getter
    @Setter
    private static class EntityInputFieldTuple<T> {
        private final JDecimalField inputField;
        private final T entity;

        public EntityInputFieldTuple(T entity){
            this.entity = entity;
            inputField = new JDecimalField();
            inputField.setMinimumBigDecimalValue(BigDecimal.ZERO);
            inputField.setHorizontalAlignment(CENTER);
            inputField.setEditable(true);
            inputField.setColumns(5);
        }

        public void updateDataInEntity(java.util.function.Consumer<T> action){
            action.accept(entity);
        }

    }

}
