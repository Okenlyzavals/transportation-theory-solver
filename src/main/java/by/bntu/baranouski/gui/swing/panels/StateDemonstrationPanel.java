package by.bntu.baranouski.gui.swing.panels;

import by.bntu.baranouski.core.model.*;
import by.bntu.baranouski.gui.swing.panels.util.GridBagHelper;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.SwingConstants.CENTER;

@Getter
public class StateDemonstrationPanel extends JPanel {
    private static final Color INACTIVE_PANEL_COLOR = new Color(220, 220, 220);
    private static final DecimalFormat FORMAT = new DecimalFormat("#0.##");
    private final Cycle cycle;
    private final List<RouteNodePanel> routeNodePanels = new ArrayList<>();

    public StateDemonstrationPanel(TransportationState state) {
        cycle = state.cycle();
        GridBagHelper helper = new GridBagHelper();
        setLayout(new GridBagLayout());
        helper.nextRow().nextCell();

        for (var consumer : state.consumers()){
            helper.nextCell().setInsets(1,1,1,1);
            add(new ConsumerPanel(consumer), helper.get());
        }
        helper.nextCell().setInsets(1,1,1,1);
        add(new JLabel("Init. supply: ", CENTER), helper.get());
        helper.nextCell().setInsets(1,1,1,1);
        add(new JLabel("Curr. supply: ", CENTER), helper.get());

        for (var producer : state.producers()){
            helper.nextRow().nextCell().setInsets(1,1,1,1);
            add(new ProducerPanel(producer), helper.get());

            for (var consumer : state.consumers()){
                helper.nextCell().setInsets(1,1,1,1);
                RouteNode node = state.nodes().stream()
                        .filter(e->e.getConsumer().equals(consumer)
                                && e.getProducer().equals(producer))
                        .findFirst().orElseThrow();
                JPanel borderPane = new JPanel(new BorderLayout());
                borderPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                var routePane = new RouteNodePanel(node);
                if (producer.getCurrentSupply().equals(BigDecimal.ZERO)
                        || consumer.getCurrentDemand().equals(BigDecimal.ZERO)){
                    routePane.setBackground(INACTIVE_PANEL_COLOR);
                }
                routeNodePanels.add(routePane);
                borderPane.add(routePane);
                add(borderPane, helper.get());
            }
            helper.nextCell().setInsets(1,1,1,1);
            add(new JLabel(FORMAT.format(producer.getInitialSupply()), CENTER), helper.get());
            helper.nextCell().setInsets(1,1,1,1);
            add(new JLabel(FORMAT.format(producer.getCurrentSupply()), CENTER), helper.get());
        }
        helper.nextRow().nextCell().setInsets(1,1,1,1);
        add(new JLabel("Init. demand: ", CENTER), helper.get());
        for (var consumer : state.consumers()){
            helper.nextCell().setInsets(1,1,1,1);
            add(new JLabel(FORMAT.format(consumer.getInitialDemand()), CENTER), helper.get());
        }
        helper.nextRow().nextCell().setInsets(1,1,1,1);
        add(new JLabel("Curr. demand: ", CENTER), helper.get());
        for (var consumer : state.consumers()){
            helper.nextCell().setInsets(1,1,1,1);
            add(new JLabel(FORMAT.format(consumer.getCurrentDemand()), CENTER), helper.get());
        }
        if (state.allConsumersSatisfied()){
            helper.nextRow().nextCell().setInsets(1,2,1,0).alignCenter().span();
            var fLabel = new JLabel("F = "+ FORMAT.format(state.calculateRouteCost()));
            fLabel.setFont(new Font("serif",Font.BOLD, 24));
            add(fLabel,helper.get());
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (cycle!= null && !cycle.isEmpty()){
            g.setColor(new Color(1,0,0,0.8f));
            ((Graphics2D)g).setStroke(new BasicStroke(3));
            for (int i = 0, j =1; i<cycle.size()-1; i++, j = i+1){
                var currNode = cycle.get(i);
                var nextNode = cycle.get(j);
                var currentPanel = routeNodePanels.stream()
                        .filter(panel->panel.getNode().equals(currNode))
                        .findFirst().orElseThrow();
                var nextPanel = routeNodePanels.stream()
                        .filter(panel->panel.getNode().equals(nextNode))
                        .findFirst().orElseThrow();
                g.drawLine(currentPanel.getParent().getX()+currentPanel.getWidth()/2,
                        currentPanel.getParent().getY()+currentPanel.getHeight()/2,
                        nextPanel.getParent().getX()+nextPanel.getWidth()/2,
                        nextPanel.getParent().getY()+nextPanel.getHeight()/2);
            }
        }
    }

    private class ConsumerPanel extends JPanel{
        ConsumerPanel(Consumer consumer){
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel indexLabel = new JLabel("C"+consumer.getIndex(), SwingConstants.LEFT);
            indexLabel.setAlignmentY(CENTER_ALIGNMENT);
            add(indexLabel);
            if (consumer.getPotential().getValue()!=null){
                JLabel potentialLabel = new JLabel(
                        "V"+consumer.getIndex()+"= "+FORMAT.format(consumer.getPotential().getValue()),
                        SwingConstants.LEFT);
                add(potentialLabel);
            }
        }
    }

    private class ProducerPanel extends JPanel{
        ProducerPanel(Producer producer){
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            JLabel indexLabel = new JLabel("P"+producer.getIndex(), SwingConstants.LEFT);
            indexLabel.setAlignmentY(CENTER_ALIGNMENT);
            add(indexLabel);
            if (producer.getPotential().getValue()!=null){
                JLabel potentialLabel = new JLabel(
                        "U"+producer.getIndex()+"= "+FORMAT.format(producer.getPotential().getValue()),
                        SwingConstants.LEFT);
                add(potentialLabel);
            }
        }
    }
}
