package by.bntu.baranouski.gui.swing.panels.util;

import javax.swing.*;
import java.awt.*;

/**
 * A utility class that is used to simplify
 * the process of configuration of {@link GridBagLayout}
 *
 * @author Baranouski Y. K.
 * @version 1.0.0
 */
public final class GridBagHelper {
    private int gridX;
    private int gridY;
    private GridBagConstraints constraints;

    public GridBagConstraints get() {
        return constraints;
    }

    /**
     * Creates a basic panel with GridBagLayout
     * using given two-dimensional array of components to fill it.
     * Components are aligned to the left.
     * @param components Array of components.
     *                   Order matters - it determines the components' position on grid
     *                   The format is: ([row][cell]).
     * @return A panel with constructed {@link GridBagLayout} filled with given components.
     */
    public static JPanel constructDefaultGridBag( JComponent[][] components){
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagHelper helper = new GridBagHelper();

        for (int i = 0; i < components[0].length; i++){
            helper.nextRow();
            for (int k = 0; k<components.length;k++){
                helper.nextCell().setInsets(5,5,5,5).alignLeft();
                components[k][i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                panel.add(components[k][i], helper.get());
            }
            helper.nextCell().setInsets(5,5,5,5).alignLeft().span();
            panel.add(components[components.length-1][i], helper.get());
        }

        return panel;
    }

    /**
     * Moves to next cell.
     */
    public GridBagHelper nextCell() {
        constraints = new GridBagConstraints();
        constraints.gridx = gridX++;
        constraints.gridy = gridY;
        return this;
    }

    /**
     * Moves to next row.
     */
    public GridBagHelper nextRow() {
        if (constraints == null){
            constraints = new GridBagConstraints();
        }
        gridY++;
        gridX = 0;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        return this;
    }

    /**
     * Stretches content of current cell to fill the horizontal dimension of layout.
     */
    public GridBagHelper span() {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }

    /**
     * Aligns content of current cell to the left.
     */
    public GridBagHelper alignLeft() {
        constraints.anchor = GridBagConstraints.LINE_START;
        return this;
    }
    /**
     * Aligns content of current cell to the center.
     */
    public GridBagHelper alignCenter() {
        constraints.anchor = GridBagConstraints.CENTER;
        return this;
    }

    /**
     * Sets insets for the current cell.
     */
    public GridBagHelper setInsets(int left, int top, int right, int bottom) {
        constraints.insets = new Insets(top, left, bottom, right);
        return this;
    }
}
