package by.bntu.baranouski.gui.swing.panels;

import by.bntu.baranouski.controller.TransportationController;
import by.bntu.baranouski.gui.swing.InitFrame;
import by.bntu.baranouski.gui.swing.MainFrame;
import by.bntu.baranouski.model.dto.SolutionDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

import static by.bntu.baranouski.controller.TransportationController.InitialSolveMethod.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LeftInputPanel extends JSplitPane {

    @NonFinal
    @Setter
    MainFrame.AppEventObserver observer;
    Dimension buttonSize = new Dimension(180, 25);
    String northWest = "North-west";
    String minimalElement = "Minimal element";
    String vogelApprox = "Vogel approximation";
    TransportationController controller = new TransportationController();

    JButton fillWithRandom = new JButton("Fill with random data");
    JButton rebuild = new JButton("Rebuild with new grid");
    JButton reset = new JButton("Reset data");
    JComboBox<String> solveMethod
            = new JComboBox<>(new String[]{northWest, minimalElement, vogelApprox});
    JButton solve = new JButton("Solve");

    @Getter
    JPanel panelWrapper = new JPanel(new BorderLayout());
    @Getter
    JPanel bottomPanel;
    @Getter
    @Setter
    @NonFinal
    StateInputPanel inputPanel;
    @Getter
    @NonFinal
    String solutionUid;

    public LeftInputPanel(int producerAmount, int consumerAmount) {
        super(VERTICAL_SPLIT, true);
        inputPanel = new StateInputPanel(producerAmount, consumerAmount);
        solutionUid = UUID.randomUUID().toString();

        panelWrapper.setBorder(BorderFactory.createTitledBorder("Input initial state: "));
        JScrollPane pane = new JScrollPane(inputPanel);
        panelWrapper.add(pane);
        setDividerSize(8);
        setOneTouchExpandable(true);

        setButtonParameters(fillWithRandom, rebuild, reset, solve, solveMethod);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));


        bottomPanel.add(Box.createVerticalStrut(20));
        bottomPanel.add(fillWithRandom);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(reset);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(rebuild);
        bottomPanel.add(Box.createVerticalStrut(10));

        bottomPanel.add(solveMethod);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(solve);
        bottomPanel.add(Box.createVerticalStrut(10));

        setBottomComponent(bottomPanel);
        setTopComponent(panelWrapper);
        setResizeWeight(1);

        fillWithRandom.addActionListener(e -> inputPanel.fillWithRandomData());
        reset.addActionListener(e -> {
            solutionUid = UUID.randomUUID().toString();
            inputPanel.reset();
            observer.notifyReset();
        });
        rebuild.addActionListener(e -> InitFrame.dialog()
                .ifPresent(p -> {
                    solutionUid = UUID.randomUUID().toString();
                    observer.notifyReset();
                    inputPanel = new StateInputPanel(p[0], p[1]);
                    panelWrapper.removeAll();
                    panelWrapper.add(new JScrollPane(inputPanel));
                    panelWrapper.revalidate();
                    panelWrapper.repaint();
                }));
        solveMethod.setSelectedIndex(0);
        solve.addActionListener(e -> observer.notifySolve());
    }

    public SolutionDto solve() {
        return switch ((String) solveMethod.getSelectedItem()) {
            case northWest -> controller.solve(inputPanel.readState(), NORTH_WEST);
            case vogelApprox -> controller.solve(inputPanel.readState(), VOGEL);
            case minimalElement -> controller.solve(inputPanel.readState(), MINIMAL);
            default -> throw new IllegalArgumentException();
        };
    }

    private void setButtonParameters(JComponent... components) {
        for (var comp : components) {
            comp.setAlignmentX(CENTER_ALIGNMENT);
            comp.setMaximumSize(buttonSize);
        }
    }
}
