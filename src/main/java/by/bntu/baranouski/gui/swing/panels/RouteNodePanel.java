package by.bntu.baranouski.gui.swing.panels;

import by.bntu.baranouski.gui.swing.panels.util.JDecimalField;
import by.bntu.baranouski.core.model.RouteNode;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

import static by.bntu.baranouski.core.model.RouteNode.OptimizationValue.PLUS;

@Getter
public class RouteNodePanel extends JPanel {
    private static final DecimalFormat FORMAT = new DecimalFormat("#0.##");

    private final JDecimalField tariffField = new JDecimalField();
    private final RouteNode node;

    public RouteNodePanel(RouteNode node) {
        this.node = node;
        setLayout(new GridLayout(2,2));

        JLabel alphaLabel = new JLabel("   ",SwingConstants.CENTER);
        JLabel takenLabel = new JLabel("   ",SwingConstants.CENTER);
        tariffField.setHorizontalAlignment(SwingConstants.CENTER);

        if (node.getAlpha() != null){
            alphaLabel.setText(FORMAT.format(node.getAlpha()));
        }
        if (node.getTaken() != null){
            takenLabel.setText(FORMAT.format(node.getTaken()));
        }
        if (node.getTariff() != null){
            tariffField.setText(FORMAT.format(node.getTariff()));
            tariffField.setEditable(false);
        }

        add(tariffField);
        add(alphaLabel);
        add(new JLabel(node.getOpt() == null ? " " : node.getOpt() == PLUS ? "+" : "-", SwingConstants.CENTER));
        add(takenLabel);
        setPreferredSize(new Dimension(60,60));
    }

    public void updateRoutePanel(){
        node.setTariff(tariffField.getBigDecimalValue());
    }

    public String toString(){
        DecimalFormat format = new DecimalFormat("##.###");
        return String.format("N(%d,%d);<br>t=%s;<br>α=%s;<br> val=%s.",
                node.getProducer().getIndex(),
                node.getConsumer().getIndex(),
                format.format(node.getTariff()),
                node.getAlpha() == null ? "..." : format.format(node.getAlpha()),
                node.getTaken() == null ? "..." : format.format(node.getTaken()));
    }
}
